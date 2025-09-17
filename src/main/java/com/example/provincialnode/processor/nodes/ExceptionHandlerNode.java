package com.example.provincialnode.processor.nodes;

import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.processor.Node;
import com.example.provincialnode.processor.context.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 异常处理节点
 * 负责处理流程执行过程中出现的异常情况
 */
@Slf4j
@Component("exceptionHandlerNode")
public class ExceptionHandlerNode implements Node {

    private static final String NODE_ID = "exceptionHandlerNode";
    private static final String NODE_NAME = "异常处理节点";

    @Override
    public boolean execute(ProcessContext context) {
        log.info("执行异常处理节点: {}", context.getRequestId());
        
        try {
            // 1. 检查是否有异常发生
            if (!context.isSuccess()) {
                String errorCode = context.getErrorCode();
                String errorMessage = context.getErrorMessage();
                
                log.error("处理异常: 错误码={}, 错误信息={}", errorCode, errorMessage);
                
                // 2. 根据不同类型的异常执行不同的处理逻辑
                handleException(context, errorCode, errorMessage);
                
                // 3. 可以根据实际情况决定是否继续执行后续节点
                // 这里返回false表示流程终止
                return false;
            }
            
            // 没有异常，继续执行
            return true;
        } catch (Exception e) {
            log.error("异常处理节点执行异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 根据不同类型的异常执行不同的处理逻辑
     * 
     * @param context 处理上下文
     * @param errorCode 错误码
     * @param errorMessage 错误消息
     */
    private void handleException(ProcessContext context, String errorCode, String errorMessage) {
        // 1. 根据错误码分类处理
        if (ResultCode.UNAUTHORIZED.getCode().equals(errorCode) ||
            ResultCode.INVALID_TOKEN.getCode().equals(errorCode) ||
            ResultCode.EXPIRED_TOKEN.getCode().equals(errorCode) ||
            ResultCode.INVALID_APP_KEY.getCode().equals(errorCode) ||
            ResultCode.SIGNATURE_ERROR.getCode().equals(errorCode)) {
            // 认证授权相关异常
            handleAuthException(context, errorCode, errorMessage);
        } else if (ResultCode.PARAM_ERROR.getCode().equals(errorCode) ||
                   ResultCode.PARAM_MISSING.getCode().equals(errorCode) ||
                   ResultCode.INVALID_PARAM_FORMAT.getCode().equals(errorCode)) {
            // 参数相关异常
            handleParamException(context, errorCode, errorMessage);
        } else if (ResultCode.NATIONAL_NODE_ERROR.getCode().equals(errorCode) ||
                   ResultCode.NATIONAL_NODE_TIMEOUT.getCode().equals(errorCode)) {
            // 全国节点相关异常
            handleNationalNodeException(context, errorCode, errorMessage);
        } else if (ResultCode.SYSTEM_ERROR.getCode().equals(errorCode) ||
                   ResultCode.SERVICE_UNAVAILABLE.getCode().equals(errorCode) ||
                   ResultCode.NETWORK_ERROR.getCode().equals(errorCode)) {
            // 系统相关异常
            handleSystemException(context, errorCode, errorMessage);
        } else {
            // 其他异常
            handleOtherException(context, errorCode, errorMessage);
        }
    }

    /**
     * 处理认证授权相关异常
     */
    private void handleAuthException(ProcessContext context, String errorCode, String errorMessage) {
        log.warn("认证授权异常处理: {}", errorMessage);
        // 可以记录审计日志、通知管理员等
    }

    /**
     * 处理参数相关异常
     */
    private void handleParamException(ProcessContext context, String errorCode, String errorMessage) {
        log.warn("参数异常处理: {}", errorMessage);
        // 可以记录详细的参数信息，方便排查问题
    }

    /**
     * 处理全国节点相关异常
     */
    private void handleNationalNodeException(ProcessContext context, String errorCode, String errorMessage) {
        log.error("全国节点异常处理: {}", errorMessage);
        // 可以触发告警、记录详细日志等
    }

    /**
     * 处理系统相关异常
     */
    private void handleSystemException(ProcessContext context, String errorCode, String errorMessage) {
        log.error("系统异常处理: {}", errorMessage);
        // 可以触发告警、通知管理员、记录错误日志等
    }

    /**
     * 处理其他类型的异常
     */
    private void handleOtherException(ProcessContext context, String errorCode, String errorMessage) {
        log.error("其他异常处理: {}", errorMessage);
        // 通用异常处理逻辑
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