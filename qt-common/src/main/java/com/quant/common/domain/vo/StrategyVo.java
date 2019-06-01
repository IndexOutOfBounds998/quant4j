package com.quant.common.domain.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

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
    private Setting6Entity setting6;


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

        private List<BuyStrategyBean> buyStrategy;
        private List<SellStrategyBean> sellStrategy;

        @Data
        public static class BuyStrategyBean {
            /**
             * id : 1
             * buyKlineOption : 1
             * buyKline : 1min
             * buyWeights : 3
             * buyPercent : 2
             */

            private int id;
            @JSONField(name = "buyKlineOption")
            private String buyKlineOption;
            @JSONField(name = "buyKline")
            private String buyKline;
            @JSONField(name = "buyWeights")
            private int buyWeights;
            @JSONField(name = "buyPercent")
            private String buyPercent;
        }

        @Data
        public static class SellStrategyBean {
            /**
             * id : 1
             * sellKlineOption : 1
             * sellKline : 5min
             * sellWeights : 5
             * sellPercent : 4
             */

            private int id;
            @JSONField(name = "sellKlineOption")
            private String sellKlineOption;
            @JSONField(name = "sellKline")
            private String sellKline;
            @JSONField(name = "sellWeights")
            private int sellWeights;
            @JSONField(name = "sellPercent")
            private String sellPercent;

        }
    }

    /**
     * "setting6":{"isAble":true,"takeProfit":0.0001,"stopLoss":0.0002}}
     */
    @Data
    public static class Setting6Entity {
        private int isAble;
        private BigDecimal takeProfit;
        private BigDecimal stopLoss;
    }
}
