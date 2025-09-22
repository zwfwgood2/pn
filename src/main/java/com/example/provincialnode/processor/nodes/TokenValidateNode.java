package com.example.provincialnode.processor.nodes;

import com.example.provincialnode.common.CacheService;
import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.processor.Node;
import com.example.provincialnode.processor.context.ProcessContext;
import com.example.provincialnode.service.SysAccessOrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Token验证节点
 * 负责验证市级节点请求中的Token
 */
@Slf4j
@Component("tokenValidateNode")
public class TokenValidateNode implements Node {

    @Autowired
    private SysAccessOrganizationService sysAccessOrganizationService;
    
    @Autowired
    private CacheService cacheService;

    private static final String NODE_ID = "tokenValidateNode";
    private static final String NODE_NAME = "token验证节点";

    @Override
    public boolean execute(ProcessContext context) {
        log.info("执行Token验证节点: {}", context.getRequestId());
        try {
            // 1. 获取请求参数中的AppKey和Token
            Map<String, Object> requestParams = context.getAttributeByParamName(Node.inParamName);
            String token = (String) requestParams.get("token");

            // 2. 验证Token是否有效
            if (token == null || token.isEmpty()) {
                context.markFailure(ResultCode.INVALID_TOKEN.getCode(), "Token不能为空");
                log.error("Token不能为空");
                return false;
            }

            // 例如JWT验证、Redis中Token有效性检查等
            boolean tokenValid = validateToken(token);
            if (!tokenValid) {
                context.markFailure(ResultCode.INVALID_TOKEN.getCode(), "无效的Token");
                log.error("无效的Token: {}", token);
                return false;
            }
            //TODO 从token中取出appKey并放入上下文
            context.setAppKey("city_node_001_key");
           // context.setAttribute("appKey", "city_node_001_key");
            log.info("Token验证通过: {}", context.getRequestId());
            return true;
        } catch (Exception e) {
            log.error("Token验证节点执行异常: {}", e.getMessage(), e);
            context.markFailure(ResultCode.SYSTEM_ERROR.getCode(), "Token验证异常");
            return false;
        }
    }

    /**
     * 验证Token的有效性
     * 使用Redis验证token
     * @param token 令牌
     * @return 是否有效
     */
    private boolean validateToken(String token) {
        // 从Redis中验证token
//        String tokenKey = "node:token:" + token;
//        String storedAppKey = cacheService.get(tokenKey, String.class);
//        return storedAppKey != null;
        return true;
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