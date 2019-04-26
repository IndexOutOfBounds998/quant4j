package com.qklx.qt.core.response;

import lombok.Data;

/**
 * @Author cyy
 * @Date 2018/1/14
 * @Time 18:22
 */
@Data
public class OrdersDetail {

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

    private String id;
    private String symbol;
    private String accountId;
    private String amount;
    private String price;
    private long createdAt;
    private String type;
    private String fieldAmount;
    private String fieldCashAmount;
    private String fieldFees;
    private long finishedAt;
    private String userId;
    private String source;
    private String state;
    private long canceleAt;
    private String exchange;
    private String batch;
}
