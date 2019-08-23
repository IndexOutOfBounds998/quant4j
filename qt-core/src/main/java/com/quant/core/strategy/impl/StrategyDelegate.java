package com.quant.core.strategy.impl;

import com.quant.common.enums.HBOrderType;
import com.quant.common.enums.OrderType;
import com.quant.core.trading.TradingApi;

import java.math.BigDecimal;

/**
 * StrategyDelegate
 * Created by yang on 2019/8/23.
 */
public interface StrategyDelegate {

    /**
     * 计算盈利
     */
    void CalculateProfit();

    /**
     * 下单
     *
     * @param tradingApi
     * @param sellAmount
     * @param sellPrice
     * @param HBOrderType
     * @param type
     */
    void orderPlace(TradingApi tradingApi, BigDecimal sellAmount, BigDecimal sellPrice, HBOrderType HBOrderType, OrderType type);
}
