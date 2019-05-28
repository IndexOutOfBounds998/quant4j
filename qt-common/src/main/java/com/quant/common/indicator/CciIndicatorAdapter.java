
package com.quant.common.indicator;


import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;


/**
 * cci指标计算
 */
public class CciIndicatorAdapter extends IndicatorAdapter {


    public CciIndicatorAdapter(TimeSeries timeSeries, int barCount) {
        super(timeSeries, barCount);
    }

    /**
     * 计算并返回Indicator
     *
     * @return
     */
    @Override
    public org.ta4j.core.Indicator strategCalculation() {
        return new CCIIndicator(timeSeries, barCount);
    }
}

