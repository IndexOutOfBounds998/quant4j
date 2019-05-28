package com.quant.common.domain.to;

import lombok.Data;

/**
 * Created by yang on 2019/5/24.
 */
@Data
public class SimpleIndicatorVo {
    /**
     * kline : 1min
     * size :
     * symbol :
     * indicatorBuy : {"indicatorAdapter":"","count":"14","value":""}
     * indicatorSell : {"indicatorAdapter":"","count":"14","value":""}
     */
    private Double stopGain;
    private Double stopLoss;
    private String kline;
    private String size;
    private String symbol;
    private IndicatorBuyBean indicatorBuy;
    private IndicatorSellBean indicatorSell;

    @Data
    public static class IndicatorBuyBean {
        /**
         * indicatorAdapter :
         * count : 14
         * value :
         */

        private String indicator;
        private String count;
        private String value;

    }

    @Data
    public static class IndicatorSellBean {
        /**
         * indicatorAdapter :
         * count : 14
         * value :
         */

        private String indicator;
        private String count;
        private String value;

    }
}
