package com.example.provincialnode.processor.nodes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.processor.Node;
import com.example.provincialnode.processor.context.ProcessContext;
import com.example.provincialnode.processor.validator.ParamValidator;
import com.example.provincialnode.processor.validator.ParamValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 增强型参数验证节点
 * 支持多层级嵌套验证，包括map、array、iterable、json字符串、string、number等类型
 */
@Slf4j
@Component("paramValidateNode")
public class EnhancedParamValidateNode implements Node {

    @Autowired
    private ParamValidatorFactory validatorFactory;

    private static final String NODE_ID = "paramValidateNode";
    private static final String NODE_NAME = "参数验证节点";

    @Override
    public boolean execute(ProcessContext context) {
        log.info("执行参数验证节点: {}", context.getRequestId());

        try {
            //从节点配置中获取参数校验规则
            Map<String, Object> nodeConfig = context.getAttribute(Node.nodeConfig);
            if (nodeConfig == null || !nodeConfig.containsKey("validateRules")) {
                log.info("未配置参数校验规则，跳过参数校验");
                return true;
            }

            // 解析校验规则
            // 解析校验规则
            String validateRulesJson = nodeConfig.get("validateRules").toString();
            JSONArray validateRules = JSON.parseArray(validateRulesJson);

            // 获取请求参数
            Map<String, Object> requestParams = context.getAttributeByParamName(Node.inParamName);
            
            // 执行参数校验
            for (int i = 0; i < validateRules.size(); i++) {
                JSONObject rule = validateRules.getJSONObject(i);
                String paramName = rule.getString("paramName");
                // 获取字段级别的校验规则
                JSONArray fieldRules = rule.getJSONArray("rules");
                
                if (paramName == null || fieldRules == null || fieldRules.isEmpty()) {
                    continue;
                }

                // 获取参数值（支持嵌套路径）
                Object paramValue = getNestedValue(requestParams, paramName);
                
                // 验证参数类型和值
                boolean isValid = validateParam(paramValue, fieldRules, paramName, context);
                if (!isValid) {
                    return false;
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

    /**
     * 从嵌套对象中获取指定路径的值
     * @param source 源对象
     * @param path 参数路径，如 "user.info.address.city"
     * @return 参数值
     */
    private Object getNestedValue(Object source, String path) {
        if (source == null || path == null || path.isEmpty()) {
            return null;
        }

        String[] parts = path.split("\\.");
        Object current = source;

        for (String part : parts) {
            if (current == null) {
                return null;
            }

            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else if (current instanceof JSONObject) {
                current = ((JSONObject) current).get(part);
            } else {
                // 无法继续深入，返回当前值
                return current;
            }
        }

        return current;
    }

    /**
     * 验证参数
     * @param paramValue 参数值
     * @param rules 验证规则
     * @param paramName 参数名称
     * @param context 上下文
     * @return 是否验证通过
     */
    private boolean validateParam(Object paramValue, JSONArray rules, String paramName, ProcessContext context) {
        // 先进行非空验证和基础验证
        for (int i=0;i<rules.size();i++) {
            JSONObject rule = rules.getJSONObject(i);
            String validatorType = (String) rule.get("type");
            Object ruleConfig = rule.get("config");
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
        // 获取参数类型配置，如果有嵌套验证规则则进行递归验证
        for (int i=0;i<rules.size();i++) {
            JSONObject rule = rules.getJSONObject(i);
            String validatorType = (String) rule.get("type");
            if ("dataType".equals(validatorType)) {
                String dataType = rule.get("config").toString().toLowerCase();
                JSONArray nestedRules =rule.getJSONArray("nestedRules");
                if (nestedRules != null && !nestedRules.isEmpty()) {
                    // 根据类型进行不同的嵌套验证
                    if ("array".equals(dataType) || "iterable".equals(dataType)) {
                        // 数组或集合类型的嵌套验证
                        if (paramValue instanceof JSONArray) {
                            for (int j = 0; i < ((JSONArray) paramValue).size(); j++) {
                                Object item = ((JSONArray) paramValue).get(j);
                                boolean isValid = validateParam(item, nestedRules, paramName + "[" + j + "]", context);
                                if (!isValid) {
                                    return false;
                                }
                            }
                        } else if (paramValue instanceof Iterable) {
                            int index = 0;
                            for (Object item : (Iterable<?>) paramValue) {
                                boolean isValid = validateParam(item, nestedRules, paramName + "[" + index + "]", context);
                                if (!isValid) {
                                    return false;
                                }
                                index++;
                            }
                        } else if (paramValue.getClass().isArray()) {
                            int length = java.lang.reflect.Array.getLength(paramValue);
                            for (int a = 0; a < length; a++) {
                                Object item = java.lang.reflect.Array.get(paramValue, a);
                                boolean isValid = validateParam(item, nestedRules, paramName + "[" + a + "]", context);
                                if (!isValid) {
                                    return false;
                                }
                            }
                        }
                    } else if ("map".equals(dataType) || "object".equals(dataType)) {
                        // Map或对象类型的嵌套验证
                        if (paramValue instanceof Map || paramValue instanceof JSONObject) {
                            for(int r=0;r<nestedRules.size();r++) {
                                JSONObject nestRule=(JSONObject) nestedRules.get(r);
                                String nestedParamName = (String) nestRule.get("paramName");
                                JSONArray fieldRules =nestRule.getJSONArray("rules");
                                if (nestedParamName != null && fieldRules != null && !fieldRules.isEmpty()) {
                                    Object nestedValue = getNestedValue(paramValue, nestedParamName);
                                    boolean isValid = validateParam(nestedValue, fieldRules, paramName + "." + nestedParamName, context);
                                    if (!isValid) {
                                        return false;
                                    }
                                }
                            }
                        }
                    } else if ("json".equals(dataType) && paramValue instanceof String) {
                        // JSON字符串类型的嵌套验证
                        try {
                            Object jsonObj = JSON.parse((String) paramValue);
                            for (int r=0;r<nestedRules.size();r++) {
                                JSONObject nestRule=(JSONObject) nestedRules.get(r);
                                String nestedParamName = (String) nestRule.get("paramName");
                                JSONArray fieldRules =nestRule.getJSONArray("rules");
                                if (nestedParamName != null && fieldRules != null && !fieldRules.isEmpty()) {
                                    Object nestedValue = getNestedValue(jsonObj, nestedParamName);
                                    boolean isValid = validateParam(nestedValue, fieldRules, paramName + "." + nestedParamName, context);
                                    if (!isValid) {
                                        return false;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("JSON解析失败: {}", e.getMessage());
                            context.markFailure(ResultCode.PARAM_ERROR.getCode(), "参数 " + paramName + " JSON格式错误");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
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