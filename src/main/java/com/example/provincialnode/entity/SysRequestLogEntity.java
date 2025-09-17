package com.example.provincialnode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 请求日志实体类
 * 记录每次接口调用的详细信息
 */
@Data
@TableName("sys_request_log")
public class SysRequestLogEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String requestId;

    private String interfaceCode;

    private String appKey;

    private Date requestTime;

    private Date responseTime;

    private String requestParams;

    private String responseResult;

    private Integer status; // 0:失败, 1:成功

    private String errorMessage;

    private String requestIp;

    private Long processingTime; // 处理耗时，毫秒
}