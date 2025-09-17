package com.example.provincialnode.processor.nodes;

import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.processor.Node;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.provincialnode.processor.context.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 字段名称转换节点
 * 将输入数据中的字段名称根据配置规则转换为目标字段名称
 * 支持普通对象、数组和嵌套对象的字段转换
 */
@Slf4j
@Component("fieldNameConvertNode")
public class FieldNameConvertNode implements Node {

    private static final String NODE_ID = "fieldNameConvertNode";
    private static final String NODE_NAME = "字段名称转换节点";

    @Override
    public boolean execute(ProcessContext context) {
        log.info("执行字段名称转换节点: {}", context.getRequestId());
        
        try {
            // 从节点配置中获取转换规则
            Map<String, Object> nodeConfig = context.getAttribute("nodeConfig");
            if (nodeConfig == null || !nodeConfig.containsKey("convertRules")) {
                log.info("未配置字段转换规则，跳过字段转换");
                return true;
            }
            
            // 解析转换规则
            String convertRulesJson = nodeConfig.get("convertRules").toString();
            JSONObject convertRules = JSON.parseObject(convertRulesJson);
            
            // 获取要转换的数据
            // 默认从requestParams中获取数据，可以通过配置指定数据源
            String dataSource = nodeConfig.containsKey("dataSource") ? nodeConfig.get("dataSource").toString() : "requestParams";
            Object sourceData = null;
            
            if ("requestParams".equals(dataSource)) {
                sourceData = context.getRequestParams();
            } else if ("responseData".equals(dataSource)) {
                sourceData = context.getAttribute("responseData");
            } else {
                sourceData = context.getAttribute(dataSource);
            }
            
            if (sourceData == null) {
                log.info("数据源 {} 中没有数据，跳过字段转换", dataSource);
                return true;
            }
            
            // 执行字段转换
            Object convertedData = convertFields(sourceData, convertRules);
            
            // 将转换后的数据放回指定的目标位置
            String targetDataKey = nodeConfig.containsKey("targetDataKey") ? 
                    nodeConfig.get("targetDataKey").toString() : 
                    ("requestParams".equals(dataSource) ? "requestParams" : "responseData");
            
            if ("requestParams".equals(targetDataKey)) {
                context.setRequestParams((Map<String, Object>) convertedData);
            } else if ("responseData".equals(targetDataKey)) {
                context.setAttribute("responseData", convertedData);
            } else {
                context.setAttribute(targetDataKey, convertedData);
            }
            
            log.info("字段名称转换完成: {}", context.getRequestId());
            return true;
        } catch (Exception e) {
            log.error("字段名称转换节点执行异常: {}", e.getMessage(), e);
            context.markFailure(ResultCode.SYSTEM_ERROR.getCode(), "字段名称转换异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 根据转换规则递归转换字段名称
     * 
     * @param sourceData 源数据
     * @param convertRules 转换规则
     * @return 转换后的数据
     */
    private Object convertFields(Object sourceData, JSONObject convertRules) {
        if (sourceData == null) {
            return null;
        }
        
        // 处理JSON对象
        if (sourceData instanceof JSONObject) {
            JSONObject sourceObj = (JSONObject) sourceData;
            JSONObject targetObj = new JSONObject();
            
            for (String key : sourceObj.keySet()) {
                Object value = sourceObj.get(key);
                
                // 检查是否有该字段的转换规则
                String targetKey = convertRules.containsKey(key) ? 
                        convertRules.getString(key) : key;
                
                // 递归处理嵌套对象或数组
                if (value instanceof JSONObject || value instanceof JSONArray || 
                        value instanceof Map || value.getClass().isArray() || value instanceof Iterable) {
                    targetObj.put(targetKey, convertFields(value, convertRules));
                } else {
                    targetObj.put(targetKey, value);
                }
            }
            
            return targetObj;
        }
        
        // 处理Map
        if (sourceData instanceof Map) {
            Map<String, Object> sourceMap = (Map<String, Object>) sourceData;
            Map<String, Object> targetMap = new HashMap<>();
            
            for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // 检查是否有该字段的转换规则
                String targetKey = convertRules.containsKey(key) ? 
                        convertRules.getString(key) : key;
                
                // 递归处理嵌套对象或数组
                if (value instanceof JSONObject || value instanceof JSONArray || 
                        value instanceof Map || value.getClass().isArray() || value instanceof Iterable) {
                    targetMap.put(targetKey, convertFields(value, convertRules));
                } else {
                    targetMap.put(targetKey, value);
                }
            }
            
            return targetMap;
        }
        
        // 处理JSON数组
        if (sourceData instanceof JSONArray) {
            JSONArray sourceArray = (JSONArray) sourceData;
            JSONArray targetArray = new JSONArray();
            
            for (int i = 0; i < sourceArray.size(); i++) {
                Object item = sourceArray.get(i);
                // 递归处理数组中的每个元素
                targetArray.add(convertFields(item, convertRules));
            }
            
            return targetArray;
        }
        
        // 处理Java数组
        if (sourceData.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(sourceData);
            JSONArray targetArray = new JSONArray();
            
            for (int i = 0; i < length; i++) {
                Object item = java.lang.reflect.Array.get(sourceData, i);
                // 递归处理数组中的每个元素
                targetArray.add(convertFields(item, convertRules));
            }
            
            return targetArray;
        }
        
        // 处理Iterable集合
        if (sourceData instanceof Iterable) {
            JSONArray targetArray = new JSONArray();
            
            for (Object item : (Iterable<?>) sourceData) {
                // 递归处理集合中的每个元素
                targetArray.add(convertFields(item, convertRules));
            }
            
            return targetArray;
        }
        
        // 基本数据类型或不支持的数据类型，直接返回
        return sourceData;
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