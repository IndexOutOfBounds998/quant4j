package com.quant.common.response;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 14:38
 */

public class DepthResponse<T> {


    /**
     * status : ok
     * ch : market.btcusdt.depth.step1
     * ts : 1489472598812
     * tick : {"id":"1489464585407","ts":"1489464585407","bids":[[7964,0.0678],[7963,0.9162]],"asks":[[7979,0.0736],[8020,13.6584]]}
     */

    private String status;
    private String ch;
    private String ts;
    public String errCode;
    public String errMsg;

    /**
     * tick 说明:
     * "tick": {
     * "id": 消息id,
     * "ts": 消息生成时间，单位：毫秒,
     * "bids": 买盘,[price(成交价), amount(成交量)], 按price降序,
     * "asks": 卖盘,[price(成交价), amount(成交量)], 按price升序
     * }
     */
    private Depth tick;


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

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public Depth getTick() {
        return tick;
    }

    public void setTick(Depth tick) {
        this.tick = tick;
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
