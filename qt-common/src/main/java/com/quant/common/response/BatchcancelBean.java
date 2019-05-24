package com.quant.common.response;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 17:53
 */

public class BatchcancelBean {
    /**
     * err-msg : 记录无效
     * order-id : 2
     * err-code : base-record-invalid
     */

    private String errmsg;
    private String orderid;
    private String errcode;

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getErrcode() {
        return errcode;
    }

    public void setErrcode(String errcode) {
        this.errcode = errcode;
    }
}
