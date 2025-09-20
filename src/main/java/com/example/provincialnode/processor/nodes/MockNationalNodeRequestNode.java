package com.example.provincialnode.processor.nodes;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.common.SignUtil;
import com.example.provincialnode.config.NationalNodeConfig;
import com.example.provincialnode.entity.SysInterfaceDefinitionEntity;
import com.example.provincialnode.processor.Node;
import com.example.provincialnode.processor.context.ProcessContext;
import com.example.provincialnode.service.SysInterfaceDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("mockNationalNodeRequestNode")
public class MockNationalNodeRequestNode implements Node {

    private static final String NODE_ID = "mockNationalNodeRequestNode";
    private static final String NODE_NAME = "全国节点请求MOCK节点";
    @Autowired
    private NationalNodeConfig nationalNodeConfig;

    @Autowired
    private SysInterfaceDefinitionService sysInterfaceDefinitionService;
    @Override
    public boolean execute(ProcessContext context) {
        log.info("执行全国节点请求节点: {}", context.getRequestId());
        try {
            // 1. 获取请求参数和接口信息
            String interfaceCode = context.getInterfaceCode();
            Map<String, Object> requestParams = context.getAttributeByParamName(Node.inParamName);

            // 2. 构建全国节点请求URL
            String requestUrl = nationalNodeConfig.getNationalNodeUrl() + "/" + interfaceCode;

            // 3. 构建请求参数
            JSONObject requestBody = new JSONObject();
            requestBody.putAll(requestParams);
            //移除全国节点不需要的字段
            requestBody.remove("txnIttChnlId");
            requestBody.remove("txnIttChnlCgyCode");
            //替换省级节点publicKey和国家节点token
            requestBody.put("publicKey",context.getAttribute("selfPublicKey").toString());
            requestBody.put("token", nationalNodeConfig.getToken());

            // 4. 发送请求到全国节点
            String response = sendRequest(interfaceCode, requestBody.toJSONString());

            // 5. 解析响应结果
            if (response == null || response.isEmpty()) {
                context.markFailure(ResultCode.NATIONAL_NODE_ERROR.getCode(), "全国节点返回空结果");
                log.error("全国节点返回空结果: {}", requestUrl);
                return false;
            }
            //6. 转换为市级接口需要的格式
            Map<String,Object> nationalNodeResponse = JSON.parseObject(response, Map.class);
            if(!((boolean) nationalNodeResponse.get("success"))){
                context.markFailure(nationalNodeResponse.get("errorCode").toString(), nationalNodeResponse.get("errorMessage").toString());
                log.error("全国节点返回异常，结果: {}", nationalNodeResponse);
                return false;
            }

            // 7. 将全国节点返回结果保存到上下文中
            context.setAttributeByParamName(Node.outParamName, nationalNodeResponse);
            log.info("全国节点请求成功: {}, 国家节点响应:{},转换市级响应: {}", requestUrl,response,nationalNodeResponse);
            return true;
        } catch (Exception e) {
            log.error("全国节点请求节点执行异常: {}", e.getMessage(), e);
            context.markFailure(ResultCode.NATIONAL_NODE_ERROR.getCode(), "全国节点请求异常");
            return false;
        }
    }

    /**
     * 发送HTTP请求到全国节点
     * @param interfaceCode 请求URL
     * @param requestBody 请求体
     * @return 响应结果
     */
    private String sendRequest(String interfaceCode, String requestBody) {
        //String responseBody="{\n" +
        //                        "\"success\": true, \"errorCode\": \"\", \"errorMessage\": \"\",
        //                        \"timestamp\": 1566147961604, \"key\": \"data 节点 SM4 算法加密秘钥\",
        //                        \"data\": \"服务端相应加密报文\", \"signatureData\": \"签名数据\"}";
        try {
            log.info("发送请求到全国节点,请求体: {}", requestBody);
            String publicKey = nationalNodeConfig.getPublicKey();
            String privateKey="MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgKc2O0VKMKis8BXs7qoyG9rI5ZEhQDJ+EI6dkccdvkMOgCgYIKoEcz1UBgi2hRANCAAQJa0ePyHLISNNjcQIT2XyLZPoBCEQ6y4Piqp/ZQjh7FUfmtsgEnKuDZ113Jj5/4UYGt0C4th5GKYVj1aoW2BQW";
            JSONObject responseBody = new JSONObject();
            responseBody.put("success", true);
            responseBody.put("errorCode", "");
            responseBody.put("errorMessage", "");
            responseBody.put("timestamp", System.currentTimeMillis());
            //查询接口类型
            SysInterfaceDefinitionEntity interfaceDefinition = sysInterfaceDefinitionService.findByInterfaceCode(interfaceCode);
            //读接口
            if(interfaceDefinition.getInterfaceType().equals(0)){
                List<JSONObject> responseData = new ArrayList<>(2);
                JSONObject responseData1 = new JSONObject();
                responseData1.put("orgCode", "111111");
                JSONObject responseData2 = new JSONObject();
                responseData2.put("orgCode", "222222");
                responseData.add(responseData1);
                responseData.add(responseData2);
                String data = JSON.toJSONString(responseData);
                JSONObject requestData = JSON.parseObject(requestBody);
                //对响应体进行签名并返回
                Map<String, Object> stringStringMap = SignUtil.signData(data, privateKey,requestData.get("publicKey").toString());
                responseBody.put("data", stringStringMap.get("encryptedData"));
                responseBody.put("signatureData", stringStringMap.get("signatureData"));
                responseBody.put("key", stringStringMap.get("key"));
                return responseBody.toJSONString();
            }else{ //写接口
                //验签并返回
                JSONObject requestData = JSON.parseObject(requestBody);
                String originalData = SignUtil.verifySignature(requestData, requestData.get("requestData").toString(), requestData.get("publicKey").toString(),privateKey);
                if(StrUtil.isNotBlank(originalData)){
                    //插入数据
                    //返回结果
                    return responseBody.toJSONString();
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("全国节点请求异常: {}", e.getMessage());
            throw new RuntimeException("全国节点请求失败！");
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
