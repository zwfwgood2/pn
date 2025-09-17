package com.example.provincialnode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 流程执行记录实体类
 * 存储流程执行的详细信息，支持流程重放
 */
@Data
@TableName("sys_process_execution_record")
public class SysProcessExecutionRecordEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String executionId;

    private String requestId;

    private String processCode;

    private String interfaceCode;

    private String appKey;

    private Date startTime;
    private Date endTime;

    private Integer status; // 0:初始, 1:执行中, 2:成功, 3:失败, 4:需要重试

    private String errorMessage;

    private Integer retryCount = 0;

    private Integer maxRetryCount = 3;

    private String currentNodeId;

    private String executionContext; // 执行上下文，JSON格式，用于重放

    private Date updateTime;

}