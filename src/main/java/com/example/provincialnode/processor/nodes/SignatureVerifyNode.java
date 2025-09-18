package com.example.provincialnode.processor.nodes;

import cn.hutool.core.util.StrUtil;
import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.common.SignUtil;
import com.example.provincialnode.config.NationalNodeConfig;
import com.example.provincialnode.entity.SysAccessOrganizationEntity;
import com.example.provincialnode.processor.Node;
import com.example.provincialnode.processor.context.ProcessContext;
import com.example.provincialnode.service.SysAccessOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 验签节点
 * 负责验证请求数据的签名是否有效
 */
@Slf4j
@Component("signatureVerifyNode")
public class SignatureVerifyNode implements Node {

    @Autowired
    private SysAccessOrganizationService sysAccessOrganizationService;
    @Autowired
    private NationalNodeConfig mationalNodeConfig;
    private static final String NODE_ID = "signatureVerifyNode";
    private static final String NODE_NAME = "验签节点";

    @Override
    public boolean execute(ProcessContext context) {
        log.info("执行验签节点: {}", context.getRequestId());
        
        try {
            // 1. 获取请求参数和AppKey
            Map<String, Object> requestParams = context.getAttribute(Node.inParamName);
            String appKey = context.getAppKey();
            // 2. 从请求参数中获取签名
            String signature = (String) requestParams.get("signatureData");
            if (signature == null || signature.isEmpty()) {
                context.markFailure(ResultCode.SIGNATURE_ERROR.getCode(), "签名不能为空");
                log.error("签名不能为空");
                return false;
            }
            
            // 3. 获取机构的公钥
            SysAccessOrganizationEntity org = sysAccessOrganizationService.selectByAppKey(appKey);
            if (org == null) {
                throw new RuntimeException("未找到对应的机构信息: " + appKey);
            }

            Map<String, Object> nodeConfig = context.getAttribute("nodeConfig");
            String side =nodeConfig.get(Node.side).toString();
            //默认为市级端点
            String verifyPublicKey =requestParams.get("publicKey").toString();
            String decryptPrivateKey=org.getPrivateKey();
            //根据端点获取解密私钥和验签公钥
            //全国端点
            if(side.equals("national")){
                SysAccessOrganizationEntity self = sysAccessOrganizationService.selectByAppKey("self");
                verifyPublicKey=mationalNodeConfig.getPublicKey();
                decryptPrivateKey=self.getPrivateKey();
            }
            // 5. 验证签名
            String requestData = SignUtil.verifySignature(requestParams,verifyPublicKey,decryptPrivateKey);
            if (StrUtil.isEmpty(requestData)) {
                context.markFailure(ResultCode.SIGNATURE_ERROR.getCode(), "签名验证失败");
                log.error("签名验证失败: appKey={}", appKey);
                return false;
            }
            Map<String,Object> verifyResult=new HashMap<>();
            verifyResult.putAll(requestParams);
            verifyResult.put("requestData",requestData);

            //将验签结果放入上下文
            context.setAttribute(Node.outParamName,verifyResult);
            log.info("签名验证成功: {}", context.getRequestId());
            return true;
        } catch (Exception e) {
            log.error("验签节点执行异常: {}", e.getMessage(), e);
            context.markFailure(ResultCode.SIGNATURE_ERROR.getCode(), "验签异常");
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