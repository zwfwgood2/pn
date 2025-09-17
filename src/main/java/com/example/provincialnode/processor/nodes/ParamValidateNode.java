package com.example.provincialnode.processor.nodes;

import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.processor.Node;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.provincialnode.processor.context.ProcessContext;
import com.example.provincialnode.processor.validator.ParamValidator;
import com.example.provincialnode.processor.validator.ParamValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 增强型参数验证节点
 * 支持配置化规则，使用可扩展的校验器进行参数校验
 */
@Slf4j
@Component("paramValidateNode")
public class ParamValidateNode implements Node {

    @Autowired
    private ParamValidatorFactory validatorFactory;

    private static final String NODE_ID = "paramValidateNode";
    private static final String NODE_NAME = "参数验证节点";

    @Override
    public boolean execute(ProcessContext context) {
        log.info("执行增强型参数验证节点: {}", context.getRequestId());
        
        try {
            //从节点配置中获取参数校验规则
            Map<String, Object> nodeConfig = context.getAttribute("nodeConfig");
            if (nodeConfig == null || !nodeConfig.containsKey("validateRules")) {
                log.info("未配置参数校验规则，跳过参数校验");
                return true;
            }
            
            // 解析校验规则
            String validateRulesJson = nodeConfig.get("validateRules").toString();
            JSONArray validateRules = JSON.parseArray(validateRulesJson);
            
            // 获取请求参数
            Map<String, Object> requestParams = context.getRequestParams();
            
            // 执行参数校验
            for (int i = 0; i < validateRules.size(); i++) {
                JSONObject rule = validateRules.getJSONObject(i);
                String paramName = rule.getString("paramName");
                
                // 获取字段级别的校验规则
                JSONArray fieldRules = rule.getJSONArray("rules");
                if (fieldRules == null || fieldRules.isEmpty()) {
                    continue;
                }
                
                // 获取参数值
                Object paramValue = requestParams.get(paramName);
                
                // 对每个字段执行配置的所有校验规则
                for (int j = 0; j < fieldRules.size(); j++) {
                    JSONObject fieldRule = fieldRules.getJSONObject(j);
                    String validatorType = fieldRule.getString("type");
                    Object ruleConfig = fieldRule.get("config");
                    
                    // 获取对应的校验器
                    ParamValidator validator = validatorFactory.getValidator(validatorType);
                    if (validator == null) {
                        log.warn("未找到校验器: {}", validatorType);
                        continue;
                    }
                    
                    // 执行校验
                    boolean isValid = validator.validate(paramValue, ruleConfig);
                    if (!isValid) {
                        // 校验失败，生成错误消息并标记为失败
                        String errorMessage = validator.getErrorMessage(paramName, paramValue, ruleConfig);
                        context.markFailure(ResultCode.PARAM_ERROR.getCode(), errorMessage);
                        log.error("参数校验失败: {}", errorMessage);
                        return false;
                    }
                }
            }
            
            log.info("参数校验通过: {}", context.getRequestId());
            return true;
        } catch (Exception e) {
            log.error("参数验证节点执行异常: {}", e.getMessage(), e);
            context.markFailure(ResultCode.PARAM_ERROR.getCode(), "参数验证异常: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getNodeId() {
        return NODE_ID;
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }
}