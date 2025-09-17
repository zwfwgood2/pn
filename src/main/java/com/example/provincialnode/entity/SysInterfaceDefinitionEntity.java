package com.example.provincialnode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 接口定义实体类
 * 存储系统中所有接口的定义信息
 */
@Data
@TableName("sys_interface_definition")
public class SysInterfaceDefinitionEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String interfaceCode;

    private String interfaceName;

    private Integer interfaceType; // 0:查询接口, 1:写入接口

    private String requestMethod; // GET, POST, PUT, DELETE等

    private String requestPath;

    private String processCode; // 关联的流程定义代码

    private Integer status; // 0:禁用, 1:启用

    private Date createTime;

    private Date updateTime;

    private String description;

}