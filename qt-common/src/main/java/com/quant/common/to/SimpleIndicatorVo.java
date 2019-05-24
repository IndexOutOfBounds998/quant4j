package com.quant.common.to;

/**
 * Created by yang on 2019/5/24.
 */
public class SimpleIndicatorVo {


    /**
     * kline : 1min
     * size :
     * symbol :
     * indicatorBuy : {"indicator":"","count":"14","value":""}
     * indicatorSell : {"indicator":"","count":"14","value":""}
     */

    private String kline;
    private String size;
    private String symbol;
    private IndicatorBuyBean indicatorBuy;
    private IndicatorSellBean indicatorSell;

    public String getKline() {
        return kline;
    }

    public void setKline(String kline) {
        this.kline = kline;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public IndicatorBuyBean getIndicatorBuy() {
        return indicatorBuy;
    }

    public void setIndicatorBuy(IndicatorBuyBean indicatorBuy) {
        this.indicatorBuy = indicatorBuy;
    }

    public IndicatorSellBean getIndicatorSell() {
        return indicatorSell;
    }

    public void setIndicatorSell(IndicatorSellBean indicatorSell) {
        this.indicatorSell = indicatorSell;
    }

    public static class IndicatorBuyBean {
        /**
         * indicator :
         * count : 14
         * value :
         */

        private String indicator;
        private String count;
        private String value;

        public String getIndicator() {
            return indicator;
        }

        public void setIndicator(String indicator) {
            this.indicator = indicator;
        }

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class IndicatorSellBean {
        /**
         * indicator :
         * count : 14
         * value :
         */

        private String indicator;
        private String count;
        private String value;

        public String getIndicator() {
            return indicator;
        }

        public void setIndicator(String indicator) {
            this.indicator = indicator;
        }

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
