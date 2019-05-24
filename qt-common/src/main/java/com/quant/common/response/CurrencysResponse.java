package com.quant.common.response;

import java.util.List;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 15:46
 */

public class CurrencysResponse {


    /**
     * status : ok
     * data : ["usdt","eth","etc"]
     */

    private String status;
    public String errCode;
    public String errMsg;
    private List<String> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
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
