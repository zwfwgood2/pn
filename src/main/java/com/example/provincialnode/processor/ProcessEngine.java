package com.example.provincialnode.processor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.provincialnode.common.Result;
import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.entity.SysProcessExecutionRecordEntity;
import com.example.provincialnode.entity.SysProcessNodeConfigEntity;
import com.example.provincialnode.processor.context.ProcessContext;
import com.example.provincialnode.service.SysProcessExecutionRecordService;
import com.example.provincialnode.service.SysProcessNodeConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 流程引擎类
 * 处理流程的执行、异步执行和重试逻辑
 */
@Slf4j
@Component
public class ProcessEngine {

    @Autowired
    private SysProcessNodeConfigService processNodeConfigService;

    @Autowired
    private SysProcessExecutionRecordService processExecutionRecordService;

    @Autowired
    private Map<String, Node> nodeMap;

    // 异步执行线程池
    private final ExecutorService asyncExecutor = new ThreadPoolExecutor(
            10, // 核心线程数
            50, // 最大线程数
            60L, TimeUnit.SECONDS, // 空闲线程存活时间
            new LinkedBlockingQueue<>(1000), // 工作队列
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
    );

    /**
     * 执行流程
     * @param processCode 流程代码
     * @param context 处理上下文
     * @return 执行结果
     */
    public Result<JSONObject> executeProcess(String processCode, ProcessContext context) {
        log.info("开始执行流程: {}, 请求ID: {}", processCode, context.getRequestId());

        // 创建流程执行记录
        SysProcessExecutionRecordEntity executionRecord = processExecutionRecordService.createExecutionRecord(
                processCode,
                context.getInterfaceCode(),
                context.getAppKey(),
                context.getRequestId()
        );

        // 记录执行记录ID到上下文
        context.setAttribute("executionId", executionRecord.getExecutionId());

        try {
            // 标记流程开始执行
            processExecutionRecordService.updateExecutionStatus(
                    executionRecord.getExecutionId(),
                    1, // 执行中
                    null
            );

            // 查询流程节点配置
            List<SysProcessNodeConfigEntity> nodeConfigs = processNodeConfigService.getEnabledNodesByProcessCode(processCode);
            if (nodeConfigs.isEmpty()) {
                log.error("流程未配置节点: {}", processCode);
                context.markFailure(ResultCode.SYSTEM_ERROR.getCode(), "系统内部错误: 流程未配置节点");
                return Result.error(context.getErrorCode(), context.getErrorMessage());
            }

            // 按顺序执行节点
            for (SysProcessNodeConfigEntity nodeConfig : nodeConfigs) {
                String nodeId = nodeConfig.getNodeId();
                String nodeName = nodeConfig.getNodeName();

                // 更新当前节点
                processExecutionRecordService.updateExecutionStatus(
                        executionRecord.getExecutionId(),
                        1, // 执行中
                        nodeId
                );

                // 解析节点配置
                Map<String, Object> nodeConfigMap = new HashMap<>();
                if (nodeConfig.getNodeConfig() != null && !nodeConfig.getNodeConfig().isEmpty()) {
                    try {
                        nodeConfigMap = JSON.parseObject(nodeConfig.getNodeConfig(), Map.class);
                    } catch (Exception e) {
                        log.error("解析节点配置失败: {}", e.getMessage());
                    }
                }
                context.setAttribute("nodeConfig", nodeConfigMap);

                // 获取节点实现
                Node node = nodeMap.get(nodeId);
                if (node == null) {
                    log.error("未找到节点实现: {}", nodeId);
                    context.markFailure(ResultCode.SYSTEM_ERROR.getCode(), "系统内部错误: 未找到节点实现");
                    break;
                }

                // 执行节点
                if (nodeConfig.getAsyncExecution()) {
                    // 异步执行
                    executeNodeAsync(node, context, nodeConfig);
                } else {
                    // 同步执行
                    executeNodeSync(node, context, nodeConfig);
                }

                // 如果执行失败，结束流程
                if (!context.isSuccess()) {
                    break;
                }
            }

            // 生成执行结果
            Result<JSONObject> result;
            if (context.isSuccess()) {
                result = Result.success(context.getAttribute("responseData"));
            } else {
                result = Result.error(context.getErrorCode(), context.getErrorMessage());
            }

            // 完成流程执行
            processExecutionRecordService.completeExecution(
                    executionRecord.getExecutionId(),
                    context.isSuccess() ? 2 : 3, // 2:成功, 3:失败
                    context.isSuccess() ? null : context.getErrorMessage()
            );

            log.info("流程执行完成: {}, 请求ID: {}, 结果: {}", 
                    processCode, context.getRequestId(), context.isSuccess() ? "成功" : "失败");
            return result;

        } catch (Exception e) {
            log.error("流程执行异常: {}", e.getMessage(), e);
            context.markFailure(ResultCode.SYSTEM_ERROR.getCode(), "系统内部错误");

            // 完成流程执行（失败）
            processExecutionRecordService.completeExecution(
                    executionRecord.getExecutionId(),
                    3, // 失败
                    "系统内部错误: " + e.getMessage()
            );

            return Result.error(context.getErrorCode(), context.getErrorMessage());
        } finally {
            // 记录执行上下文，用于重放
            processExecutionRecordService.recordExecutionContext(
                    executionRecord.getExecutionId(),
                    context
            );
        }
    }

    /**
     * 同步执行节点
     * @param node 节点
     * @param context 上下文
     * @param nodeConfig 节点配置
     */
    private void executeNodeSync(Node node, ProcessContext context, SysProcessNodeConfigEntity nodeConfig) {
        log.info("执行节点: {}-{}, 请求ID: {}", 
                node.getNodeId(), node.getNodeName(), context.getRequestId());

        // 获取重试配置
        RetryConfig retryConfig = getRetryConfig(nodeConfig.getRetryConfig());
        int retryCount = 0;

        boolean success = false;
        while (!success && retryCount <= retryConfig.getMaxRetryCount()) {
            try {
                if (retryCount > 0) {
                    // 重试等待
                    Thread.sleep(retryConfig.getDelayMs(retryCount));
                    log.info("重试执行节点: {}-{}, 重试次数: {}, 请求ID: {}", 
                            node.getNodeId(), node.getNodeName(), retryCount, context.getRequestId());
                }

                // 执行节点
                success = node.execute(context);
                retryCount++;
            } catch (Exception e) {
                log.error("节点执行异常: {}", e.getMessage(), e);
                retryCount++;
                if (retryCount > retryConfig.getMaxRetryCount()) {
                    context.markFailure(ResultCode.SYSTEM_ERROR.getCode(), "节点执行异常: " + e.getMessage());
                }
            }
        }

        // 处理节点执行结果
        if (!success && !context.isSuccess()) {
            // 节点执行失败且没有设置错误信息
            context.markFailure(ResultCode.SYSTEM_ERROR.getCode(), "节点执行失败: " + node.getNodeName());
        }
    }

    /**
     * 异步执行节点
     * @param node 节点
     * @param context 上下文
     * @param nodeConfig 节点配置
     */
    private void executeNodeAsync(Node node, ProcessContext context, SysProcessNodeConfigEntity nodeConfig) {
        log.info("异步执行节点: {}-{}, 请求ID: {}", 
                node.getNodeId(), node.getNodeName(), context.getRequestId());

        // 异步执行的结果处理可能需要额外的设计，这里简化处理
        CompletableFuture.runAsync(() -> {
            try {
                // 执行节点
                boolean success = node.execute(context);
                if (!success) {
                    log.error("异步节点执行失败: {}-{}, 请求ID: {}", 
                            node.getNodeId(), node.getNodeName(), context.getRequestId());
                    // 异步节点失败可能需要特殊处理，这里简化处理
                }
            } catch (Exception e) {
                log.error("异步节点执行异常: {}", e.getMessage(), e);
            }
        }, asyncExecutor);
    }

    /**
     * 重放流程执行
     * @param executionId 执行ID
     * @return 执行结果
     */
    public Result<?> replayProcess(String executionId) {
        log.info("开始重放流程执行: {}", executionId);

        try {
            // 获取执行记录
            SysProcessExecutionRecordEntity executionRecord = processExecutionRecordService.getByExecutionId(executionId);
            if(executionRecord==null){
               throw  new RuntimeException("执行记录不存在: " + executionId);
            }

            // 恢复上下文
            ProcessContext context = processExecutionRecordService.restoreContext(executionId);

            // 执行流程
            return executeProcess(executionRecord.getProcessCode(), context);
        } catch (Exception e) {
            log.error("流程重放异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR.getCode(), "流程重放异常: " + e.getMessage());
        }
    }

    /**
     * 获取重试配置
     * @param retryConfigJson 重试配置JSON字符串
     * @return 重试配置
     */
    private RetryConfig getRetryConfig(String retryConfigJson) {
        if (retryConfigJson == null || retryConfigJson.isEmpty()) {
            // 默认重试配置
            return new RetryConfig(3, 1000, 2.0);
        }

        try {
            Map<String, Object> configMap = JSON.parseObject(retryConfigJson, Map.class);
            int maxRetryCount = configMap.containsKey("maxRetryCount") ? 
                    ((Number) configMap.get("maxRetryCount")).intValue() : 3;
            int initialDelay = configMap.containsKey("initialDelay") ? 
                    ((Number) configMap.get("initialDelay")).intValue() : 1000;
            double multiplier = configMap.containsKey("multiplier") ? 
                    ((Number) configMap.get("multiplier")).doubleValue() : 2.0;
            
            return new RetryConfig(maxRetryCount, initialDelay, multiplier);
        } catch (Exception e) {
            log.error("解析重试配置失败: {}", e.getMessage());
            // 返回默认重试配置
            return new RetryConfig(3, 1000, 2.0);
        }
    }

    /**
     * 重试配置内部类
     */
    private static class RetryConfig {
        private final int maxRetryCount; // 最大重试次数
        private final int initialDelay; // 初始延迟（毫秒）
        private final double multiplier; // 延迟倍数

        public RetryConfig(int maxRetryCount, int initialDelay, double multiplier) {
            this.maxRetryCount = maxRetryCount;
            this.initialDelay = initialDelay;
            this.multiplier = multiplier;
        }

        /**
         * 获取指定重试次数的延迟时间（指数退避）
         * @param retryCount 重试次数
         * @return 延迟时间（毫秒）
         */
        public long getDelayMs(int retryCount) {
            return (long) (initialDelay * Math.pow(multiplier, retryCount - 1));
        }

        public int getMaxRetryCount() {
            return maxRetryCount;
        }
    }

}