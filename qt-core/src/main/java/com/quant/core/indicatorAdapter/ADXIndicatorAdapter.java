package com.quant.core.indicatorAdapter;

import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.adx.ADXIndicator;

/**
 * 计算adx指标
 * Created by yang on 2019/5/26.
 */
public class ADXIndicatorAdapter extends IndicatorAdapter {

    public ADXIndicatorAdapter(TimeSeries timeSeries, int barCount) {
        super(timeSeries, barCount);
    }

    @Override


    public Indicator strategCalculation() {
        ADXIndicator adxIndicator = new ADXIndicator(timeSeries, barCount);
        return adxIndicator;
    }
}
