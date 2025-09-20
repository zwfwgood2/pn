package com.example.provincialnode.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.provincialnode.entity.SysProcessExecutionRecordEntity;
import com.example.provincialnode.mapper.SysProcessExecutionRecordMapper;
import com.example.provincialnode.processor.context.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 流程执行记录服务类
 * 处理流程执行记录相关的业务逻辑，包括重试和重放功能
 */
@Slf4j
@Service
public class SysProcessExecutionRecordService extends ServiceImpl<SysProcessExecutionRecordMapper, SysProcessExecutionRecordEntity> {

    /**
     * 根据执行ID查询执行记录
     * @param executionId 执行ID
     * @return 执行记录
     */
    public SysProcessExecutionRecordEntity getByExecutionId(String executionId) {
        return getOne(new LambdaQueryWrapper<SysProcessExecutionRecordEntity>()
                .eq(SysProcessExecutionRecordEntity::getExecutionId, executionId));
    }

    /**
     * 根据请求ID查询执行记录
     * @param requestId 请求ID
     * @return 执行记录
     */
    public SysProcessExecutionRecordEntity getByRequestId(String requestId) {
        return getOne(new LambdaQueryWrapper<SysProcessExecutionRecordEntity>()
                .eq(SysProcessExecutionRecordEntity::getRequestId, requestId));
    }

    /**
     * 查询需要重试的执行记录
     * @return 执行记录列表
     */
    public List<SysProcessExecutionRecordEntity> getRetryableExecutionRecords() {
        // 查询状态为4（需要重试）且重试次数小于最大重试次数的记录
        return list(new LambdaQueryWrapper<SysProcessExecutionRecordEntity>()
                .eq(SysProcessExecutionRecordEntity::getStatus, 4)
                .lt(SysProcessExecutionRecordEntity::getRetryCount, 10));
    }

    /**
     * 创建流程执行记录
     * @param processCode 流程代码
     * @param interfaceCode 接口代码
     * @param appKey AppKey
     * @param requestId 请求ID
     * @return 执行记录
     */
    public SysProcessExecutionRecordEntity createExecutionRecord(String processCode, String interfaceCode, String appKey, String requestId) {
        SysProcessExecutionRecordEntity record = new SysProcessExecutionRecordEntity();
        record.setExecutionId(UUID.randomUUID().toString());
        record.setRequestId(requestId);
        record.setProcessCode(processCode);
        record.setInterfaceCode(interfaceCode);
        record.setAppKey(appKey);
        record.setStatus(0); // 初始状态
        record.setRetryCount(0);
        record.setMaxRetryCount(3);
        record.setStartTime(new Date());
        save(record);
        return record;
    }

    /**
     * 更新流程执行记录状态
     * @param executionId 执行ID
     * @param status 状态
     * @param currentNodeId 当前节点ID
     * @return 更新后的执行记录
     */
    public SysProcessExecutionRecordEntity updateExecutionStatus(String executionId, Integer status, String currentNodeId) {
        SysProcessExecutionRecordEntity record = getByExecutionId(executionId);
        if (record == null) {
            throw new RuntimeException("执行记录不存在: " + executionId);
        }
        record.setStatus(status);
        record.setCurrentNodeId(currentNodeId);
        record.setUpdateTime(new Date());
        updateById(record);
        return record;
    }

    /**
     * 完成流程执行
     * @param executionId 执行ID
     * @param status 状态
     * @param errorMessage 错误信息
     * @return 更新后的执行记录
     */
    public SysProcessExecutionRecordEntity completeExecution(String executionId, Integer status, String errorMessage) {
        SysProcessExecutionRecordEntity record = getByExecutionId(executionId);
        if (record == null) {
            throw new RuntimeException("执行记录不存在: " + executionId);
        }
        record.setStatus(status);
        record.setErrorMessage(errorMessage);
        record.setEndTime(new Date());
        record.setUpdateTime(new Date());
        updateById(record);
        return record;
    }

    /**
     * 记录执行上下文
     * @param executionId 执行ID
     * @param context 处理上下文
     * @return 更新后的执行记录
     */
    public SysProcessExecutionRecordEntity recordExecutionContext(String executionId, ProcessContext context) {
        SysProcessExecutionRecordEntity record = getByExecutionId(executionId);
        if (record == null) {
            throw new RuntimeException("执行记录不存在: " + executionId);
        }
        // 序列化上下文为JSON字符串
        record.setExecutionContext(JSON.toJSONString(context));
        record.setUpdateTime(new Date());
        updateById(record);
        return record;
    }

    /**
     * 标记流程需要重试
     * @param executionId 执行ID
     * @param errorMessage 错误信息
     * @return 更新后的执行记录
     */
    public SysProcessExecutionRecordEntity markForRetry(String executionId, String errorMessage) {
        SysProcessExecutionRecordEntity record = getByExecutionId(executionId);
        if (record == null) {
            throw new RuntimeException("执行记录不存在: " + executionId);
        }

        if (record.getRetryCount() >= record.getMaxRetryCount()) {
            // 超过最大重试次数，标记为失败
            return completeExecution(executionId, 3, errorMessage);
        }

        record.setStatus(4); // 需要重试
        record.setErrorMessage(errorMessage);
        record.setRetryCount(record.getRetryCount() + 1);
        record.setUpdateTime(new Date());
        updateById(record);
        return record;
    }

    /**
     * 从执行记录中恢复上下文
     * @param executionId 执行ID
     * @return 处理上下文
     */
    public ProcessContext restoreContext(String executionId) {
        SysProcessExecutionRecordEntity record = getByExecutionId(executionId);
        if (record == null) {
            throw new RuntimeException("执行记录不存在: " + executionId);
        }

        String contextJson = record.getExecutionContext();
        if (contextJson != null && !contextJson.isEmpty()) {
            try {
                return JSON.parseObject(contextJson, ProcessContext.class);
            } catch (Exception e) {
                log.error("恢复上下文失败: {}", e.getMessage());
            }
        }

        // 如果恢复失败，创建新的上下文
        ProcessContext context = new ProcessContext();
        context.setRequestId(record.getRequestId());
        context.setInterfaceCode(record.getInterfaceCode());
        context.setAppKey(record.getAppKey());
        return context;
    }


}