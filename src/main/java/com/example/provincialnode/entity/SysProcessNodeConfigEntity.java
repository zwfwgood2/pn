package com.example.provincialnode.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 流程节点配置实体类
 * 存储流程中每个节点的配置信息
 */
@Data
@TableName("sys_process_node_config")
public class SysProcessNodeConfigEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String processCode;

    private String nodeId;

    private String nodeName;

    private Integer nodeOrder;

    private String retryConfig; // 重试配置，JSON格式

    private Boolean asyncExecution = false; // 是否异步执行

    private String nodeConfig; // 节点配置参数，JSON格式

    private Integer status; // 0:禁用, 1:启用
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;
}