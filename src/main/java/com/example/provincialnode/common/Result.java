package com.example.provincialnode.common;

import lombok.Data;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 给市级别节点的通用响应结果类
 * 统一接口返回格式
 */
@Data
public class Result implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String C_API_STATUS="C-API-Status";
    private static final String C_RESPONSE_CODE="C-Response-Code";
    private static final String C_RESPONSE_BODY="C-Response-Body";
    private static final String C_RESPONSE_DESC="C-Response-Desc";

    private Map<String,Object> result = new LinkedHashMap<>(5);

    /**
     * 成功返回
     * @param data 数据
     * @return 响应结果
     */
    public static Result success(Map<String,Object> data) {
        Result result = new Result();
        Map<String,Object> cityNodeResponse = new LinkedHashMap<>(4);
        cityNodeResponse.put("C-API-Status", (boolean) data.get("success") ? "00":"01");
        cityNodeResponse.put("C-Response-Code",data.get("errorCode"));
        cityNodeResponse.put("C-Response-Desc",data.get("errorMessage"));
        Map<String,Object> body =null ;
        if((boolean) data.get("success")){
            body = new LinkedHashMap<>(7);
            body.put("txnCommCom",null);
            body.put("fileCom",null);
            body.put("timestamp",data.get("timestamp"));
            body.put("key",data.get("key"));
            body.put("signatureData",data.get("signatureData"));
            body.put("data",data.get("data"));
        }
        cityNodeResponse.put("C-Response-Body",body);
        result.setResult(cityNodeResponse);
        return result;
    }

    /**
     * 成功返回（无数据）
     * @return 响应结果
     */
    public static Result success() {
        Result result = new Result();
        Map<String,Object> map = new LinkedHashMap<>(2);
        map.put(C_API_STATUS, "00");
        map.put(C_RESPONSE_CODE,ResultCode.SUCCESS.getCode());
        map.put(C_RESPONSE_DESC,ResultCode.SUCCESS.getMessage());
        result.setResult(map);
        return result;
    }

    /**
     * 失败返回
     * @param code 错误码
     * @param message 错误消息
     * @return 响应结果
     */
    public static Result error(String code, String message) {
        Result result = new Result();
        Map<String,Object> map = new LinkedHashMap<>(2);
        map.put(C_API_STATUS, "01");
        map.put(C_RESPONSE_CODE,code);
        map.put(C_RESPONSE_DESC,message);
        result.setResult(map);
        return result;
    }

    /**
     * 失败返回
     * @param resultCode 结果码枚举
     * @return 响应结果
     */
    public static Result error(ResultCode resultCode) {
        Result result = new Result();
        Map<String,Object> map = new LinkedHashMap<>(2);
        map.put(C_API_STATUS, "01");
        map.put(C_RESPONSE_CODE,resultCode.getCode());
        map.put(C_RESPONSE_DESC,resultCode.getMessage());
        result.setResult(map);
        return result;
    }

    public String getCode(){
        return (String) this.getResult().get(C_RESPONSE_CODE);

    }

    public String getMessage(){
        return (String) this.getResult().get(C_RESPONSE_DESC);
    }

    public Map<String,Object> getBody(){
        return this.result;
    }
}