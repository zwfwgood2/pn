package com.example.provincialnode.processor.nodes;

import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.common.SignUtil;
import com.example.provincialnode.config.NationalNodeConfig;
import com.example.provincialnode.entity.SysAccessOrganizationEntity;
import com.example.provincialnode.exception.BusinessException;
import com.example.provincialnode.processor.Node;
import com.example.provincialnode.processor.context.ProcessContext;
import com.example.provincialnode.service.SysAccessOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据签名节点
 * 负责使用市级节点公钥重新签名数据
 * node_config字段配置示例：
 * {    
 *   "side": "national" // 可选值：national（全国节点）、provincial（省级节点）、city（市级节点）
 * } 此配置项用于指定签名节点的运行环境以便于选择不 同的公私钥，默认值为city（市级节点）。  
 */
@Slf4j
@Component("signatureNode")
public class SignatureNode implements Node {

    @Autowired
    private SysAccessOrganizationService sysAccessOrganizationService;
    @Autowired
    private NationalNodeConfig mationalNodeConfig;
    private static final String NODE_ID = "signatureNode";
    private static final String NODE_NAME = "签名节点";

    @Override
    public boolean execute(ProcessContext context) {
        log.info("执行签名节点: {}", context.getRequestId());
        
        try {
            // 1. 获取AppKey
            String appKey = context.getAppKey();
            // 2. 获取待签名数据（从上下文中获取）
            Map<String, Object> requestParams = context.getAttribute(Node.inParamName);
            if (requestParams == null || requestParams.isEmpty()) {
                context.markFailure(ResultCode.DATA_NOT_FOUND.getCode(), "待签名数据为空");
                return false;
            }
            // 3. 获取机构信息
            SysAccessOrganizationEntity org = sysAccessOrganizationService.selectByAppKey(appKey);
            if (org == null) {
                throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "未找到对应的机构信息: " + appKey);
            }

            String verifyPrivateKey =org.getPrivateKey();
            String encryptPublicKey=requestParams.get("publicKey").toString();
            //根据端点获取解密私钥和验签公钥
            if(side.equals("national")){
                SysAccessOrganizationEntity self = sysAccessOrganizationService.selectByAppKey("self");
                verifyPrivateKey=self.getPrivateKey();
                encryptPublicKey=mationalNodeConfig.getPublicKey();
            }
            // 5. 对数据进行签名
            Map<String,String> signature = SignUtil.signData(requestParams.get("requestData").toString(), encryptPublicKey,verifyPrivateKey);
            Map<String,Object> responseParams = new HashMap<>(6);
            responseParams.putAll(requestParams);
            responseParams.putAll(signature);
            // 6. 将签名结果保存到上下文中
            context.setAttribute(Node.outParamName,responseParams);
            
            log.info("数据签名成功: {}", context.getRequestId());
            return true;
        } catch (Exception e) {
            log.error("签名节点执行异常: {}", e.getMessage(), e);
            context.markFailure(ResultCode.SYSTEM_ERROR.getCode(), "数据签名异常");
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