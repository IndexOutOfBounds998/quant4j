package com.qklx.qt.core.strategy;

import com.qklx.qt.core.trading.MarketOrder;

import java.math.BigDecimal;

/**
 * 策略基类
 */
public abstract class AbstractStrategy {
    protected MarketOrder marketOrder;


    protected void executeor() {
        executeSetting1(marketOrder);
        executeSetting2(marketOrder);
        executeSetting3(marketOrder);
        executeSetting4(marketOrder);
        executeSetting5();
    }

    protected abstract void executeSetting5();

    protected abstract void executeSetting4(MarketOrder marketOrder);

    protected abstract void executeSetting3(MarketOrder marketOrder);

    /**
     * 只返回最新的出售订单的价格
     *
     * @param marketOrder
     * @return
     */
    protected abstract void executeSetting2(MarketOrder marketOrder);

    /**
     * 只返回最新的购买订单的价格
     *
     * @param marketOrder
     * @return
     */
    protected abstract void executeSetting1(MarketOrder marketOrder);


}
