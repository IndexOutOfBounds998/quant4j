package com.quant.common.exception;

/**
 * @author yang
 * @desc IndicatorException
 * @date 2019/5/28
 */

public class IndicatorException extends RuntimeException {

    final String errCode;

    public IndicatorException(String errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode;
    }

    public IndicatorException(Exception e) {
        super(e);
        this.errCode = e.getClass().getName();
    }

    public String getErrCode() {
        return this.errCode;
    }

}
