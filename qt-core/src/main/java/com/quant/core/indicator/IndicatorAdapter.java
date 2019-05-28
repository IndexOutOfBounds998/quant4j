package com.quant.core.indicator;

import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;

/**
 * Created by yang on 2019/5/23.
 * <p>
 * <p>
 * <p>
 * 指标计算
 */
public abstract class IndicatorAdapter {

    protected TimeSeries timeSeries;

    protected Integer barCount;
    protected Integer barCount2;
    public IndicatorAdapter(TimeSeries timeSeries, int barCount) {
        this.timeSeries = timeSeries;
        this.barCount = barCount;
    }
    public IndicatorAdapter(TimeSeries timeSeries, int barCount,int barCount2) {
        this.timeSeries = timeSeries;
        this.barCount = barCount;
        this.barCount2 = barCount2;
    }

   public abstract Indicator strategCalculation();
}
