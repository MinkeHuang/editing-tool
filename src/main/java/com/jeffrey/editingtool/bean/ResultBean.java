package com.jeffrey.editingtool.bean;

import lombok.Data;

/**
 * @author jeffreydou
 */
@Data
public class ResultBean {

    private String msg;
    private int code;
    private String verifyCode;
    private Object bizResult;
    private String traceId;
    private String errorMsg;

    public ResultBean(int code, Object bizResult, String msg) {
        this.msg = msg;
        this.code = code;
        this.bizResult = bizResult;
    }

    public ResultBean(int code, String msg) {
        this.msg = msg;
        this.code = code;
    }
}