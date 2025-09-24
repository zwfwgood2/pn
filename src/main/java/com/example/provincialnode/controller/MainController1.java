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
public class MainController1 {

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
    @RequestMapping(value = "/b", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public String handleRequest(@RequestBody(required = false) Map<String, Object> requestBody,HttpServletRequest request) {
        return "hello tk";
    }
}