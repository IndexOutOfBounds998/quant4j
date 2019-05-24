package com.quant.core.strategy;

import com.quant.core.strategy.impl.HuoBiStrategyImpl;
import com.quant.core.trading.OrderType;
import com.quant.core.vo.StrategyVo;

/**
 * 策略基类
 */
public abstract class AbstractStrategy {
    protected HuoBiStrategyImpl.OrderState orderState;
    protected StrategyVo.BaseInfoEntity baseInfo;

    /**
     * 权重计算
     */
    protected void weightsCalculation() {

        if ((this.orderState.getType() == OrderType.SELL || this.orderState.getType() == null) &&
                this.baseInfo.getBuyAllWeights() != 0) {
            buyCalculation();
        }

        if (this.orderState.getType() == OrderType.BUY
                && this.baseInfo.getSellAllWeights() != 0) {
            sellCalculation();
        }

    }

    /**
     * 买入权重计算
     */
    protected abstract void buyCalculation();

    /**
     * 卖出权重计算
     */
    protected abstract void sellCalculation();

    public static class Weights {
        private volatile Integer buyTotal = 0;
        private volatile Integer sellTotal = 0;


        public Integer getBuyTotal() {
            return buyTotal;
        }

        public Integer getSellTotal() {
            return sellTotal;
        }

        public void AddBuyTotal(Integer buyTotal) {
            this.buyTotal += buyTotal;
        }

        public void AddSellTotal(Integer sellTotal) {
            this.sellTotal += sellTotal;
        }

        public void reSet() {
            this.buyTotal = 0;
            this.sellTotal = 0;
        }

    }

}
