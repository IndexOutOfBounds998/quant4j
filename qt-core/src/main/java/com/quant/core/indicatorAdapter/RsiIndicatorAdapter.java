
package com.quant.core.indicatorAdapter;


import com.quant.common.domain.to.BuyAndSellIndicatorTo;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.RSIIndicator;


/**
 * Rsi指标计算
 */
public class RsiIndicatorAdapter extends IndicatorAdapter {


    public RsiIndicatorAdapter(TimeSeries timeSeries, int barCount, BuyAndSellIndicatorTo.SourceBean sourceBean) {
        super(timeSeries, barCount, sourceBean);
    }

    /**
     * 计算并返回Indicator
     *
     * @return
     */
    @Override
    public Indicator indicatorCalculation() {
        final Indicator indicator =defaultIndicatorFromSource();
        return new RSIIndicator(indicator, barCount);
    }
}

