package com.quant.core.indicator;

import org.ta4j.core.TimeSeries;

/**
 * Created by yang on 2019/5/23.
 * <p>
 * <p>
 * <p>
 * 指标计算
 */
public abstract class IndicatorCat {

    protected TimeSeries timeSeries;

    protected int barCount;

    public IndicatorCat(TimeSeries timeSeries, int barCount) {
        this.timeSeries = timeSeries;
        this.barCount = barCount;
    }

   public abstract org.ta4j.core.Indicator strategCalculation();
}
