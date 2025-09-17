package com.example.provincialnode.processor.nodes;

import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.processor.Node;
import com.example.provincialnode.processor.context.ProcessContext;
import com.example.provincialnode.service.BusinessException;
import com.example.provincialnode.service.SysAuthorizationLetterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 授权书校验节点
 * 根据授权标识符查找并校验授权记录
 */
@Slf4j
@Component("authorizationLetterValidateNode")
public class AuthorizationLetterValidateNode implements Node {

    @Autowired
    private SysAuthorizationLetterService authorizationLetterService;
    private static final String NODE_ID = "authorizationLetterValidateNode";
    /**
     * 节点名称
     */
    private static final String NODE_NAME = "授权书校验节点";

    @Override
    public boolean execute(ProcessContext context) {
        log.info("开始执行授权书校验节点，请求ID: {}", context.getRequestId());

        try {
            // 1. 获取请求参数中的授权标识符
            Map<String, Object> requestParams = context.getRequestParams();
            String authIdentifier = (String) requestParams.get("authIdentifier");

            // 2. 校验授权标识符是否存在
            if (authIdentifier == null || authIdentifier.isEmpty()) {
                log.error("授权标识符不能为空，请求ID: {}", context.getRequestId());
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "授权标识符不能为空");
            }

            log.info("授权标识符: {}，开始校验", authIdentifier);

            // 3. 调用服务层进行授权书校验
            boolean isValid = authorizationLetterService.validateAuthorizationLetter(authIdentifier);

            // 4. 处理校验结果
            if (!isValid) {
                log.error("授权书校验失败，授权标识符: {},请求ID: {}", context.getRequestId());
                throw new BusinessException(ResultCode.AUTHORIZATION_ERROR.getCode(), "授权书校验失败");
            }

            // 5. 校验通过，将授权信息存入上下文
            context.setAttribute("authorization", "passed");
            context.setAttribute("authIdentifier", authIdentifier);

            log.info("授权书校验通过，授权标识符: {}, 请求ID: {}", context.getRequestId());
            return true;

        } catch (BusinessException e) {
            log.error("授权书校验节点业务异常: {}", e.getMessage());
            context.markFailure(ResultCode.SYSTEM_ERROR.getCode(), "授权书校验失败");
            return false;
        } catch (Exception e) {
            log.error("授权书校验节点执行异常: {}", e.getMessage(), e);
            context.markFailure(ResultCode.SYSTEM_ERROR.getCode(), "授权书校验失败");
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