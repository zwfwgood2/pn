package com.example.provincialnode.processor.nodes;

import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.entity.SysAccessOrganizationEntity;
import com.example.provincialnode.processor.Node;
import com.example.provincialnode.processor.context.ProcessContext;
import com.example.provincialnode.service.SysAccessOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * 数据签名节点
 * 负责使用市级节点公钥重新签名数据
 */
@Slf4j
@Component("signatureNode")
public class SignatureNode implements Node {

    @Autowired
    private SysAccessOrganizationService sysAccessOrganizationService;

    @Value("${provincial.node.signature.algorithm}")
    private String signatureAlgorithm;

    private static final String NODE_ID = "signatureNode";
    private static final String NODE_NAME = "签名节点";

    @Override
    public boolean execute(ProcessContext context) {
        log.info("执行签名节点: {}", context.getRequestId());
        
        try {
            // 1. 获取AppKey
            String appKey = context.getAppKey();
            
            // 2. 获取全国节点返回的数据（从上下文中获取）
            String nationalNodeResponse = context.getAttribute("nationalNodeResponse");
            if (nationalNodeResponse == null || nationalNodeResponse.isEmpty()) {
                context.markFailure(ResultCode.DATA_NOT_FOUND.getCode(), "全国节点返回数据为空");
                log.error("全国节点返回数据为空");
                return false;
            }
            
            // 3. 获取机构信息
            SysAccessOrganizationEntity org = sysAccessOrganizationService.selectByAppKey(appKey);
            if (org == null) {
                throw new RuntimeException("未找到对应的机构信息: " + appKey);
            }
            
            // 4. 获取省级私钥进行签名（实际项目中应使用安全的方式存储和获取私钥）
            String privateKeyStr = getProvincialPrivateKey();
            
            // 5. 对数据进行签名
            String signature = signData(nationalNodeResponse, privateKeyStr);
            
            // 6. 将签名结果保存到上下文中
            context.setAttribute("signatureResult", signature);
            
            log.info("数据签名成功: {}", context.getRequestId());
            return true;
        } catch (Exception e) {
            log.error("签名节点执行异常: {}", e.getMessage(), e);
            context.markFailure(ResultCode.SYSTEM_ERROR.getCode(), "数据签名异常");
            return false;
        }
    }

    /**
     * 获取省级节点的私钥
     * 实际项目中应使用更安全的方式存储和获取私钥，例如密钥管理器
     * 
     * @return 私钥字符串
     */
    private String getProvincialPrivateKey() {
        // 这里仅做示例，实际应从安全的存储中获取
        // 例如密钥库、密钥管理系统等
        return "provincial_private_key_example";
    }

    /**
     * 对数据进行签名
     * 
     * @param content 待签名内容
     * @param privateKeyStr 私钥字符串
     * @return 签名结果
     */
    private String signData(String content, String privateKeyStr) throws Exception {
        // 1. 将Base64编码的私钥字符串转换为PrivateKey对象
        byte[] privateKeyBytes = Base64.decodeBase64(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        
        // 2. 进行签名
        Signature sig = Signature.getInstance(signatureAlgorithm);
        sig.initSign(privateKey);
        sig.update(content.getBytes("UTF-8"));
        byte[] signatureBytes = sig.sign();
        
        // 3. 将签名结果进行Base64编码
        return Base64.encodeBase64String(signatureBytes);
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