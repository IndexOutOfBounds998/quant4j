
package com.qklx.qt.core.strategy.impl;


import com.qklx.qt.core.strategy.StrategyCalculation;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;


/**
 * Rsi指标计算
 */
public class RsiStrategyImpl implements StrategyCalculation {

    private TimeSeries timeSeries;

    private int barCount;

    public RsiStrategyImpl(TimeSeries timeSeries, int barCount) {
        this.timeSeries = timeSeries;
        this.barCount = barCount;
    }

    /**
     * 计算并返回Indicator
     *
     * @return
     */
    @Override
    public Indicator strategCalculation() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        return new RSIIndicator(closePrice, barCount);
    }
}

