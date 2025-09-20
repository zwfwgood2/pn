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
 * {
 *   "//":"可选值：national（全国节点）、provincial（省级节点）、city（市级节点）",
 *   "side": "national"
 *  } 此配置项用于指定签名节点的运行环境以便于选择不 同的公私钥，默认值为city（市级节点）。 
 */
@Slf4j
@Component("verifyNode")
public class VerifyNode implements Node {

    @Autowired
    private SysAccessOrganizationService sysAccessOrganizationService;
    @Autowired
    private NationalNodeConfig mationalNodeConfig;
    private static final String NODE_ID = "verifyNode";
    private static final String NODE_NAME = "验签节点";

    @Override
    public boolean execute(ProcessContext context) {
        log.info("执行验签节点: {}", context.getRequestId());
        
        try {
            //获取请求参数和AppKey
            Map<String, Object> requestParams = context.getAttributeByParamName(Node.inParamName);
            String appKey = context.getAttribute("appKey").toString();
            //从请求参数中获取签名
            String signature = (String) requestParams.get("signatureData");
            if (signature == null || signature.isEmpty()) {
                context.markFailure(ResultCode.VERIFY_ERROR.getCode(), "签名不能为空");
                log.error("签名不能为空");
                return false;
            }

            //从节点配置中获取签名端点
            Map<String, Object> nodeConfig = context.getAttribute(Node.nodeConfig);
            if (nodeConfig == null || !nodeConfig.containsKey(Node.side)) {
                log.info("未配置验签端点,无法完成验签!");
                context.markFailure(ResultCode.VERIFY_ERROR.getCode(), "数据验签异常");
                return false;
            }

            //根据端点获取解密私钥和验签公钥
            String verifyPublicKey=null,decryptPrivateKey=null,encryptData=null,paramKey = null;
            //市级端点
            if(nodeConfig.get(Node.side).equals("city")){
                //获取机构的公钥
                SysAccessOrganizationEntity org = sysAccessOrganizationService.selectByAppKey(appKey);
                if (org == null) {
                    throw new RuntimeException("未找到对应的机构信息: " + appKey);
                }
                //默认为市级端点
                 verifyPublicKey =context.getRequestParams().get("publicKey").toString();
                 decryptPrivateKey=org.getPrivateKey();
                 encryptData=requestParams.get("requestData").toString();
                 paramKey="requestData";
            }
            //全国端点
            if(nodeConfig.get(Node.side).equals("national")){
                SysAccessOrganizationEntity self = sysAccessOrganizationService.selectByAppKey("self");
                verifyPublicKey=mationalNodeConfig.getPublicKey();
                decryptPrivateKey=self.getPrivateKey();
                encryptData=requestParams.get("data").toString();
                paramKey="data";
            }
            String originalData = SignUtil.verifySignature(requestParams,encryptData,verifyPublicKey,decryptPrivateKey);
            if (StrUtil.isEmpty(originalData)) {
                context.markFailure(ResultCode.VERIFY_ERROR.getCode(), "签名验证失败");
                log.error("签名验证失败: appKey={}", appKey);
                return false;
            }
            Map<String,Object> verifyResult=new HashMap<>();
            verifyResult.putAll(requestParams);
            verifyResult.put(paramKey,originalData);

            //将验签结果放入上下文
            context.setAttributeByParamName(Node.outParamName,verifyResult);
            log.info("签名验证成功: {}", context.getRequestId());
            return true;
        } catch (Exception e) {
            log.error("验签节点执行异常: {}", e.getMessage(), e);
            context.markFailure(ResultCode.VERIFY_ERROR.getCode(), "验签异常");
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