package com.qklx.qt.core.response;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 18:48
 */

public class MatchresultsOrdersDetailResponse<T> {


    /**
     * status : ok
     * data : [{"id":29553,"order-id":59378,"match-id":59335,"symbol":"ethusdt","type":"buy-limit","source":"api","price":"100.1000000000","filled-amount":"9.1155000000","filled-fees":"0.0182310000","created-at":1494901400435}]
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
