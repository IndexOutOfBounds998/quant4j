
package com.quant.core.indicatorAdapter;


import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.AwesomeOscillatorIndicator;
import org.ta4j.core.indicators.helpers.MedianPriceIndicator;


/**
 * cci指标计算
 */
public class AoIndicatorAdapter extends IndicatorAdapter {


    public AoIndicatorAdapter(TimeSeries timeSeries, Integer barCount, Integer barCount2) {
        super(timeSeries, barCount, barCount2);
    }

    /**
     * 计算并返回Indicator
     *
     * @return
     */
    @Override
    public Indicator indicatorCalculation() {

        if (barCount != null && barCount2 == null) {
            return new AwesomeOscillatorIndicator(new MedianPriceIndicator(timeSeries), barCount, 34);
        } else if (barCount != null) {
            return new AwesomeOscillatorIndicator(new MedianPriceIndicator(timeSeries), barCount, barCount2);
        } else {
            return new AwesomeOscillatorIndicator(timeSeries);
        }

    }
}

