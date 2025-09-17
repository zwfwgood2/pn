package com.example.provincialnode.processor;

import com.example.provincialnode.processor.context.ProcessContext;

/**
 * 接口处理节点基础接口
 * 定义节点执行的核心方法
 */
public interface Node {

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