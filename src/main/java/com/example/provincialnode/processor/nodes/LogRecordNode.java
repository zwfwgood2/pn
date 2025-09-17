package com.example.provincialnode.processor.nodes;

import com.example.provincialnode.entity.SysRequestLogEntity;
import com.example.provincialnode.processor.Node;
import com.alibaba.fastjson.JSON;
import com.example.provincialnode.processor.context.ProcessContext;
import com.example.provincialnode.service.SysRequestLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

/**
 * 请求日志记录节点
 * 负责记录每次接口请求的详细信息
 */
@Slf4j
@Component("logRecordNode")
public class LogRecordNode implements Node {

    @Autowired
    private SysRequestLogService sysRequestLogService;

    private static final String NODE_ID = "logRecordNode";
    private static final String NODE_NAME = "日志记录节点";

    @Override
    public boolean execute(ProcessContext context) {
        log.info("执行日志记录节点: {}", context.getRequestId());
        
        try {
            // 1. 创建请求日志对象
            SysRequestLogEntity requestLog = new SysRequestLogEntity();
            requestLog.setRequestId(context.getRequestId() != null ? context.getRequestId() : UUID.randomUUID().toString());
            requestLog.setInterfaceCode(context.getInterfaceCode());
            requestLog.setAppKey(context.getAppKey());
            requestLog.setRequestTime(new Date());
            requestLog.setRequestParams(JSON.toJSONString(context.getRequestParams()));
            requestLog.setStatus(1); // 默认成功
            
            // 2. 保存请求日志
            sysRequestLogService.save(requestLog);
            
            // 3. 将日志ID保存到上下文中，方便后续更新
            context.setAttribute("requestLogId", requestLog.getId());
            
            log.info("请求日志记录成功: {}", requestLog.getRequestId());
            return true;
        } catch (Exception e) {
            log.error("日志记录节点执行异常: {}", e.getMessage(), e);
            // 日志记录失败不应影响主流程，返回true继续执行
            return true;
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