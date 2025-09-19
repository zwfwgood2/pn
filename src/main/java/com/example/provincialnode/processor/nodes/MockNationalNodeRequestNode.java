package com.example.provincialnode.processor.nodes;

import com.example.provincialnode.processor.Node;
import com.example.provincialnode.processor.context.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("mockNationalNodeRequestNode")
public class MockNationalNodeRequestNode implements Node {

    private static final String NODE_ID = "mockNationalNodeRequestNode";
    private static final String NODE_NAME = "全国节点请求MOCK节点";

    @Override
    public boolean execute(ProcessContext context) {

        log.info("执行全国节点请求MOCK节点: {}", context.getRequestId());
        return false;
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
