-- 初始化接口定义表
sys_interface_definition
INSERT INTO `sys_interface_definition` (`interface_code`, `interface_name`, `interface_type`, `request_method`, `request_path`, `process_code`, `status`, `description`) VALUES
('GET_TOKEN_API', '获取Token接口', 0, 'POST', '/api/getToken', 'GET_TOKEN_PROCESS', 1, '用于获取访问全国节点的令牌'),
('UPDATE_USER_API', '更新用户接口', 1, 'POST', '/api/updateUser', 'UPDATE_USER_PROCESS', 1, '用于更新全国节点密码'),
('GET_PUBLIC_KEY_API', '获取公钥接口', 0, 'POST', '/api/publicKey', 'GET_PUBLIC_KEY_PROCESS', 1, '用于获取全国节点公钥');

-- 初始化流程定义表
sys_process_definition
INSERT INTO `sys_process_definition` (`process_code`, `process_name`, `interface_code`, `status`, `description`) VALUES
('GET_TOKEN_PROCESS', '获取Token流程', 'GET_TOKEN_API', 1, '获取Token的处理流程'),
('UPDATE_USER_PROCESS', '更新用户流程', 'UPDATE_USER_API', 1, '更新全国节点密码流程'),
('GET_PUBLIC_KEY_PROCESS', '获取公钥流程', 'GET_PUBLIC_KEY_API', 1, '获取全国节点公钥的处理流程');

-- 初始化流程节点配置表
sys_process_node_config

-- GET_TOKEN_PROCESS流程节点配置
-- logRecordNode节点
INSERT INTO `sys_process_node_config` (`process_code`, `node_id`, `node_name`, `node_order`, `retry_config`, `async_execution`, `node_config`, `status`) VALUES
('GET_TOKEN_PROCESS', 'logRecordNode', '日志记录节点', 1, '{"maxRetryCount": 3, "initialDelay": 1000, "multiplier": 2.0}', 0, '{"inParamName": "requestParams", "inParamType": "map", "outParamName": "logRecordResult", "outParamType": "boolean"}', 1),
-- signatureNode节点
('GET_TOKEN_PROCESS', 'signatureNode', '签名节点', 2, '{"maxRetryCount": 3, "initialDelay": 1000, "multiplier": 2.0}', 0, '{"inParamName": "requestParams", "inParamType": "map", "outParamName": "signedData", "outParamType": "map", "side": "provincial"}', 1),
-- nationalNodeRequestNode节点
('GET_TOKEN_PROCESS', 'nationalNodeRequestNode', '全国节点请求节点', 3, '{"maxRetryCount": 5, "initialDelay": 2000, "multiplier": 1.5}', 0, '{"inParamName": "requestParams", "inParamType": "map", "outParamName": "nationalNodeResponse", "outParamType": "jsonObject"}', 1),
-- verifyNode节点
('GET_TOKEN_PROCESS', 'verifyNode', '验证节点', 4, '{"maxRetryCount": 3, "initialDelay": 1000, "multiplier": 2.0}', 0, '{"inParamName": "nationalNodeResponse", "inParamType": "jsonObject", "outParamName": "verifyResult", "outParamType": "boolean"}', 1);

-- UPDATE_USER_PROCESS流程节点配置
-- logRecordNode节点
INSERT INTO `sys_process_node_config` (`process_code`, `node_id`, `node_name`, `node_order`, `retry_config`, `async_execution`, `node_config`, `status`) VALUES
('UPDATE_USER_PROCESS', 'logRecordNode', '日志记录节点', 1, '{"maxRetryCount": 3, "initialDelay": 1000, "multiplier": 2.0}', 0, '{"inParamName": "requestParams", "inParamType": "map", "outParamName": "logRecordResult", "outParamType": "boolean"}', 1),
-- signatureNode节点
('UPDATE_USER_PROCESS', 'signatureNode', '签名节点', 2, '{"maxRetryCount": 3, "initialDelay": 1000, "multiplier": 2.0}', 0, '{"inParamName": "requestParams", "inParamType": "map", "outParamName": "signedData", "outParamType": "map", "side": "provincial"}', 1),
-- nationalNodeRequestNode节点
('UPDATE_USER_PROCESS', 'nationalNodeRequestNode', '全国节点请求节点', 3, '{"maxRetryCount": 5, "initialDelay": 2000, "multiplier": 1.5}', 0, '{"inParamName": "requestParams", "inParamType": "map", "outParamName": "nationalNodeResponse", "outParamType": "jsonObject"}', 1),
-- verifyNode节点
('UPDATE_USER_PROCESS', 'verifyNode', '验证节点', 4, '{"maxRetryCount": 3, "initialDelay": 1000, "multiplier": 2.0}', 0, '{"inParamName": "nationalNodeResponse", "inParamType": "jsonObject", "outParamName": "verifyResult", "outParamType": "boolean"}', 1);

-- GET_PUBLIC_KEY_PROCESS流程节点配置
-- logRecordNode节点
INSERT INTO `sys_process_node_config` (`process_code`, `node_id`, `node_name`, `node_order`, `retry_config`, `async_execution`, `node_config`, `status`) VALUES
('GET_PUBLIC_KEY_PROCESS', 'logRecordNode', '日志记录节点', 1, '{"maxRetryCount": 3, "initialDelay": 1000, "multiplier": 2.0}', 0, '{"inParamName": "requestParams", "inParamType": "map", "outParamName": "logRecordResult", "outParamType": "boolean"}', 1),
-- nationalNodeRequestNode节点
('GET_PUBLIC_KEY_PROCESS', 'nationalNodeRequestNode', '全国节点请求节点', 2, '{"maxRetryCount": 5, "initialDelay": 2000, "multiplier": 1.5}', 0, '{"inParamName": "requestParams", "inParamType": "map", "outParamName": "nationalNodeResponse", "outParamType": "jsonObject"}', 1),
-- verifyNode节点
('GET_PUBLIC_KEY_PROCESS', 'verifyNode', '验证节点', 3, '{"maxRetryCount": 3, "initialDelay": 1000, "multiplier": 2.0}', 0, '{"inParamName": "nationalNodeResponse", "inParamType": "jsonObject", "outParamName": "verifyResult", "outParamType": "boolean"}', 1);