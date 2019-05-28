package com.quant.common.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProfitMessage {


    long sellOrderId;
    long buyOrderId;
    int robot_id;

    BigDecimal buyPrice;
    BigDecimal sellPrice;

    BigDecimal buyAmount;
    BigDecimal sellAmount;

    BigDecimal buyCashAmount;
    BigDecimal sellCashAmount;
    //是否盈利
    int isProfit;

    BigDecimal diff;

    BigDecimal divide;

}
