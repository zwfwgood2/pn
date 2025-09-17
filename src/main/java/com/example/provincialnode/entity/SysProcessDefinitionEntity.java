package com.example.provincialnode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 流程定义实体类
 * 存储接口处理流程的定义信息
 */
@Data
@TableName("sys_process_definition")
public class SysProcessDefinitionEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String processCode;

    private String processName;

    private String interfaceCode;

    private String description;

    private Integer status; // 0:禁用, 1:启用

    private Date createTime;

    private Date updateTime;
}