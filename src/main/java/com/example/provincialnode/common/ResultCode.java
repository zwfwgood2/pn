package com.example.provincialnode.common;

import lombok.Getter;

import javax.management.loading.MLetContent;

/**
 * 结果码枚举类
 * 定义系统中所有的响应码和对应的消息
 */
@Getter
public enum ResultCode {

    // 成功状态码
    SUCCESS("0000", "成功"),
    
    // 系统错误
    SYSTEM_ERROR("1001", "系统内部错误"),
    SERVICE_UNAVAILABLE("1002", "服务暂不可用"),
    NETWORK_ERROR("1003", "网络异常"),
    NOT_FOUND("1004","接口定义不存在"),
    
    // 认证授权错误
    UNAUTHORIZED("2001", "未授权访问"),
    INVALID_TOKEN("2002", "无效的令牌"),
    EXPIRED_TOKEN("2003", "令牌已过期"),
    INVALID_APP_KEY("2004", "无效的AppKey"),
    SIGNATURE_ERROR("2005", "签名验证失败"),
    
    // 参数错误
    PARAM_ERROR("3001", "参数错误"),
    PARAM_MISSING("3002", "缺少必要参数"),
    INVALID_PARAM_FORMAT("3003", "参数格式无效"),
    AUTHORIZATION_ERROR("3004", "授权书校验失败"),
    
    // 业务错误
    BUSINESS_ERROR("4001", "业务处理失败"),
    DATA_NOT_FOUND("4002", "未找到数据"),
    DUPLICATE_DATA("4003", "数据重复"),
    OPERATION_FAILED("4004", "操作失败"),
    
    // 全国节点错误
    NATIONAL_NODE_ERROR("5001", "全国节点服务异常"),
    NATIONAL_NODE_TIMEOUT("5002", "全国节点服务超时"),
    
    // 其他错误
    UNKNOWN_ERROR("9999", "未知错误");

    private final String code;
    private final String message;

    ResultCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

}