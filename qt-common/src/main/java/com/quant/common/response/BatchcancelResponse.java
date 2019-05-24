package com.quant.common.response;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 17:51
 */

public class BatchcancelResponse<T> {


    /**
     * status : ok
     * data : {"success":["1","3"],"failed":[{"err-msg":"记录无效","order-id":"2","err-code":"base-record-invalid"}]}
     */

    private String status;
    public String errCode;
    public String errMsg;
    private T data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
