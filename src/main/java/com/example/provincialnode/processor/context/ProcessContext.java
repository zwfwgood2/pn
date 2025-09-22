package com.example.provincialnode.processor.context;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 处理上下文类
 * 在接口处理流程中传递和存储中间状态、数据和结果
 */
@Data
public class ProcessContext {

    // 请求ID
    private String requestId;

    // 接口代码
    private String interfaceCode;

    private String interfacePath;
    // AppKey
    private String appKey;

    // 请求参数
    private Map<String, Object> requestParams = new HashMap<>();

    // 响应结果
    private Object responseData;

    // 处理状态
    private boolean success = true;

    // 错误信息
    private String errorMessage;

    // 错误码
    private String errorCode;

    // 中间数据存储
    private Map<String, Object> attributes = new HashMap<>();

    /**
     * 设置属性
     * @param key 属性键
     * @param value 属性值
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * 获取属性
     * @param key 属性键
     * @param <T> 属性类型
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * 获取属性
     * @param paramName 参数名称，作为属性键
     * @param <T> 属性类型
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttributeByParamName(String paramName) {
        //获取参数名称
        String key = getAttribute(paramName);
        return (T) attributes.get(key);
    }

    /**
     * 设置属性
     * @param paramName 参数名称，作为属性键
     */
    public void setAttributeByParamName(String paramName,Object value) {
        //获取参数名称
        String key = getAttribute(paramName);
        attributes.put(key,value);
    }

    /**
     * 删除属性
     * @param paramName 参数名称，作为属性键
     */
    public void removeAttributeByParamName(String paramName) {
        //获取参数名称
        String key = getAttribute(paramName);
        attributes.remove(key);
    }

    /**
     * 移除属性
     * @param key 属性键
     */
    public void removeAttribute(String key) {
        attributes.remove(key);
    }

    /**
     * 判断是否包含指定属性
     * @param key 属性键
     * @return 是否包含
     */
    public boolean containsAttribute(String key) {
        return attributes.containsKey(key);
    }

    /**
     * 标记处理失败
     * @param errorCode 错误码
     * @param errorMessage 错误消息
     */
    public void markFailure(String errorCode, String errorMessage) {
        this.success = false;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

}