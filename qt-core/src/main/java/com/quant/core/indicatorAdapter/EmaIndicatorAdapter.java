
package com.quant.core.indicatorAdapter;


import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;


/**
 * ema指标计算
 */
public class EmaIndicatorAdapter extends IndicatorAdapter {


    public EmaIndicatorAdapter(TimeSeries timeSeries, int barCount) {
        super(timeSeries, barCount);
    }

    /**
     * 计算并返回Indicator
     *
     * @return
     */
    @Override
    public Indicator strategCalculation() {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        return new EMAIndicator(closePrice, barCount);
    }
}

