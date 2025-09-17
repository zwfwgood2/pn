package com.example.provincialnode.processor.validator.impl;

import com.example.provincialnode.processor.validator.ParamValidator;
import org.springframework.stereotype.Component;

/**
 * 正则表达式校验器
 * 使用正则表达式验证字符串参数的格式
 */
@Component
public class RegexValidator implements ParamValidator {
    
    private static final String TYPE = "regex";
    
    @Override
    public boolean validate(Object paramValue, Object ruleConfig) {
        if (paramValue == null || ruleConfig == null || !(paramValue instanceof String)) {
            return true;
        }
        
        String strValue = (String) paramValue;
        String regex = ruleConfig.toString();
        
        try {
            return strValue.matches(regex);
        } catch (Exception e) {
            // 正则表达式格式错误，视为校验失败
            return false;
        }
    }
    
    @Override
    public String getErrorMessage(String paramName, Object paramValue, Object ruleConfig) {
        return "参数 " + paramName + " 格式不正确";
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}