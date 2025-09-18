package com.example.provincialnode.processor.nodes;

import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.config.NationalNodeConfig;
import com.example.provincialnode.processor.Node;
import com.example.provincialnode.processor.context.ProcessContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 全国节点请求处理节点
 * 负责调用全国节点服务获取数据
 */
@Slf4j
@Component("nationalNodeRequestNode")
public class NationalNodeRequestNode implements Node {

    @Autowired
    private NationalNodeConfig nationalNodeConfig;

    private static final String NODE_ID = "nationalNodeRequestNode";
    private static final String NODE_NAME = "全国节点请求节点";

    @Override
    public boolean execute(ProcessContext context) {
        log.info("执行全国节点请求节点: {}", context.getRequestId());
        
        try {
            // 1. 获取请求参数和接口信息
            String interfaceCode = context.getInterfaceCode();
            Map<String, Object> requestParams = context.getAttribute(Node.inParamName);
            
            // 2. 构建全国节点请求URL
            String requestUrl = nationalNodeConfig.getNationalNodeUrl() + "/" + interfaceCode;
            
            // 3. 构建请求参数
            JSONObject requestBody = new JSONObject();
            requestBody.putAll(requestParams);
            
            // 4. 发送请求到全国节点
            String response = sendRequest(requestUrl, requestBody.toJSONString());
            
            // 5. 解析响应结果
            if (response == null || response.isEmpty()) {
                context.markFailure(ResultCode.NATIONAL_NODE_ERROR.getCode(), "全国节点返回空结果");
                log.error("全国节点返回空结果: {}", requestUrl);
                return false;
            }
            
            // 6. 将全国节点返回结果保存到上下文中
            context.setAttribute(Node.outParamName, response);
            
            log.info("全国节点请求成功: {}, 响应: {}", requestUrl, response);
            return true;
        } catch (Exception e) {
            log.error("全国节点请求节点执行异常: {}", e.getMessage(), e);
            context.markFailure(ResultCode.NATIONAL_NODE_ERROR.getCode(), "全国节点请求异常");
            return false;
        }
    }

    /**
     * 发送HTTP请求到全国节点
     * @param url 请求URL
     * @param requestBody 请求体
     * @return 响应结果
     */
    private String sendRequest(String url, String requestBody) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
        httpPost.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
        try {
            log.info("发送请求到全国节点,请求体: {}", requestBody);
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                response.close();
                httpClient.close();
                return responseBody;
            } else {
                log.warn("全国节点请求失败，状态码: {}", statusCode);
                EntityUtils.consume(response.getEntity());
                response.close();
            }
        } catch (IOException e) {
            log.warn("全国节点请求异常: {}", e.getMessage());
            throw new RuntimeException("全国节点请求失败！");
        }
        return null;
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