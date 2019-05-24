package com.quant.common.response;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 15:39
 */

public class SymbolsResponse<T> {


    /**
     * status : ok
     * ch : market.btcusdt.detail
     * ts : 1489473538996
     * tick : {"amount":4316.4346,"open":8090.54,"close":7962.62,"high":8119,"ts":1489464451000,"id":1489464451,"count":9595,"low":7875,"vol":3.449727690576E7}
     */

    private String status;
    private String ch;
    private long ts;
    public String errCode;
    public String errMsg;
    private T tick;

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

    public T getTick() {
        return tick;
    }

    public void setTick(T tick) {
        this.tick = tick;
    }
}
