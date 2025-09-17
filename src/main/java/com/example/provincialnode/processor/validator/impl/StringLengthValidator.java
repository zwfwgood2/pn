package com.example.provincialnode.processor.validator.impl;

import com.example.provincialnode.processor.validator.ParamValidator;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 字符串长度校验器
 * 验证字符串参数的长度是否在指定范围内
 */
@Component
public class StringLengthValidator implements ParamValidator {
    
    private static final String TYPE = "stringLength";
    
    @Override
    public boolean validate(Object paramValue, Object ruleConfig) {
        if (paramValue == null || ruleConfig == null || !(paramValue instanceof String)) {
            return true;
        }
        
        String strValue = (String) paramValue;
        
        if (ruleConfig instanceof Map) {
            Map<String, Object> config = (Map<String, Object>) ruleConfig;
            
            // 检查最小长度
            if (config.containsKey("minLength")) {
                try {
                    int minLength = Integer.parseInt(config.get("minLength").toString());
                    if (strValue.length() < minLength) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    // 配置格式错误，视为校验通过
                }
            }
            
            // 检查最大长度
            if (config.containsKey("maxLength")) {
                try {
                    int maxLength = Integer.parseInt(config.get("maxLength").toString());
                    if (strValue.length() > maxLength) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    // 配置格式错误，视为校验通过
                }
            }
        }
        
        return true;
    }
    
    @Override
    public String getErrorMessage(String paramName, Object paramValue, Object ruleConfig) {
        if (paramValue instanceof String && ruleConfig instanceof Map) {
            String strValue = (String) paramValue;
            Map<String, Object> config = (Map<String, Object>) ruleConfig;
            
            if (config.containsKey("minLength")) {
                try {
                    int minLength = Integer.parseInt(config.get("minLength").toString());
                    if (strValue.length() < minLength) {
                        return "参数 " + paramName + " 长度不能小于 " + minLength;
                    }
                } catch (NumberFormatException e) {
                    // 配置格式错误，使用默认错误消息
                }
            }
            
            if (config.containsKey("maxLength")) {
                try {
                    int maxLength = Integer.parseInt(config.get("maxLength").toString());
                    if (strValue.length() > maxLength) {
                        return "参数 " + paramName + " 长度不能大于 " + maxLength;
                    }
                } catch (NumberFormatException e) {
                    // 配置格式错误，使用默认错误消息
                }
            }
        }
        
        return "参数 " + paramName + " 长度不符合要求";
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}