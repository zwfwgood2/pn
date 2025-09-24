package com.example.provincialnode.processor.nodes;

import com.alibaba.fastjson.JSON;
import com.example.provincialnode.common.ResultCode;
import com.example.provincialnode.processor.Node;
import com.example.provincialnode.processor.context.ProcessContext;
import com.example.provincialnode.processor.validator.ParamValidator;
import com.example.provincialnode.processor.validator.ParamValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class EnhancedParamValidateNodeTest {

    @Mock
    private ParamValidatorFactory validatorFactory;

    @Mock
    private ParamValidator requiredValidator;

    @Mock
    private ParamValidator dataTypeValidator;

    @Mock
    private ParamValidator stringLengthValidator;

    @InjectMocks
    private EnhancedParamValidateNode node;

    private ProcessContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        context = mock(ProcessContext.class);
        when(context.getRequestId()).thenReturn("test-request-id");
        when(context.getAppKey()).thenReturn("test-app-key");
        
        // 模拟验证器行为
        when(validatorFactory.getValidator("required")).thenReturn(requiredValidator);
        when(validatorFactory.getValidator("dataType")).thenReturn(dataTypeValidator);
        when(validatorFactory.getValidator("stringLength")).thenReturn(stringLengthValidator);
        
        // 默认验证通过
        when(requiredValidator.validate(any(), any())).thenReturn(true);
        when(dataTypeValidator.validate(any(), any())).thenReturn(true);
        when(stringLengthValidator.validate(any(), any())).thenReturn(true);
        
        when(requiredValidator.getErrorMessage(anyString(), any(), any())).thenReturn("参数不能为空");
        when(dataTypeValidator.getErrorMessage(anyString(), any(), any())).thenReturn("参数类型错误");
        when(stringLengthValidator.getErrorMessage(anyString(), any(), any())).thenReturn("参数长度不符合要求");
    }

    @Test
    void testSimpleValidation() {
        // 准备测试数据
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("orgCode", "ABC123");
        
        List<Map<String, Object>> rules = new ArrayList<>();
        Map<String, Object> fieldRule = new HashMap<>();
        fieldRule.put("paramPath", "orgCode");
        List<Map<String, Object>> validators = new ArrayList<>();
        Map<String, Object> requiredRule = new HashMap<>();
        requiredRule.put("type", "required");
        requiredRule.put("config", true);
        validators.add(requiredRule);
        fieldRule.put("rules", validators);
        rules.add(fieldRule);
        
        Map<String, Object> nodeConfig = new HashMap<>();
        nodeConfig.put("validateRules", JSON.toJSONString(rules));
        
        // 设置上下文
        when(context.getAttribute(Node.nodeConfig)).thenReturn(nodeConfig);
        when(context.getAttributeByParamName(Node.inParamName)).thenReturn(requestParams);
        
        // 执行测试
        boolean result = node.execute(context);
        
        // 验证结果
        assertTrue(result);
        verify(requiredValidator).validate("ABC123", true);
        verify(context, never()).markFailure(anyString(), anyString());
    }

    @Test
    void testNestedObjectValidation() {
        // 准备测试数据
        Map<String, Object> requestParams = new HashMap<>();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", "张三");
        userInfo.put("age", 25);
        requestParams.put("userInfo", userInfo);
        
        // 构建嵌套验证规则
        List<Map<String, Object>> rules = new ArrayList<>();
        Map<String, Object> fieldRule = new HashMap<>();
        fieldRule.put("paramPath", "userInfo");
        List<Map<String, Object>> validators = new ArrayList<>();
        
        // 数据类型验证
        Map<String, Object> dataTypeRule = new HashMap<>();
        dataTypeRule.put("type", "dataType");
        dataTypeRule.put("config", "object");
        
        // 嵌套验证规则
        List<Map<String, Object>> nestedRules = new ArrayList<>();
        Map<String, Object> nameRule = new HashMap<>();
        nameRule.put("paramPath", "name");
        List<Map<String, Object>> nameValidators = new ArrayList<>();
        Map<String, Object> nameRequiredRule = new HashMap<>();
        nameRequiredRule.put("type", "required");
        nameRequiredRule.put("config", true);
        nameValidators.add(nameRequiredRule);
        nameRule.put("rules", nameValidators);
        nestedRules.add(nameRule);
        
        dataTypeRule.put("nestedRules", nestedRules);
        validators.add(dataTypeRule);
        fieldRule.put("rules", validators);
        rules.add(fieldRule);
        
        Map<String, Object> nodeConfig = new HashMap<>();
        nodeConfig.put("validateRules", JSON.toJSONString(rules));
        
        // 设置上下文
        when(context.getAttribute(Node.nodeConfig)).thenReturn(nodeConfig);
        when(context.getAttributeByParamName(Node.inParamName)).thenReturn(requestParams);
        
        // 执行测试
        boolean result = node.execute(context);
        
        // 验证结果
        assertTrue(result);
        verify(requiredValidator).validate("张三", true);
    }

    @Test
    void testArrayValidation() {
        // 准备测试数据
        Map<String, Object> requestParams = new HashMap<>();
        List<String> ids = Arrays.asList("123", "456", "789");
        requestParams.put("ids", ids);
        
        // 构建数组验证规则
        List<Map<String, Object>> rules = new ArrayList<>();
        Map<String, Object> fieldRule = new HashMap<>();
        fieldRule.put("paramPath", "ids");
        List<Map<String, Object>> validators = new ArrayList<>();
        
        // 数据类型验证
        Map<String, Object> dataTypeRule = new HashMap<>();
        dataTypeRule.put("type", "dataType");
        dataTypeRule.put("config", "array");
        
        // 数组元素验证规则
        List<Map<String, Object>> elementRules = new ArrayList<>();
        Map<String, Object> elementRule = new HashMap<>();
        List<Map<String, Object>> elementValidators = new ArrayList<>();
        Map<String, Object> lengthRule = new HashMap<>();
        lengthRule.put("type", "stringLength");
        Map<String, Object> lengthConfig = new HashMap<>();
        lengthConfig.put("minLength", 3);
        lengthConfig.put("maxLength", 3);
        lengthRule.put("config", lengthConfig);
        elementValidators.add(lengthRule);
        elementRule.put("rules", elementValidators);
        elementRules.add(elementRule);
        
        dataTypeRule.put("nestedRules", elementRules);
        validators.add(dataTypeRule);
        fieldRule.put("rules", validators);
        rules.add(fieldRule);
        
        Map<String, Object> nodeConfig = new HashMap<>();
        nodeConfig.put("validateRules", JSON.toJSONString(rules));
        
        // 设置上下文
        when(context.getAttribute(Node.nodeConfig)).thenReturn(nodeConfig);
        when(context.getAttributeByParamName(Node.inParamName)).thenReturn(requestParams);
        
        // 执行测试
        boolean result = node.execute(context);
        
        // 验证结果
        assertTrue(result);
        verify(stringLengthValidator, times(3)).validate(anyString(), eq(lengthConfig));
    }

    @Test
    void testValidationFailure() {
        // 准备测试数据
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("orgCode", null);
        
        List<Map<String, Object>> rules = new ArrayList<>();
        Map<String, Object> fieldRule = new HashMap<>();
        fieldRule.put("paramPath", "orgCode");
        List<Map<String, Object>> validators = new ArrayList<>();
        Map<String, Object> requiredRule = new HashMap<>();
        requiredRule.put("type", "required");
        requiredRule.put("config", true);
        validators.add(requiredRule);
        fieldRule.put("rules", validators);
        rules.add(fieldRule);
        
        Map<String, Object> nodeConfig = new HashMap<>();
        nodeConfig.put("validateRules", JSON.toJSONString(rules));
        
        // 设置上下文
        when(context.getAttribute(Node.nodeConfig)).thenReturn(nodeConfig);
        when(context.getAttributeByParamName(Node.inParamName)).thenReturn(requestParams);
        
        // 模拟验证失败
        when(requiredValidator.validate(null, true)).thenReturn(false);
        
        // 执行测试
        boolean result = node.execute(context);
        
        // 验证结果
        assertFalse(result);
        verify(context).markFailure(ResultCode.PARAM_ERROR.getCode(), "参数不能为空");
    }

    @Test
    void testJsonStringValidation() {
        // 准备测试数据
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("jsonData", "{\"user\":{\"name\":\"张三\",\"age\":25}}}");
        
        // 构建JSON字符串验证规则
        List<Map<String, Object>> rules = new ArrayList<>();
        Map<String, Object> fieldRule = new HashMap<>();
        fieldRule.put("paramPath", "jsonData");
        List<Map<String, Object>> validators = new ArrayList<>();
        
        // 数据类型验证
        Map<String, Object> dataTypeRule = new HashMap<>();
        dataTypeRule.put("type", "dataType");
        dataTypeRule.put("config", "json");
        
        // JSON内部字段验证规则
        List<Map<String, Object>> jsonRules = new ArrayList<>();
        Map<String, Object> userRule = new HashMap<>();
        userRule.put("paramPath", "user.name");
        List<Map<String, Object>> userValidators = new ArrayList<>();
        Map<String, Object> nameRequiredRule = new HashMap<>();
        nameRequiredRule.put("type", "required");
        nameRequiredRule.put("config", true);
        userValidators.add(nameRequiredRule);
        userRule.put("rules", userValidators);
        jsonRules.add(userRule);
        
        dataTypeRule.put("nestedRules", jsonRules);
        validators.add(dataTypeRule);
        fieldRule.put("rules", validators);
        rules.add(fieldRule);
        
        Map<String, Object> nodeConfig = new HashMap<>();
        nodeConfig.put("validateRules", JSON.toJSONString(rules));
        
        // 设置上下文
        when(context.getAttribute(Node.nodeConfig)).thenReturn(nodeConfig);
        when(context.getAttributeByParamName(Node.inParamName)).thenReturn(requestParams);
        
        // 执行测试
        boolean result = node.execute(context);
        
        // 验证结果
        assertTrue(result);
    }

    @Test
    void testJsonParseFailure() {
        // 准备测试数据 - 无效的JSON
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("jsonData", "{invalid json}");
        
        // 构建JSON字符串验证规则
        List<Map<String, Object>> rules = new ArrayList<>();
        Map<String, Object> fieldRule = new HashMap<>();
        fieldRule.put("paramPath", "jsonData");
        List<Map<String, Object>> validators = new ArrayList<>();
        
        // 数据类型验证
        Map<String, Object> dataTypeRule = new HashMap<>();
        dataTypeRule.put("type", "dataType");
        dataTypeRule.put("config", "json");
        
        // JSON内部字段验证规则
        List<Map<String, Object>> jsonRules = new ArrayList<>();
        Map<String, Object> userRule = new HashMap<>();
        userRule.put("paramPath", "user.name");
        List<Map<String, Object>> userValidators = new ArrayList<>();
        Map<String, Object> nameRequiredRule = new HashMap<>();
        nameRequiredRule.put("type", "required");
        nameRequiredRule.put("config", true);
        userValidators.add(nameRequiredRule);
        userRule.put("rules", userValidators);
        jsonRules.add(userRule);
        
        dataTypeRule.put("nestedRules", jsonRules);
        validators.add(dataTypeRule);
        fieldRule.put("rules", validators);
        rules.add(fieldRule);
        
        Map<String, Object> nodeConfig = new HashMap<>();
        nodeConfig.put("validateRules", JSON.toJSONString(rules));
        
        // 设置上下文
        when(context.getAttribute(Node.nodeConfig)).thenReturn(nodeConfig);
        when(context.getAttributeByParamName(Node.inParamName)).thenReturn(requestParams);
        
        // 执行测试
        boolean result = node.execute(context);
        
        // 验证结果
        assertFalse(result);
        verify(context).markFailure(ResultCode.PARAM_ERROR.getCode(), "参数 jsonData JSON格式错误");
    }
}