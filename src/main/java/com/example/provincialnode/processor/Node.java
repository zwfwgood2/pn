package com.example.provincialnode.processor;

import com.example.provincialnode.processor.context.ProcessContext;

/**
 * 接口处理节点基础接口
 * 定义节点执行的核心方法
 * node_config字段配置示例： 
 * {    
 *   "inParamName": "XX" // 描述节点在不同流程中的输入参数名称，不配置默认取requestParams中的数据
 *   “inParamType”: "String" // 描述节点在不同流程中输入参数类型，支持【array,map,jsonObject,string】
 *   "outParamName": "XX" // 描述节点在不同流程中的输出参数名称
 *   “outParamType”: "String" // 描述节点在不同流程中输出参数类型，支持【array,map,jsonObject,string】
 * }  此配置主要作用为描述节点在不同流程中的输入输出参数名称和类型，以便于在流程中进行参数传递和校验。  
 * */
public interface Node {

    String inParamName="inParamName";
    String outParamName="outParamName";
    String inParamType="inParamType";
    String outParamType="outParamType";

    String side = "side";

    String requestParams="requestParams";
    String responseData="responseData";
    String nodeConfig="nodeConfig";
    /**
     * 节点执行方法
     * @param context 处理上下文
     * @return 执行结果
     */
    boolean execute(ProcessContext context);

    /**
     * 获取节点ID
     * @return 节点ID
     */
    String getNodeId();

    /**
     * 获取节点名称
     * @return 节点名称
     */
    String getNodeName();

}