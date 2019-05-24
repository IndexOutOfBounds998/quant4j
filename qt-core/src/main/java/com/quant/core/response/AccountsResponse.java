package com.quant.core.response;

import com.quant.core.api.ApiException;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 16:01
 */

public class AccountsResponse<T> {

    /**
     * status : ok
     * data : [{"id":100009,"type":"spot","state":"working","user-id":1000}]
     */

    private String status;
    public String errCode;
    public String errMsg;
    private T data;

    public T checkAndReturn() {
        if ("ok".equals(status)) {
            return data;
        }
        throw new ApiException(errCode, errMsg);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
