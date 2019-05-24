package com.quant.core.response;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 15:22
 */

public class HistoryTradeResponse<T> {


    /**
     * status : ok
     * ch : market.ethusdt.trade.detail
     * ts : 1502448925216
     * data : [{"id":31459998,"ts":1502448920106,"data":[{"id":17592256642623,"amount":0.04,"price":1997,"direction":"buy","ts":1502448920106}]}]
     */

    private String status;
    private String ch;
    private long ts;
    public String errCode;
    public String errMsg;
    private T data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCh() {
        return ch;
    }

    public void setCh(String ch) {
        this.ch = ch;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
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
