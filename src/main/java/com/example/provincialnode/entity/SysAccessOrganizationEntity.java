package com.example.provincialnode.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 接入机构实体类
 */

@Data
@TableName("sys_access_organization")
public class SysAccessOrganizationEntity{

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 机构名称
     */
    private String orgName;

    /**
     * 机构代码
     */
    private String orgCode;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * AppKey
     */
    private String appKey;

    /**
     * 公钥
     */
    private String publicKey;

    /**
     * 私钥
     */
    private String privateKey;

    /**
     * 状态 0:禁用, 1:启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private Date updateTime;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 是否管理员
     */
    private Boolean isAdmin = false;

    /**
     * 结构类型 0:省级节点, 1:市级节点, 2:其他
     */
    private Integer structureType;

    /**
     * 联系人
     */
    private String contactPerson;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 描述
     */
    private String description;

}