package com.qklx.qt.core.response;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 18:50
 */

public class MatchresultsOrdersDetail {
    /**
     * id : 29553
     * order-id : 59378
     * match-id : 59335
     * symbol : ethusdt
     * type : buy-limit
     * source : api
     * price : 100.1000000000
     * filled-amount : 9.1155000000
     * filled-fees : 0.0182310000
     * created-at : 1494901400435
     */

    private int id;
    @com.google.gson.annotations.SerializedName("order-id")
    private int orderid;
    @com.google.gson.annotations.SerializedName("match-id")
    private int matchid;
    private String symbol;
    private String type;
    private String source;
    private String price;
    @com.google.gson.annotations.SerializedName("filled-amount")
    private String filledamount;
    @com.google.gson.annotations.SerializedName("filled-fees")
    private String filledfees;
    @com.google.gson.annotations.SerializedName("created-at")
    private long createdat;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderid() {
        return orderid;
    }

    public void setOrderid(int orderid) {
        this.orderid = orderid;
    }

    public int getMatchid() {
        return matchid;
    }

    public void setMatchid(int matchid) {
        this.matchid = matchid;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getFilledamount() {
        return filledamount;
    }

    public void setFilledamount(String filledamount) {
        this.filledamount = filledamount;
    }

    public String getFilledfees() {
        return filledfees;
    }

    public void setFilledfees(String filledfees) {
        this.filledfees = filledfees;
    }

    public long getCreatedat() {
        return createdat;
    }

    public void setCreatedat(long createdat) {
        this.createdat = createdat;
    }
}
