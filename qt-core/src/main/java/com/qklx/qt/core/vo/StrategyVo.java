package com.qklx.qt.core.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StrategyVo {
    /**
     * baseInfo : {"buyPrice":1.0E-8,"sleep":3,"strategyName":"","buyAmount":"","buyAllWeights":1,"sellAmount":"","isAllBuy":true,"sellPrice":1.0E-8,"isAllSell":true,"isLimitPrice":true,"sellAllWeights":1}
     * setting2 : {"weights":1,"sellOrderUsdt":1.0E-8,"buyOrderUsdt":1.0E-8}
     * setting3 : {"sellDownSecond":1,"sellDownPercent":1,"buyDownPercent":1,"buyDownSecond":1,"weights":1}
     * setting1 : {"sellOrdersUsdt":1.0E-8,"buyOrdersUsdt":1.0E-8,"weights":1}
     * setting4 : {"sellUpSecond":1,"buyUpSecond":1,"weights":1,"buyUpPercent":1,"sellUpPercent":1}
     * setting5 : {"kline":"1min","weights":1,"klineOption":"1"}
     */
    private Integer id;
    private BaseInfoEntity baseInfo;
    private Setting1Entity setting1;
    private Setting2Entity setting2;
    private Setting3Entity setting3;
    private Setting4Entity setting4;
    private Setting5Entity setting5;


    @Data
    public static class BaseInfoEntity {
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
        private int sleep;
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

    @Data
    public static class Setting2Entity {

        /**
         * weights : 1
         * sellOrderUsdt : 1.0E-8
         * buyOrderUsdt : 1.0E-8
         */
        private int buyWeights;
        private int sellWeights;
        private BigDecimal sellOrderUsdt;
        private BigDecimal buyOrderUsdt;

    }

    @Data
    public static class Setting3Entity {
        /**
         * sellDownSecond : 1
         * sellDownPercent : 1
         * buyDownPercent : 1
         * buyDownSecond : 1
         * weights : 1
         */
        private int sellDownSecond;
        private double sellDownPercent;
        private double buyDownPercent;
        private int buyDownSecond;
        private int sellWeights;
        private int buyWeights;

    }

    @Data
    public static class Setting1Entity {
        /**
         * sellOrdersUsdt : 1.0E-8
         * buyOrdersUsdt : 1.0E-8
         * weights : 1
         */
        private BigDecimal sellOrdersUsdt;
        private BigDecimal buyOrdersUsdt;
        private int sellWeights;
        private int buyWeights;
    }

    @Data
    public static class Setting4Entity {


        /**
         * sellUpSecond : 1
         * buyUpSecond : 1
         * weights : 1
         * buyUpPercent : 1
         * sellUpPercent : 1
         */
        private int sellUpSecond;
        private int buyUpSecond;
        private int buyWeights;
        private int sellWeights;
        private double buyUpPercent;
        private double sellUpPercent;

    }

    @Data
    public static class Setting5Entity {

        /**
         * buyKline : 1min
         * buyWeights : 1
         * sellKline : 1min
         * buyKlineOption : 1
         * sellWeights : 1
         * sellKlineOption : 1
         */
        private String buyKline;
        private int buyWeights;
        private String sellKline;
        private String buyKlineOption;
        private int sellWeights;
        private String sellKlineOption;
        private double buyPercent;
        private double sellPercent;

    }
}
