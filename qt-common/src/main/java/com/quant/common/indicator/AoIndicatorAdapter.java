
package com.quant.common.indicator;


import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.AroonOscillatorIndicator;
import org.ta4j.core.indicators.AwesomeOscillatorIndicator;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
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
    public org.ta4j.core.Indicator strategCalculation() {

        if (barCount != null && barCount2 == null) {
            return new AwesomeOscillatorIndicator(new MedianPriceIndicator(timeSeries), barCount, 34);
        } else if (barCount != null && barCount2 != null) {
            return new AwesomeOscillatorIndicator(new MedianPriceIndicator(timeSeries), barCount, barCount2);
        } else {
            return new AwesomeOscillatorIndicator(timeSeries);
        }

    }
}

