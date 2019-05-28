package com.quant.common.domain.response;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 18:21
 */

public class OrdersDetailResponse<T> {

    /**
     * status : ok
     * data : {"id":59378,"symbol":"ethusdt","account-id":100009,"amount":"10.1000000000","price":"100.1000000000","created-at":1494901162595,"type":"buy-limit","field-amount":"10.1000000000","field-cash-amount":"1011.0100000000","field-fees":"0.0202000000","finished-at":1494901400468,"user-id":1000,"source":"api","state":"filled","canceled-at":0,"exchange":"huobi","batch":""}
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

    public static class DataBean {

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
