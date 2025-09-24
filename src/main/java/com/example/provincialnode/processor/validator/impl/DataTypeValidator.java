package com.example.provincialnode.processor.validator.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.provincialnode.processor.validator.ParamValidator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 数据类型校验器
 * 验证参数的数据类型是否符合要求
 */
@Component
public class DataTypeValidator implements ParamValidator {
    
    private static final String TYPE = "dataType";
    
    @Override
    public boolean validate(Object paramValue, Object ruleConfig) {
        if (paramValue == null || ruleConfig == null) {
            return true;
        }
        
        String dataType = ruleConfig.toString().toLowerCase();
        
        switch (dataType) {
            case "string":
                return paramValue instanceof String;
            case "json": //TODO 校验是否符合json字符串格式
                return true;
            case "integer":
            case "int":
                return paramValue instanceof Integer;
            case "long":
                return paramValue instanceof Long;
            case "double":
                return paramValue instanceof Double;
            case "float":
                return paramValue instanceof Float;
            case "boolean":
                return paramValue instanceof Boolean;
            case "iterable":
            case "array":
                return paramValue instanceof JSONArray || paramValue.getClass().isArray() || paramValue instanceof Iterable;
            case "map":
            case "object":
                return paramValue instanceof JSONObject || paramValue instanceof Map;
            default:
                return true;
        }
    }
    
    @Override
    public String getErrorMessage(String paramName, Object paramValue, Object ruleConfig) {
        return "参数 " + paramName + " 数据类型错误，应为 " + ruleConfig;
    }
    
    @Override
    public String getType() {
        return TYPE;
    }
}