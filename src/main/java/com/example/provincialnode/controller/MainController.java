package com.example.provincialnode.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.provincialnode.common.Result;
import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.entity.SysInterfaceDefinitionEntity;
import com.example.provincialnode.processor.ProcessEngine;
import com.example.provincialnode.processor.context.ProcessContext;
import com.example.provincialnode.service.SysInterfaceDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 主控制器类
 * 处理所有接口请求，按照接口后缀找到流程执行
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class MainController {

    @Autowired
    private SysInterfaceDefinitionService sysInterfaceDefinitionService;

    @Autowired
    private ProcessEngine processEngine;

    /**
     * 处理所有接口请求
     * 通配符路径，根据URL后缀识别接口
     */
    @RequestMapping(value = "{b}", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public Result<JSONObject> handleRequest(MultipartFile file, @PathVariable String b , HttpServletRequest request, @RequestBody(required = false) Map<String, Object> requestBody) {
        // 1. 获取请求URL和请求方法
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();
        log.info(b);
        // 2. 从URL中提取接口路径
        // 去除上下文路径
        String contextPath = request.getContextPath();
        String interfacePath = requestURI.substring(contextPath.length());
        
        log.info("收到请求: URL={}, Method={}", interfacePath, requestMethod);
        
        try {
            // 3. 根据请求路径查询接口定义
            SysInterfaceDefinitionEntity interfaceDefinition = sysInterfaceDefinitionService.findByRequestPath(interfacePath);
            if(interfaceDefinition == null){
               Result.error(ResultCode.NOT_FOUND.getCode(), "接口定义不存在");
            }

            // 4. 检查接口是否启用
            if (interfaceDefinition.getStatus() != 1) {
                log.error("接口未启用: {}", interfacePath);
                return Result.error(ResultCode.SERVICE_UNAVAILABLE.getCode(), "接口未启用");
            }
            
            // 5. 获取接口代码
            String interfaceCode = interfaceDefinition.getInterfaceCode();
            
            // 6. 构建处理上下文
            ProcessContext context = buildProcessContext(request, requestBody, interfaceCode, interfaceDefinition);
            
            // 7.按照接口维度加载流程执行
            // 检查接口是否配置了特定流程
            String processCode = interfaceDefinition.getProcessCode();
            // 使用流程引擎执行流程
            Result<JSONObject> result = processEngine.executeProcess(processCode, context);
            log.info("接口请求处理完成: {}, 结果: {}", interfaceCode, result.getCode());
            return result;
        } catch (Exception e) {
            String errorMsg = "处理请求异常: " + e.getMessage();
            log.error(errorMsg, e);
            
            // 记录异常日志
            String requestId = UUID.randomUUID().toString();
            log.error("请求ID: {}, 请求URL: {}, 异常信息: {}", requestId, interfacePath, e.getMessage());
            
            return Result.error(ResultCode.SYSTEM_ERROR.getCode(), "系统内部错误");
        }
    }

    /**
     * 流程重放API
     * 用于重新执行失败的流程
     */
    @PostMapping("/api/replay/{executionId}")
    public Result<?> replayProcess(@PathVariable String executionId) {
        log.info("收到流程重放请求: {}", executionId);
        
        try {
            // 调用流程引擎进行重放
            Result<?> result = processEngine.replayProcess(executionId);
            
            log.info("流程重放完成: {}, 结果: {}", executionId, result.getCode());
            return result;
        } catch (Exception e) {
            log.error("流程重放异常: {}", e.getMessage(), e);
            return Result.error(ResultCode.SYSTEM_ERROR.getCode(), "流程重放失败: " + e.getMessage());
        }
    }

    /**
     * 构建处理上下文
     */
    private ProcessContext buildProcessContext(HttpServletRequest request, Map<String, Object> requestBody, 
                                             String interfaceCode, SysInterfaceDefinitionEntity interfaceDefinition) {
        ProcessContext context = new ProcessContext();
        
        // 1. 设置请求ID
        String requestId = UUID.randomUUID().toString();
        context.setRequestId(requestId);
        
        // 2. 设置接口代码
        context.setInterfaceCode(interfaceCode);
        
        // 3. 获取AppKey（从请求头或参数中获取）
        String appKey = request.getHeader("appKey");
        if (appKey == null || appKey.isEmpty()) {
            appKey = request.getParameter("appKey");
        }
        if (appKey == null || appKey.isEmpty() && requestBody != null) {
            appKey = (String) requestBody.get("appKey");
        }
        context.setAppKey(appKey);
        
        // 4. 构建请求参数
        Map<String, Object> requestParams = new HashMap<>();
        
        // 添加请求参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            if (entry.getValue().length == 1) {
                requestParams.put(entry.getKey(), entry.getValue()[0]);
            } else {
                requestParams.put(entry.getKey(), entry.getValue());
            }
        }
        
        // 添加请求体参数
        if (requestBody != null) {
            requestParams.putAll(requestBody);
        }

       // requestParams.put("_headers", headers);
        
        // 添加请求方法
        requestParams.put("_method", request.getMethod());
        
        // 添加URL路径信息，用于根据后缀识别接口
        requestParams.put("_url", request.getRequestURI());
        
        // 获取URL后缀（例如：/api/user/list.json -> json）
        String url = request.getRequestURI();
        int lastDotIndex = url.lastIndexOf('.');
        if (lastDotIndex > 0) {
            String suffix = url.substring(lastDotIndex + 1);
            requestParams.put("_urlSuffix", suffix);
        }
        
        context.setRequestParams(requestParams);
        
        // 5. 添加其他必要信息
        context.setAttribute("requestIp", getClientIp(request));
        context.setAttribute("interfaceDefinition", interfaceDefinition);
        
        return context;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 多级代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

}