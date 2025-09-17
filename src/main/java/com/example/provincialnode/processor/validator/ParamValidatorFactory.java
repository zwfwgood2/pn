package com.example.provincialnode.processor.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 参数校验器工厂
 * 管理所有的参数校验器实现，提供校验器的注册和获取功能
 */
@Component
public class ParamValidatorFactory {
    
    private final Map<String, ParamValidator> validatorMap = new ConcurrentHashMap<>();
    
    /**
     * 构造函数，自动注入所有的ParamValidator实现
     * 
     * @param validators 参数校验器列表
     */
    @Autowired
    public ParamValidatorFactory(List<ParamValidator> validators) {
        for (ParamValidator validator : validators) {
            validatorMap.put(validator.getType(), validator);
        }
    }
    
    /**
     * 根据类型获取对应的校验器
     * 
     * @param type 校验器类型
     * @return 参数校验器
     */
    public ParamValidator getValidator(String type) {
        return validatorMap.get(type);
    }
    
    /**
     * 注册新的校验器
     * 
     * @param validator 参数校验器
     */
    public void registerValidator(ParamValidator validator) {
        validatorMap.put(validator.getType(), validator);
    }
}