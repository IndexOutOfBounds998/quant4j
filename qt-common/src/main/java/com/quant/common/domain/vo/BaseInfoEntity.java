package com.quant.common.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BaseInfoEntity {
    /**
     * buyPrice : 1.0E-8
     * sleep : 3
     * strategyName :
     * buyAmount :
     * buyAllWeights : 1
     * sellAmount :
     * isAllBuy : true
     * sellPrice : 1.0E-8
     * isAllSell : true
     * isLimitPrice : true
     * sellAllWeights : 1
     */
    private BigDecimal buyPrice;
    private double sleep;
    private String strategyName;
    private BigDecimal buyAmount;
    private int buyAllWeights;
    private BigDecimal sellAmount;
    private int isAllBuy;
    private BigDecimal sellPrice;
    private BigDecimal buyQuotaPrice;
    private int isAllSell;
    private int isLimitPrice;
    private int sellAllWeights;
    private int profit;

}