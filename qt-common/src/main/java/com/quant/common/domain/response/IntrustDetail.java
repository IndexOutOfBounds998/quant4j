package com.quant.common.domain.response;

import com.google.gson.annotations.SerializedName;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 19:20
 */

public class IntrustDetail {

    /**
     * id : 59378
     * symbol : ethusdt
     * account-id : 100009
     * amount : 10.1000000000
     * price : 100.1000000000
     * created-at : 1494901162595
     * type : buy-limit
     * field-amount : 10.1000000000
     * field-cash-amount : 1011.0100000000
     * field-fees : 0.0202000000
     * finished-at : 1494901400468
     * user-id : 1000
     * source : api
     * state : filled
     * canceled-at : 0
     * exchange : huobi
     * batch :
     */

    private int id;
    private String symbol;
    @SerializedName("account-id")
    private int accountid;
    private String amount;
    private String price;
    @SerializedName("created-at")
    private long createdat;
    private String type;
    @SerializedName("field-amount")
    private String fieldamount;
    @SerializedName("field-cash-amount")
    private String fieldcashamount;
    @SerializedName("field-fees")
    private String fieldfees;
    @SerializedName("finished-at")
    private long finishedat;
    @SerializedName("user-id")
    private int userid;
    private String source;
    private String state;
    @SerializedName("canceled-at")
    private int canceledat;
    private String exchange;
    private String batch;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getAccountid() {
        return accountid;
    }

    public void setAccountid(int accountid) {
        this.accountid = accountid;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public long getCreatedat() {
        return createdat;
    }

    public void setCreatedat(long createdat) {
        this.createdat = createdat;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFieldamount() {
        return fieldamount;
    }

    public void setFieldamount(String fieldamount) {
        this.fieldamount = fieldamount;
    }

    public String getFieldcashamount() {
        return fieldcashamount;
    }

    public void setFieldcashamount(String fieldcashamount) {
        this.fieldcashamount = fieldcashamount;
    }

    public String getFieldfees() {
        return fieldfees;
    }

    public void setFieldfees(String fieldfees) {
        this.fieldfees = fieldfees;
    }

    public long getFinishedat() {
        return finishedat;
    }

    public void setFinishedat(long finishedat) {
        this.finishedat = finishedat;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getCanceledat() {
        return canceledat;
    }

    public void setCanceledat(int canceledat) {
        this.canceledat = canceledat;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }
}
