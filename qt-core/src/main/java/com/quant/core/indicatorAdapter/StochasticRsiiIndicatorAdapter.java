package com.quant.core.indicatorAdapter;

import com.quant.common.domain.to.BuyAndSellIndicatorTo;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.StochasticRSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

/**
 * Created by yang on 2019/5/30.
 */
public class StochasticRsiiIndicatorAdapter extends IndicatorAdapter {


    public StochasticRsiiIndicatorAdapter(TimeSeries timeSeries, int barCount, int barCount2, BuyAndSellIndicatorTo.SourceBean sourceBean) {
        super(timeSeries, barCount, barCount2,sourceBean);
    }


    @Override
    public Indicator indicatorCalculation() {
        final Indicator indicator =defaultIndicatorFromSource();
        RSIIndicator r = new RSIIndicator(indicator, barCount);
        return new StochasticRSIIndicator(r, barCount2);
    }
}
