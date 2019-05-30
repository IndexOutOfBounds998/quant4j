package com.quant.core.indicatorAdapter;

import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;

/**
 * Created by yang on 2019/5/30.
 */
public class StochasticKiIndicatorAdapter extends IndicatorAdapter {


    public StochasticKiIndicatorAdapter(TimeSeries timeSeries, int barCount) {
        super(timeSeries, barCount);
    }


    @Override
    public Indicator indicatorCalculation() {
        return new StochasticOscillatorKIndicator(timeSeries, barCount);
    }
}
