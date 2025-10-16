-- 初始化接口定义数据
INSERT INTO `sys_interface_definition` (`interface_code`, `interface_name`, `interface_type`, `request_method`, `request_path`, `process_code`, `status`, `description`) VALUES
('CREDIT_REAL_ESTATE_CHECK', '房产核验接口', 3, 'POST', '/api/creditInquiry/realEstateCheck', 'PROCESS_REAL_ESTATE_CHECK', 1, '信用查询-房产核验接口'),
('CREDIT_MARRIAGE_CHECK', '婚姻核验接口', 3, 'POST', '/api/creditInquiry/marriageCheck', 'PROCESS_MARRIAGE_CHECK', 1, '信用查询-婚姻核验接口'),
('UPLOAD_ENTERPRISES_INFO', '企业信息上传接口', 1, 'POST', '/api/upload/enterprisesInfo', 'PROCESS_ENTERPRISES_INFO', 1, '企业信息上传接口'),
('UPLOAD_FINANCING_REQUIREMENTS', '融资需求上传接口', 1, 'POST', '/api/upload/financingRequirements', 'PROCESS_FINANCING_REQUIREMENTS', 1, '融资需求上传接口'),
('UPLOAD_FINANCIAL_INSTITUTIONS', '金融机构上传接口', 1, 'POST', '/api/v1/upload/financialInstitutions', 'PROCESS_FINANCIAL_INSTITUTIONS', 1, '金融机构上传接口'),
('UPLOAD_FINANCIAL_PRODUCTS', '金融产品上传接口', 1, 'POST', '/api/upload/financialProducts', 'PROCESS_FINANCIAL_PRODUCTS', 1, '金融产品上传接口'),
('UPLOAD_POLICIES', '政策上传接口', 1, 'POST', '/api/upload/policies', 'PROCESS_POLICIES', 1, '政策上传接口'),
('UPLOAD_CREDIT_SERVICE_AGENCIES', '信用服务机构上传接口', 1, 'POST', '/api/upload/creditServiceAgencies', 'PROCESS_CREDIT_SERVICE_AGENCIES', 1, '信用服务机构上传接口');

-- 初始化流程定义数据
INSERT INTO `sys_process_definition` (`process_code`, `process_name`, `interface_code`, `status`, `description`) VALUES
('PROCESS_REAL_ESTATE_CHECK', '房产核验流程', 'CREDIT_REAL_ESTATE_CHECK', 1, '房产核验接口处理流程'),
('PROCESS_MARRIAGE_CHECK', '婚姻核验流程', 'CREDIT_MARRIAGE_CHECK', 1, '婚姻核验接口处理流程'),
('PROCESS_ENTERPRISES_INFO', '企业信息上传流程', 'UPLOAD_ENTERPRISES_INFO', 1, '企业信息上传接口处理流程'),
('PROCESS_FINANCING_REQUIREMENTS', '融资需求上传流程', 'UPLOAD_FINANCING_REQUIREMENTS', 1, '融资需求上传接口处理流程'),
('PROCESS_FINANCIAL_INSTITUTIONS', '金融机构上传流程', 'UPLOAD_FINANCIAL_INSTITUTIONS', 1, '金融机构上传接口处理流程'),
('PROCESS_FINANCIAL_PRODUCTS', '金融产品上传流程', 'UPLOAD_FINANCIAL_PRODUCTS', 1, '金融产品上传接口处理流程'),
('PROCESS_POLICIES', '政策上传流程', 'UPLOAD_POLICIES', 1, '政策上传接口处理流程'),
('PROCESS_CREDIT_SERVICE_AGENCIES', '信用服务机构上传流程', 'UPLOAD_CREDIT_SERVICE_AGENCIES', 1, '信用服务机构上传接口处理流程');

-- 公共节点配置的重试配置
SET @retry_config = '{"maxRetryCount": 3, "initialDelay": 1000, "multiplier": 2.0}';

-- 1. 房产核验接口节点配置
INSERT INTO `sys_process_node_config` (`process_code`, `node_id`, `node_name`, `node_order`, `retry_config`, `async_execution`, `node_config`, `status`) VALUES
-- tokenValidateNode
('PROCESS_REAL_ESTATE_CHECK', 'tokenValidateNode', 'Token验证节点', 1, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "tokenValidateResult"}', 1),
-- verifyNode (city side)
('PROCESS_REAL_ESTATE_CHECK', 'verifyNode1', '验签节点(市级)', 2, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "verifyResult1", "side": "city", "dataSource": "requestData"}', 1),
-- paramValidateNode
('PROCESS_REAL_ESTATE_CHECK', 'paramValidateNode', '参数验证节点', 3, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "paramValidateResult", "validateRules": "[{\"paramName\": \"idCard\", \"rules\": [{\"type\": \"required\", \"config\": true}, {\"type\": \"regex\", \"config\": \"^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$\"}]}, {\"paramName\": \"name\", \"rules\": [{\"type\": \"required\", \"config\": true}]}]"}', 1),
-- logRecordNode
('PROCESS_REAL_ESTATE_CHECK', 'logRecordNode', '日志记录节点', 4, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "logRecordResult"}', 1),
-- signatureNode (provincial side)
('PROCESS_REAL_ESTATE_CHECK', 'signatureNode1', '签名节点(省级)', 5, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "signedData1", "side": "provincial", "dataSource": "requestData"}', 1),
-- mockNationalNodeRequestNode
('PROCESS_REAL_ESTATE_CHECK', 'mockNationalNodeRequestNode', '模拟全国节点请求节点', 6, @retry_config, 0, '{"inParamName": "signedData1", "outParamName": "nationalResponse", "interfaceCode": "CREDIT_REAL_ESTATE_CHECK"}', 1),
-- verifyNode (national side)
('PROCESS_REAL_ESTATE_CHECK', 'verifyNode2', '验签节点(全国)', 7, @retry_config, 0, '{"inParamName": "nationalResponse", "outParamName": "verifyResult2", "side": "national", "dataSource": "data"}', 1),
-- signatureNode (provincial side for response)
('PROCESS_REAL_ESTATE_CHECK', 'signatureNode2', '签名节点(响应)', 8, @retry_config, 0, '{"inParamName": "nationalResponse", "outParamName": "signedData2", "side": "provincial", "dataSource": "data"}', 1);

-- 2. 婚姻核验接口节点配置
INSERT INTO `sys_process_node_config` (`process_code`, `node_id`, `node_name`, `node_order`, `retry_config`, `async_execution`, `node_config`, `status`) VALUES
-- tokenValidateNode
('PROCESS_MARRIAGE_CHECK', 'tokenValidateNode', 'Token验证节点', 1, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "tokenValidateResult"}', 1),
-- verifyNode (city side)
('PROCESS_MARRIAGE_CHECK', 'verifyNode1', '验签节点(市级)', 2, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "verifyResult1", "side": "city", "dataSource": "requestData"}', 1),
-- paramValidateNode
('PROCESS_MARRIAGE_CHECK', 'paramValidateNode', '参数验证节点', 3, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "paramValidateResult", "validateRules": "[{\"paramName\": \"idCard\", \"rules\": [{\"type\": \"required\", \"config\": true}, {\"type\": \"regex\", \"config\": \"^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$\"}]}, {\"paramName\": \"name\", \"rules\": [{\"type\": \"required\", \"config\": true}]}]"}', 1),
-- logRecordNode
('PROCESS_MARRIAGE_CHECK', 'logRecordNode', '日志记录节点', 4, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "logRecordResult"}', 1),
-- signatureNode (provincial side)
('PROCESS_MARRIAGE_CHECK', 'signatureNode1', '签名节点(省级)', 5, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "signedData1", "side": "provincial", "dataSource": "requestData"}', 1),
-- mockNationalNodeRequestNode
('PROCESS_MARRIAGE_CHECK', 'mockNationalNodeRequestNode', '模拟全国节点请求节点', 6, @retry_config, 0, '{"inParamName": "signedData1", "outParamName": "nationalResponse", "interfaceCode": "CREDIT_MARRIAGE_CHECK"}', 1),
-- verifyNode (national side)
('PROCESS_MARRIAGE_CHECK', 'verifyNode2', '验签节点(全国)', 7, @retry_config, 0, '{"inParamName": "nationalResponse", "outParamName": "verifyResult2", "side": "national", "dataSource": "data"}', 1),
-- signatureNode (provincial side for response)
('PROCESS_MARRIAGE_CHECK', 'signatureNode2', '签名节点(响应)', 8, @retry_config, 0, '{"inParamName": "nationalResponse", "outParamName": "signedData2", "side": "provincial", "dataSource": "data"}', 1);

-- 3. 企业信息上传接口节点配置
INSERT INTO `sys_process_node_config` (`process_code`, `node_id`, `node_name`, `node_order`, `retry_config`, `async_execution`, `node_config`, `status`) VALUES
-- tokenValidateNode
('PROCESS_ENTERPRISES_INFO', 'tokenValidateNode', 'Token验证节点', 1, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "tokenValidateResult"}', 1),
-- verifyNode (city side)
('PROCESS_ENTERPRISES_INFO', 'verifyNode', '验签节点', 2, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "verifyResult", "side": "city", "dataSource": "requestData"}', 1),
-- paramValidateNode
('PROCESS_ENTERPRISES_INFO', 'paramValidateNode', '参数验证节点', 3, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "paramValidateResult", "validateRules": "[{\"paramName\": \"enterpriseName\", \"rules\": [{\"type\": \"required\", \"config\": true}]}, {\"paramName\": \"unifiedSocialCreditCode\", \"rules\": [{\"type\": \"required\", \"config\": true}]}]"}', 1),
-- logRecordNode
('PROCESS_ENTERPRISES_INFO', 'logRecordNode', '日志记录节点', 4, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "logRecordResult"}', 1),
-- signatureNode
('PROCESS_ENTERPRISES_INFO', 'signatureNode', '签名节点', 5, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "signedData", "side": "provincial", "dataSource": "requestData"}', 1),
-- mockNationalNodeRequestNode
('PROCESS_ENTERPRISES_INFO', 'mockNationalNodeRequestNode', '模拟全国节点请求节点', 6, @retry_config, 0, '{"inParamName": "signedData", "outParamName": "nationalResponse", "interfaceCode": "UPLOAD_ENTERPRISES_INFO"}', 1);

-- 4. 融资需求上传接口节点配置
INSERT INTO `sys_process_node_config` (`process_code`, `node_id`, `node_name`, `node_order`, `retry_config`, `async_execution`, `node_config`, `status`) VALUES
-- tokenValidateNode
('PROCESS_FINANCING_REQUIREMENTS', 'tokenValidateNode', 'Token验证节点', 1, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "tokenValidateResult"}', 1),
-- verifyNode (city side)
('PROCESS_FINANCING_REQUIREMENTS', 'verifyNode', '验签节点', 2, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "verifyResult", "side": "city", "dataSource": "requestData"}', 1),
-- paramValidateNode
('PROCESS_FINANCING_REQUIREMENTS', 'paramValidateNode', '参数验证节点', 3, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "paramValidateResult", "validateRules": "[{\"paramName\": \"enterpriseId\", \"rules\": [{\"type\": \"required\", \"config\": true}]}, {\"paramName\": \"financingAmount\", \"rules\": [{\"type\": \"required\", \"config\": true}, {\"type\": \"dataType\", \"config\": \"number\"}]}]"}', 1),
-- logRecordNode
('PROCESS_FINANCING_REQUIREMENTS', 'logRecordNode', '日志记录节点', 4, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "logRecordResult"}', 1),
-- signatureNode
('PROCESS_FINANCING_REQUIREMENTS', 'signatureNode', '签名节点', 5, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "signedData", "side": "provincial", "dataSource": "requestData"}', 1),
-- mockNationalNodeRequestNode
('PROCESS_FINANCING_REQUIREMENTS', 'mockNationalNodeRequestNode', '模拟全国节点请求节点', 6, @retry_config, 0, '{"inParamName": "signedData", "outParamName": "nationalResponse", "interfaceCode": "UPLOAD_FINANCING_REQUIREMENTS"}', 1);

-- 5. 金融机构上传接口节点配置
INSERT INTO `sys_process_node_config` (`process_code`, `node_id`, `node_name`, `node_order`, `retry_config`, `async_execution`, `node_config`, `status`) VALUES
-- tokenValidateNode
('PROCESS_FINANCIAL_INSTITUTIONS', 'tokenValidateNode', 'Token验证节点', 1, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "tokenValidateResult"}', 1),
-- verifyNode (city side)
('PROCESS_FINANCIAL_INSTITUTIONS', 'verifyNode', '验签节点', 2, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "verifyResult", "side": "city", "dataSource": "requestData"}', 1),
-- paramValidateNode
('PROCESS_FINANCIAL_INSTITUTIONS', 'paramValidateNode', '参数验证节点', 3, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "paramValidateResult", "validateRules": "[{\"paramName\": \"institutionName\", \"rules\": [{\"type\": \"required\", \"config\": true}]}, {\"paramName\": \"institutionType\", \"rules\": [{\"type\": \"required\", \"config\": true}]}]"}', 1),
-- logRecordNode
('PROCESS_FINANCIAL_INSTITUTIONS', 'logRecordNode', '日志记录节点', 4, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "logRecordResult"}', 1),
-- signatureNode
('PROCESS_FINANCIAL_INSTITUTIONS', 'signatureNode', '签名节点', 5, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "signedData", "side": "provincial", "dataSource": "requestData"}', 1),
-- mockNationalNodeRequestNode
('PROCESS_FINANCIAL_INSTITUTIONS', 'mockNationalNodeRequestNode', '模拟全国节点请求节点', 6, @retry_config, 0, '{"inParamName": "signedData", "outParamName": "nationalResponse", "interfaceCode": "UPLOAD_FINANCIAL_INSTITUTIONS"}', 1);

-- 6. 金融产品上传接口节点配置
INSERT INTO `sys_process_node_config` (`process_code`, `node_id`, `node_name`, `node_order`, `retry_config`, `async_execution`, `node_config`, `status`) VALUES
-- tokenValidateNode
('PROCESS_FINANCIAL_PRODUCTS', 'tokenValidateNode', 'Token验证节点', 1, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "tokenValidateResult"}', 1),
-- verifyNode (city side)
('PROCESS_FINANCIAL_PRODUCTS', 'verifyNode', '验签节点', 2, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "verifyResult", "side": "city", "dataSource": "requestData"}', 1),
-- paramValidateNode
('PROCESS_FINANCIAL_PRODUCTS', 'paramValidateNode', '参数验证节点', 3, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "paramValidateResult", "validateRules": "[{\"paramName\": \"productName\", \"rules\": [{\"type\": \"required\", \"config\": true}]}, {\"paramName\": \"institutionId\", \"rules\": [{\"type\": \"required\", \"config\": true}]}]"}', 1),
-- logRecordNode
('PROCESS_FINANCIAL_PRODUCTS', 'logRecordNode', '日志记录节点', 4, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "logRecordResult"}', 1),
-- signatureNode
('PROCESS_FINANCIAL_PRODUCTS', 'signatureNode', '签名节点', 5, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "signedData", "side": "provincial", "dataSource": "requestData"}', 1),
-- mockNationalNodeRequestNode
('PROCESS_FINANCIAL_PRODUCTS', 'mockNationalNodeRequestNode', '模拟全国节点请求节点', 6, @retry_config, 0, '{"inParamName": "signedData", "outParamName": "nationalResponse", "interfaceCode": "UPLOAD_FINANCIAL_PRODUCTS"}', 1);

-- 7. 政策上传接口节点配置
INSERT INTO `sys_process_node_config` (`process_code`, `node_id`, `node_name`, `node_order`, `retry_config`, `async_execution`, `node_config`, `status`) VALUES
-- tokenValidateNode
('PROCESS_POLICIES', 'tokenValidateNode', 'Token验证节点', 1, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "tokenValidateResult"}', 1),
-- verifyNode (city side)
('PROCESS_POLICIES', 'verifyNode', '验签节点', 2, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "verifyResult", "side": "city", "dataSource": "requestData"}', 1),
-- paramValidateNode
('PROCESS_POLICIES', 'paramValidateNode', '参数验证节点', 3, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "paramValidateResult", "validateRules": "[{\"paramName\": \"policyName\", \"rules\": [{\"type\": \"required\", \"config\": true}]}, {\"paramName\": \"issueDate\", \"rules\": [{\"type\": \"required\", \"config\": true}]}]"}', 1),
-- logRecordNode
('PROCESS_POLICIES', 'logRecordNode', '日志记录节点', 4, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "logRecordResult"}', 1),
-- signatureNode
('PROCESS_POLICIES', 'signatureNode', '签名节点', 5, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "signedData", "side": "provincial", "dataSource": "requestData"}', 1),
-- mockNationalNodeRequestNode
('PROCESS_POLICIES', 'mockNationalNodeRequestNode', '模拟全国节点请求节点', 6, @retry_config, 0, '{"inParamName": "signedData", "outParamName": "nationalResponse", "interfaceCode": "UPLOAD_POLICIES"}', 1);

-- 8. 信用服务机构上传接口节点配置
INSERT INTO `sys_process_node_config` (`process_code`, `node_id`, `node_name`, `node_order`, `retry_config`, `async_execution`, `node_config`, `status`) VALUES
-- tokenValidateNode
('PROCESS_CREDIT_SERVICE_AGENCIES', 'tokenValidateNode', 'Token验证节点', 1, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "tokenValidateResult"}', 1),
-- verifyNode (city side)
('PROCESS_CREDIT_SERVICE_AGENCIES', 'verifyNode', '验签节点', 2, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "verifyResult", "side": "city", "dataSource": "requestData"}', 1),
-- paramValidateNode
('PROCESS_CREDIT_SERVICE_AGENCIES', 'paramValidateNode', '参数验证节点', 3, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "paramValidateResult", "validateRules": "[{\"paramName\": \"agencyName\", \"rules\": [{\"type\": \"required\", \"config\": true}]}, {\"paramName\": \"businessScope\", \"rules\": [{\"type\": \"required\", \"config\": true}]}]"}', 1),
-- logRecordNode
('PROCESS_CREDIT_SERVICE_AGENCIES', 'logRecordNode', '日志记录节点', 4, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "logRecordResult"}', 1),
-- signatureNode
('PROCESS_CREDIT_SERVICE_AGENCIES', 'signatureNode', '签名节点', 5, @retry_config, 0, '{"inParamName": "requestData", "outParamName": "signedData", "side": "provincial", "dataSource": "requestData"}', 1),
-- mockNationalNodeRequestNode
('PROCESS_CREDIT_SERVICE_AGENCIES', 'mockNationalNodeRequestNode', '模拟全国节点请求节点', 6, @retry_config, 0, '{"inParamName": "signedData", "outParamName": "nationalResponse", "interfaceCode": "UPLOAD_CREDIT_SERVICE_AGENCIES"}', 1);