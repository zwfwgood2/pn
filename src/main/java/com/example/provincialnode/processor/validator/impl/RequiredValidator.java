package com.example.provincialnode.processor.validator.impl;

import com.example.provincialnode.processor.validator.ParamValidator;
import org.springframework.stereotype.Component;

/**
 * 非空校验器
 * 验证参数是否为空
 */
@Component
public class RequiredValidator implements ParamValidator {
    
    private static final String TYPE = "required";
    
    @Override
    public boolean validate(Object paramValue, Object ruleConfig) {
        if (ruleConfig instanceof Boolean && !((Boolean) ruleConfig)) {
            // 如果配置为非必填，则校验通过
            return true;
        }
        
        if (paramValue == null) {
            return false;
        }
        
        if (paramValue instanceof String) {
            return !((String) paramValue).trim().isEmpty();
        }
        
        if (paramValue instanceof Iterable) {
            return ((Iterable<?>) paramValue).iterator().hasNext();
        }
        
        if (paramValue.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(paramValue) > 0;
        }
        
        return true;
    }
    
    @Override
    public String getErrorMessage(String paramName, Object paramValue, Object ruleConfig) {
        return "参数 " + paramName + " 不能为空";
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}