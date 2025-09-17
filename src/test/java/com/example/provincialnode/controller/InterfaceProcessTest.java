package com.example.provincialnode.controller;

import com.example.provincialnode.common.Result;
import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.entity.SysAccessOrganizationEntity;
import com.example.provincialnode.entity.SysInterfaceDefinitionEntity;
import com.example.provincialnode.mapper.SysAccessOrganizationMapper;
import com.example.provincialnode.mapper.SysInterfaceDefinitionMapper;
import com.example.provincialnode.processor.ProcessEngine;
import com.example.provincialnode.service.SysAccessOrganizationService;
import com.example.provincialnode.service.SysInterfaceDefinitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 接口处理流程测试
 * 测试整个接口处理的完整流程
 */
@SpringBootTest
public class InterfaceProcessTest {

    @Mock
    private SysInterfaceDefinitionService sysInterfaceDefinitionService;

    @Mock
    private SysAccessOrganizationService sysAccessOrganizationService;

    @Mock
    private ProcessEngine processEngine;

    @InjectMocks
    private MainController mainController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(mainController).build();
    }

    /**
     * 测试查询接口处理流程
     */
    @Test
    void testQueryInterfaceProcess() throws Exception {
        // 1. 设置测试数据
        String interfaceCode = "QUERY_ENTERPRISE_INFO";
        String requestUrl = "/api/" + interfaceCode;
        String appKey = "test_app_key";
        String token = "test_token";
        
        // 2. 模拟接口定义
        SysInterfaceDefinitionEntity interfaceDefinition = new SysInterfaceDefinitionEntity();
        interfaceDefinition.setId(1L);
        interfaceDefinition.setInterfaceCode(interfaceCode);
        interfaceDefinition.setInterfaceName("查询企业信息接口");
        interfaceDefinition.setRequestPath("/api/" + interfaceCode);
        interfaceDefinition.setRequestMethod("GET");
        interfaceDefinition.setStatus(1);
        interfaceDefinition.setInterfaceType(0);
        
        // 3. 模拟机构信息
        SysAccessOrganizationEntity organization = new SysAccessOrganizationEntity();
        organization.setId(1L);
        organization.setOrgCode("TEST_ORG");
        organization.setOrgName("测试机构");
        organization.setAppKey(appKey);
        organization.setStatus(1);
        
        // 4. 模拟处理结果
        Result<?> mockResult = Result.success("测试数据");

        // 6. 构建请求参数
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("enterpriseName", "测试企业");
        requestBody.put("creditCode", "91110000MA001AA1AA");
        
        String requestJson = "{\"enterpriseName\":\"测试企业\",\"creditCode\":\"91110000MA001AA1AA\"}";
        
        // 7. 执行请求
        mockMvc.perform(post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("appKey", appKey)
                .header("token", token)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").value("测试数据"));
    }

    /**
     * 测试写入接口处理流程
     */
    @Test
    void testWriteInterfaceProcess() throws Exception {
        // 1. 设置测试数据
        String interfaceCode = "UPLOAD_ENTERPRISE_INFO";
        String requestUrl = "/api/" + interfaceCode;
        String appKey = "test_app_key";
        String token = "test_token";
        
        // 2. 模拟接口定义
        SysInterfaceDefinitionEntity interfaceDefinition = new SysInterfaceDefinitionEntity();
        interfaceDefinition.setId(2L);
        interfaceDefinition.setInterfaceCode(interfaceCode);
        interfaceDefinition.setInterfaceName("上传企业信息接口");
        interfaceDefinition.setRequestPath("/api/" + interfaceCode);
        interfaceDefinition.setRequestMethod("POST");
        interfaceDefinition.setStatus(1);
        interfaceDefinition.setInterfaceType(1);

        // 3. 模拟机构信息
        SysAccessOrganizationEntity organization = new SysAccessOrganizationEntity();
        organization.setId(1L);
        organization.setOrgCode("TEST_ORG");
        organization.setOrgName("测试机构");
        organization.setAppKey(appKey);
        organization.setStatus(1);
        
        // 4. 模拟处理结果
        Result<?> mockResult = Result.success("数据上传成功");

        // 6. 构建请求参数
        String requestJson = "{\"enterpriseName\":\"测试企业\",\"creditCode\":\"91110000MA001AA1AA\",\"contactPerson\":\"张三\",\"contactPhone\":\"13800138000\"}";
        
        // 7. 执行请求
        mockMvc.perform(post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("appKey", appKey)
                .header("token", token)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(ResultCode.SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data").value("数据上传成功"));
    }

    /**
     * 测试接口禁用情况
     */
    @Test
    void testDisabledInterface() throws Exception {
        // 1. 设置测试数据
        String interfaceCode = "DISABLED_INTERFACE";
        String requestUrl = "/api/" + interfaceCode;
        String appKey = "test_app_key";
        String token = "test_token";
        
        // 2. 模拟禁用的接口定义
        SysInterfaceDefinitionEntity interfaceDefinition = new SysInterfaceDefinitionEntity();
        interfaceDefinition.setId(3L);
        interfaceDefinition.setInterfaceCode(interfaceCode);
        interfaceDefinition.setInterfaceName("禁用的接口");
        interfaceDefinition.setRequestPath("/api/" + interfaceCode);
        interfaceDefinition.setInterfaceType(1);
        interfaceDefinition.setStatus(0); // 接口禁用

        // 4. 执行请求
        mockMvc.perform(post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("appKey", appKey)
                .header("token", token)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ResultCode.NOT_FOUND.getMessage()));
    }

    /**
     * 测试无效的接口路径
     */
    @Test
    void testInvalidInterfacePath() throws Exception {
        // 1. 设置测试数据
        String requestUrl = "/api/non_existent_interface";
        String appKey = "test_app_key";
        String token = "test_token";
        
//        // 2. 设置mock行为
//        when(interfaceDefinitionRepository.findByRequestPath(anyString())).thenReturn(Optional.empty());
        
        // 3. 执行请求
        mockMvc.perform(post(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("appKey", appKey)
                .header("token", token)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.DATA_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ResultCode.NOT_FOUND.getMessage()));
    }

}