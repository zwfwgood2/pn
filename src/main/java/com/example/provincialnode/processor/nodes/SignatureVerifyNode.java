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
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.TreeMap;

/**
 * 验签节点
 * 负责验证请求数据的签名是否有效
 */
@Slf4j
@Component("signatureVerifyNode")
public class SignatureVerifyNode implements Node {

    @Autowired
    private SysAccessOrganizationService sysAccessOrganizationService;

    @Value("${provincial.node.signature.algorithm}")
    private String signatureAlgorithm;

    private static final String NODE_ID = "signatureVerifyNode";
    private static final String NODE_NAME = "验签节点";

    @Override
    public boolean execute(ProcessContext context) {
        log.info("执行验签节点: {}", context.getRequestId());
        
        try {
            // 1. 获取请求参数和AppKey
            Map<String, Object> requestParams = context.getRequestParams();
            String appKey = context.getAppKey();
            
            // 2. 从请求参数中获取签名
            String signature = (String) requestParams.get("signature");
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
            String publicKeyStr = org.getPublicKey();
            
            // 4. 构建验签数据（排除signature参数）
            Map<String, Object> signData = new TreeMap<>(requestParams);
            signData.remove("signature");
            String signContent = buildSignContent(signData);
            
            // 5. 验证签名
            boolean verifyResult = verifySignature(signContent, signature, publicKeyStr);
            
            if (!verifyResult) {
                context.markFailure(ResultCode.SIGNATURE_ERROR.getCode(), "签名验证失败");
                log.error("签名验证失败: appKey={}", appKey);
                return false;
            }
            
            log.info("签名验证成功: {}", context.getRequestId());
            return true;
        } catch (Exception e) {
            log.error("验签节点执行异常: {}", e.getMessage(), e);
            context.markFailure(ResultCode.SIGNATURE_ERROR.getCode(), "验签异常");
            return false;
        }
    }

    /**
     * 构建签名内容
     * 按照一定规则将参数排序并拼接
     * 
     * @param params 参数Map
     * @return 签名内容字符串
     */
    private String buildSignContent(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * 验证签名
     * 
     * @param content 签名内容
     * @param signature 签名值
     * @param publicKeyStr 公钥字符串
     * @return 验证结果
     */
    private boolean verifySignature(String content, String signature, String publicKeyStr) throws Exception {
        // 1. 将Base64编码的公钥字符串转换为PublicKey对象
        byte[] publicKeyBytes = Base64.decodeBase64(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        
        // 2. 验证签名
        Signature sig = Signature.getInstance(signatureAlgorithm);
        sig.initVerify(publicKey);
        sig.update(content.getBytes("UTF-8"));
        return sig.verify(Base64.decodeBase64(signature));
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