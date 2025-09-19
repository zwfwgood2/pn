-- 初始化脚本：省级节点系统初始化数据
-- 创建时间：2024-10-09
-- 版本：1.0

-- -----------------------------
-- 表：sys_access_organization（接入机构表）
-- -----------------------------
INSERT INTO `sys_access_organization` (`org_name`, `org_code`, `username`, `password`, `app_key`, `public_key`, `private_key`, `status`, `structure_type`, `contact_person`, `contact_phone`, `description`)
VALUES 
  -- 省级节点记录（appKey使用self）
  ('省级节点管理中心', 'PROVINCE_NODE', 'province_admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'self', "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEMFoUMX2vVVOhrG8tEMA4W82MZ5o0fbNnnG6SSNmT/saVGWX/bJUqKkBqsCjo0h4SXqP4iXk8R1FGO3VXd6+q4w==", "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgmaJlZwRi8z/EQYwz18RC/aQ4x+SCdB1e+LvQQgl+wK+gCgYIKoEcz1UBgi2hRANCAAQwWhQxfa9VU6Gsby0QwDhbzYxnmjR9s2ecbpJI2ZP+xpUZZf9slSoqQGqwKOjSHhJeo/iJeTxHUUY7dVd3r6rj", 1, 0, '系统管理员', '13800138001', '省级节点管理机构'),
  -- 市级节点记录
  ('市级节点接入中心', 'CITY_NODE_001', 'city_admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'city_node_001_key', "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAELebssrdL4mRPhsThlO/2PPehqWVC1vMwNgnCia0SIrh2FSRzikm0k6wpoi1ofVNNWNDMq06mBPjBbcChgB+DYw==", "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgp8o7mIIi7QbIO0cwjJIRDQfgToJjQ5kTsoXSzDMODeSgCgYIKoEcz1UBgi2hRANCAAQt5uyyt0viZE+GxOGU7/Y896GpZULW8zA2CcKJrRIiuHYVJHOKSbSTrCmiLWh9U01Y0MyrTqYE+MFtwKGAH4Nj", 1, 1, '市级管理员', '13900139001', '市级节点接入机构');

-- -----------------------------
-- 表：sys_interface_definition（接口定义表）
-- -----------------------------
INSERT INTO `sys_interface_definition` (`interface_code`, `interface_name`, `interface_type`, `request_method`, `request_path`, `process_code`, `status`, `description`)
VALUES 
  -- 读接口：行政许可信息查询
  ('CREDIT_INQUIRY_ADMIN_LICENSE', '行政许可信息查询', 0, 'POST', 'creditInquiry/administrativeLicensing', 'PROCESS_CREDIT_INQUIRY', 1, '查询企业或个人的行政许可信息'),
  -- 写接口：金融机构信息上传
  ('UPLOAD_FINANCIAL_INSTITUTIONS', '金融机构信息上传', 1, 'POST', 'upload/financialInstitutions', 'PROCESS_UPLOAD_FINANCIAL', 1, '上传金融机构的基本信息和运营数据');

-- -----------------------------
-- 表：sys_process_definition（流程定义表）
-- -----------------------------
INSERT INTO `sys_process_definition` (`process_code`, `process_name`, `interface_code`, `status`, `description`)
VALUES 
  -- 读接口流程
  ('PROCESS_CREDIT_INQUIRY', '行政许可信息查询流程', 'CREDIT_INQUIRY_ADMIN_LICENSE', 1, '处理行政许可信息查询请求的流程'),
  -- 写接口流程
  ('PROCESS_UPLOAD_FINANCIAL', '金融机构信息上传流程', 'UPLOAD_FINANCIAL_INSTITUTIONS', 1, '处理金融机构信息上传请求的流程');

-- -----------------------------
-- 表：sys_process_node_config（流程节点配置表）
-- -----------------------------

-- 读接口流程节点配置（顺序：tokenValidateNode、paramValidateNode、logRecordNode、nationalNodeRequestNode、verifyNode、signatureNode）
INSERT INTO `sys_process_node_config` (`process_code`, `node_id`, `node_name`, `node_order`, `retry_config`, `async_execution`, `node_config`, `status`)
VALUES 
  -- token验证节点
  ('PROCESS_CREDIT_INQUIRY', 'tokenValidateNode', '令牌验证节点', 1, '{
    "maxRetryCount": 3,
    "initialDelay": 1000,
    "multiplier": 2.0
  }', 0, ' {
    "inParamName": "requestParams",
    "inParamType": "map",
    "outParamName": "authorizationResult",
    "outParamType": "boolean"
  }', 1),
  -- 参数验证节点
  ('PROCESS_CREDIT_INQUIRY', 'paramValidateNode', '参数验证节点', 2, '{
    "maxRetryCount": 1,
    "initialDelay": 500,
    "multiplier": 1.0
  }', 0, '{
    "inParamName": "requestParams",
    "inParamType": "map",
    "outParamName": "validationResult",
    "outParamType": "boolean",
    "validateRules": [
      {
        "paramName": "orgCode",
        "rules": [
          {"type": "required", "config": {"message": "机构代码不能为空"}},
          {"type": "stringLength", "config": {"min": 4, "max": 20, "message": "机构代码长度必须在4-20位之间"}}
        ]
      },
      {
        "paramName": "userName",
        "rules": [
          {"type": "required", "config": {"message": "用户名不能为空"}}
        ]
      }
    ]
  }', 1),
  -- 日志记录节点
  ('PROCESS_CREDIT_INQUIRY', 'logRecordNode', '日志记录节点', 3, '', 1, '{
    "inParamName": "requestParams",
    "inParamType": "map",
    "outParamName": "nationalNodeResponse",
    "outParamType": "jsonObject"
  }', 1),
  -- 全国节点请求节点
  ('PROCESS_CREDIT_INQUIRY', 'nationalNodeRequestNode', '全国节点请求节点', 4, '{
    "maxRetryCount": 5,
    "initialDelay": 2000,
    "multiplier": 1.5
  }', 0, '{
    "inParamName": "requestParams",
    "inParamType": "map",
    "outParamName": "nationalNodeResponse",
    "outParamType": "jsonObject"
  }', 1),
  -- 数据验证节点
  ('PROCESS_CREDIT_INQUIRY', 'verifyNode', '数据验证节点', 5, '', 0, '{
    "inParamName": "requestParams",
    "inParamType": "map",
    "outParamName": "signatureValidationResult",
    "outParamType": "boolean",
    "side": "city"  // 可选值：national（全国节点）、provincial（省级节点）、city（市级节点）
  }', 1),
  -- 签名验证节点
  ('PROCESS_CREDIT_INQUIRY', 'signatureNode', '签名验证节点', 6, '', 0, '{
    "inParamName": "responseData",
    "inParamType": "map",
    "outParamName": "signedData",
    "outParamType": "map",
    "side": "provincial"  // 可选值：national（全国节点）、provincial（省级节点）、city（市级节点）
  }', 1);

-- 写接口流程节点配置（顺序：tokenValidateNode、verifyNode、paramValidateNode、logRecordNode、signatureNode、nationalNodeRequestNode）
INSERT INTO `sys_process_node_config` (`process_code`, `node_id`, `node_name`, `node_order`, `retry_config`, `async_execution`, `node_config`, `status`)
VALUES 
  -- token验证节点
  ('PROCESS_UPLOAD_FINANCIAL', 'tokenValidateNode', '令牌验证节点', 1, '{
    "maxRetryCount": 3,
    "initialDelay": 1000,
    "multiplier": 2.0
  }', 0, ' {
    "inParamName": "requestParams",
    "inParamType": "map",
    "outParamName": "authorizationResult",
    "outParamType": "boolean"
  }', 1),
  -- 数据验证节点
  ('PROCESS_UPLOAD_FINANCIAL', 'verifyNode', '数据验证节点', 5, '', 0, '{
    "inParamName": "requestParams",
    "inParamType": "map",
    "outParamName": "signatureValidationResult",
    "outParamType": "boolean",
    "side": "city"  // 可选值：national（全国节点）、provincial（省级节点）、city（市级节点）
  }', 1),
  -- 参数验证节点
  ('PROCESS_UPLOAD_FINANCIAL', 'paramValidateNode', '参数验证节点', 2, '{
    "maxRetryCount": 1,
    "initialDelay": 500,
    "multiplier": 1.0
  }', 0, '{
    "inParamName": "requestParams",
    "inParamType": "map",
    "outParamName": "validationResult",
    "outParamType": "boolean",
    "validateRules": [
      {
        "paramName": "orgCode",
        "rules": [
          {"type": "required", "config": {"message": "机构代码不能为空"}},
          {"type": "stringLength", "config": {"min": 4, "max": 20, "message": "机构代码长度必须在4-20位之间"}}
        ]
      },
      {
        "paramName": "userName",
        "rules": [
          {"type": "required", "config": {"message": "用户名不能为空"}}
        ]
      }
    ]
  }', 1),
  -- 日志记录节点
  ('PROCESS_UPLOAD_FINANCIAL','logRecordNode', '日志记录节点', 3, '', 1, '{
    "inParamName": "requestParams",
    "inParamType": "map",
    "outParamName": "nationalNodeResponse",
    "outParamType": "jsonObject"
  }', 1),
  -- 签名验证节点
  ('PROCESS_UPLOAD_FINANCIAL', 'signatureNode', '签名验证节点', 6, '', 0, '{
    "inParamName": "responseData",
    "inParamType": "map",
    "outParamName": "signedData",
    "outParamType": "map",
    "side": "provincial"  // 可选值：national（全国节点）、provincial（省级节点）、city（市级节点）
  }', 1),
  -- 全国节点请求节点
  ('PROCESS_UPLOAD_FINANCIAL', 'nationalNodeRequestNode', '全国节点请求节点', 4, '{
    "maxRetryCount": 5,
    "initialDelay": 2000,
    "multiplier": 1.5
  }', 0, '{
    "inParamName": "requestParams",
    "inParamType": "map",
    "outParamName": "nationalNodeResponse",
    "outParamType": "jsonObject"
  }', 1);