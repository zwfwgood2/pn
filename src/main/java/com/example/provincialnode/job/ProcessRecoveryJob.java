package com.example.provincialnode.job;

import com.example.provincialnode.common.Result;
import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.entity.SysProcessExecutionRecordEntity;
import com.example.provincialnode.processor.ProcessEngine;
import com.example.provincialnode.service.SysProcessExecutionRecordService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 流程恢复定时任务
 * 用于定期恢复失败的流程执行
 */
@Component
@Slf4j
public class ProcessRecoveryJob {

    @Autowired
    private SysProcessExecutionRecordService processExecutionRecordService;

    @Autowired
    private ProcessEngine processEngine;

    // 线程池，用于并发执行流程恢复
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 定时恢复流程执行
     * 每5分钟执行一次
     */
    @XxlJob("processRecoveryJob")
    public ReturnT<String> processRecovery(String param) {
        log.info("开始执行流程恢复任务...");
        
        try {
            // 查询需要重试的执行记录
            List<SysProcessExecutionRecordEntity> records = processExecutionRecordService.getRetryableExecutionRecords();
            log.info("发现需要重试的流程执行记录数量: {}", records.size());
            
            // 并发处理每个需要重试的记录
            for (SysProcessExecutionRecordEntity record : records) {
                executorService.submit(() -> {
                    try {
                        log.info("开始恢复流程: {}, 执行ID: {}", 
                                record.getProcessCode(), record.getExecutionId());
                        
                        // 使用ProcessEngine重放流程
                        Result<?> result = processEngine.replayProcess(record.getExecutionId());
                        
                        if (result.getCode() != null && result.getCode().equals(ResultCode.SUCCESS.getCode())) {
                            log.info("流程恢复成功: {}, 执行ID: {}", 
                                    record.getProcessCode(), record.getExecutionId());
                        } else {
                            log.error("流程恢复失败: {}, 执行ID: {}, 错误信息: {}", 
                                    record.getProcessCode(), record.getExecutionId(), result.getMessage());
                            // 标记为需要重试，让下一次定时任务继续尝试
                            processExecutionRecordService.markForRetry(
                                    record.getExecutionId(), result.getMessage());
                        }
                    } catch (Exception e) {
                        log.error("流程恢复异常: {}, 执行ID: {}", 
                                record.getProcessCode(), record.getExecutionId(), e);
                        try {
                            // 标记为需要重试，让下一次定时任务继续尝试
                            processExecutionRecordService.markForRetry(
                                    record.getExecutionId(), e.getMessage());
                        } catch (Exception ex) {
                            log.error("更新执行记录失败: {}", ex.getMessage());
                        }
                    }
                });
            }
            
            log.info("流程恢复任务提交完成");
            return ReturnT.SUCCESS;
        } catch (Exception e) {
            log.error("流程恢复任务执行异常: {}", e.getMessage(), e);
            return new ReturnT<>(ReturnT.FAIL_CODE, e.getMessage());
        }
    }

}