
package com.quant.core.indicatorAdapter;


import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.CCIIndicator;


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
    public Indicator indicatorCalculation() {
        return new CCIIndicator(timeSeries, barCount);
    }
}

