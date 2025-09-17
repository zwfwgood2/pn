package com.example.provincialnode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 授权书实体类
 * 存储授权书相关信息 TODO 需根据上传授权书接口增加调整相应字段
 */
@Data
@TableName("sys_authorization_letter")
public class SysAuthorizationLetterEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 授权书标识字符串
     */
    private String authIdentifier;

    /**
     * 授权书名称
     */
    private String letterName;

    /**
     * 授权机构ID
     */
    private Long authOrgId;

    /**
     * 授权机构名称
     */
    private String authOrgName;

    /**
     * 授权开始时间
     */
    private Date startTime;

    /**
     * 授权结束时间
     */
    private Date endTime;

    /**
     * 状态 0:无效, 1:有效
     */
    private Integer status = 1;

    /**
     * 描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}