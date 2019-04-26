package com.qklx.qt.core.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProfitMessage {


    long sellOrderId;
    long buyOrderId;

    BigDecimal buyPrice;
    BigDecimal sellPrice;

    BigDecimal buyAmount;
    BigDecimal sellAmount;
    //是否盈利
    int isProfit;

    BigDecimal diff;

    BigDecimal divide;

}
