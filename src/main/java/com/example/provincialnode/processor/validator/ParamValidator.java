package com.example.provincialnode.processor.validator;

/**
 * 参数校验器接口
 * 定义参数校验的基本方法，支持自定义校验规则的扩展
 */
public interface ParamValidator {
    
    /**
     * 校验参数值
     * 
     * @param paramValue 要校验的参数值
     * @param ruleConfig 校验规则配置
     * @return 校验结果，true表示校验通过，false表示校验失败
     */
    boolean validate(Object paramValue, Object ruleConfig);
    
    /**
     * 获取校验失败时的错误消息
     * 
     * @param paramName 参数名称
     * @param paramValue 参数值
     * @param ruleConfig 校验规则配置
     * @return 错误消息
     */
    String getErrorMessage(String paramName, Object paramValue, Object ruleConfig);
    
    /**
     * 获取校验器类型标识
     * 
     * @return 校验器类型
     */
    String getType();
}