package com.quant.core.indicatorAdapter;

import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

/**
 * 计算madc指标
 * Created by yang on 2019/5/26.
 */
public class MacdIndicatorAdapter extends IndicatorAdapter {

    public MacdIndicatorAdapter(TimeSeries timeSeries, int barCount) {
        super(timeSeries, barCount);
    }

    @Override
    public Indicator indicatorCalculation() {
        ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(timeSeries);
        if (barCount != null && barCount2 == null) {
            return new MACDIndicator(closePriceIndicator, barCount, 26);
        } else if (barCount != null) {
            return new MACDIndicator(closePriceIndicator, barCount, barCount2);
        }
        return new MACDIndicator(closePriceIndicator);
    }
}
