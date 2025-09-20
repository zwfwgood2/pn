package com.example.provincialnode.exception;

public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String code;
    private String msg;
    public BusinessException(String code, String msg) {
        super();
        this.code = code;
        this.msg = msg;
    }

    public BusinessException(String msg) {
        super(msg);
        this.code = "500";
        this.msg = msg;
    }

}
