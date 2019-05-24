package com.quant.core.enums;

public enum OrderType {
    /**
     * 限价买入
     */
    BUY_LIMIT("buy-limit"),
    /**
     * 限价卖出
     */
    SELL_LIMIT("sell-limit"),

    /**
     * 市价买入
     */
    BUY_MARKET("buy-market"),
    /**
     * 市价卖出
     */
    SELL_MARKET("sell-market");

    String tyoe;

    OrderType(String tyoe) {
        this.tyoe = tyoe;
    }

    public String getTyoe() {
        return tyoe;
    }
}
