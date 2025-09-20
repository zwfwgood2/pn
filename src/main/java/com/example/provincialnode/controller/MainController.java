package com.example.provincialnode.controller;

import com.alibaba.fastjson.JSON;
import com.example.provincialnode.common.Result;
import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.entity.SysAccessOrganizationEntity;
import com.example.provincialnode.entity.SysInterfaceDefinitionEntity;
import com.example.provincialnode.exception.BusinessException;
import com.example.provincialnode.processor.ProcessEngine;
import com.example.provincialnode.processor.context.ProcessContext;
import com.example.provincialnode.service.SysAccessOrganizationService;
import com.example.provincialnode.service.SysInterfaceDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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
    private SysAccessOrganizationService sysAccessOrganizationService;

    @Autowired
    private ProcessEngine processEngine;
    /**
     * 处理所有接口请求
     * 通配符路径，根据URL后缀识别接口
     */
    @RequestMapping(value = "**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public Map<String,Object> handleRequest(@RequestBody(required = false) Map<String, Object> requestBody,HttpServletRequest request) {
        //获取请求路径不包含上下文
        String interfacePath=getInterfacePathWithoutContext(request);
        log.info("收到请求: URL={}", interfacePath);
        ProcessContext context=new ProcessContext();
        try {
            // 根据请求路径查询接口定义
            SysInterfaceDefinitionEntity interfaceDefinition = sysInterfaceDefinitionService.findByRequestPath(interfacePath);
            if(interfaceDefinition == null){
               return  Result.error(ResultCode.NOT_FOUND.getCode(), "接口定义不存在").getResult();
            }

            //  检查接口是否启用
            if (interfaceDefinition.getStatus() != 1) {
                log.error("接口未启用: {}", interfacePath);
                return Result.error(ResultCode.SERVICE_UNAVAILABLE.getCode(), "接口未启用").getResult();
            }

            //校验入参规则是否符合市级规范
            validateRequestBody(requestBody);
            
            // 获取接口编码
            String interfaceCode = interfaceDefinition.getInterfaceCode();
            
            // 构建处理上下文
            context = buildProcessContext(requestBody, interfaceCode,request);
            
            // 按照接口维度加载流程执行
            // 检查接口是否配置了特定流程
            String processCode = interfaceDefinition.getProcessCode();
            // 使用流程引擎执行流程
            processEngine.executeProcess(processCode, context);
            log.info("接口请求处理完成: {}, 结果: {}", interfaceCode,context.isSuccess());
            if(context.isSuccess()){
                return Result.success((Map<String, Object>) context.getResponseData()).getResult();
            }else {
                return Result.error(context.getErrorCode(),context.getErrorMessage()).getResult();
            }
        } catch (Exception e) {
            String errorMsg = "处理请求异常: " + e.getMessage();
            log.error(errorMsg, e);
            if(e instanceof BusinessException){
              return Result.error(ResultCode.SYSTEM_ERROR.getCode(),e.getMessage()).getResult();
            }
            return Result.error(ResultCode.SYSTEM_ERROR).getResult();
        }
    }

    /**
     * 流程重放API
     * 用于重新执行失败的流程
     */
    @PostMapping("/replay/{executionId}")
    public Map<String,Object> replayProcess(@PathVariable String executionId) {
        log.info("收到流程重放请求: {}", executionId);
        ProcessContext context =new ProcessContext();
        try {
            // 调用流程引擎进行重放
            context= processEngine.replayProcess(executionId);
            log.info("流程重放完成: {}, 结果: {}", executionId,context.isSuccess());
            //流程引擎统一返回ProcessContext 可结合业务情况封装自己的响应结构体进行响应，ProcessContext包含结果数据，错误信息，错误码等信息
            return Result.success((Map<String, Object>) context.getResponseData()).getResult();
        } catch (Exception e) {
            log.error("流程重放异常: {}", e.getMessage(), e);
            return Result.error(context.getErrorCode(), context.getErrorMessage()).getResult();
        }
    }

    private void validateRequestBody(Map<String, Object> requestBody) {
        if (requestBody == null) {
            throw new BusinessException("请求参数不能为空");
        }
        if (!requestBody.containsKey("txnCommCom") || !requestBody.containsKey("txnBodyCom")) {
            throw new BusinessException("请求参数缺少必要字段");
        }
    }


    private String getInterfacePathWithoutContext(HttpServletRequest request) {

        // 1. 获取完整请求URI（包含上下文路径）
        String requestUri = request.getRequestURI(); // 例如：/myapp/user/123/detail

        // 2. 获取上下文路径（例如：/myapp）
        String contextPath = request.getContextPath(); // 若为根路径，返回""

        // 3. 从完整URI中去除上下文路径，得到目标路径

        return requestUri.substring(contextPath.length());
    }

    /**
     * 构建处理上下文
     */
    private ProcessContext buildProcessContext(Map<String, Object> requestBody, String interfaceCode,HttpServletRequest request) {
        ProcessContext context = new ProcessContext();
        
        // 1. 设置请求ID
        String requestId = UUID.randomUUID().toString();
        context.setRequestId(requestId);
        
        // 2. 设置接口代码
        context.setInterfaceCode(interfaceCode);

        //3.处理结构体将txnCommCom、txnBodyCom 放入一个map 作为请求参数,方便流程使用
        Object txnCommCom = requestBody.get("txnCommCom");
        Object txnBodyCom = requestBody.get("txnBodyCom");
        String txnBodyComJSON = JSON.toJSONString(txnBodyCom);
        String txnCommComJSON = JSON.toJSONString(txnCommCom);
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.putAll(JSON.parseObject(txnBodyComJSON, Map.class));
        requestParams.putAll(JSON.parseObject(txnCommComJSON, Map.class));
        context.setRequestParams(requestParams);
        // 4. 添加其他必要信息
        SysAccessOrganizationEntity self = sysAccessOrganizationService.selectByAppKey("self");
        context.setAttribute("selfPublicKey", self.getPublicKey());
        context.setAttribute("requestIp", getClientIp(request));
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