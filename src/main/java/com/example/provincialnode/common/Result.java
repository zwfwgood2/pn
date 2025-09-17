package com.example.provincialnode.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 通用响应结果类
 * 统一接口返回格式
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功返回
     * @param data 数据
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功返回（无数据）
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    /**
     * 失败返回
     * @param code 错误码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> error(String code, String message) {
        return new Result<>(code, message);
    }

    /**
     * 失败返回
     * @param resultCode 结果码枚举
     * @param <T> 数据类型
     * @return 响应结果
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage());
    }

}