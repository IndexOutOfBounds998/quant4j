package com.quant.core.indicatorAdapter;

import com.quant.common.domain.to.BuyAndSellIndicatorTo;
import com.quant.common.domain.vo.IndicatorCalParam;
import com.quant.common.exception.IndicatorException;
import com.quant.core.factory.IndicatorFactory;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;

import static com.quant.common.constans.IndicatorCons.*;

/**
 * Created by yang on 2019/5/23.
 * <p>
 * <p>
 * <p>
 * 指标计算
 */
public abstract class IndicatorAdapter {

    protected TimeSeries timeSeries;

    protected BuyAndSellIndicatorTo.SourceBean sourceBean;

    protected Integer barCount;

    protected Integer barCount2;


    public IndicatorAdapter(TimeSeries timeSeries, int barCount, BuyAndSellIndicatorTo.SourceBean sourceBean) {
        this.timeSeries = timeSeries;
        this.barCount = barCount;
        this.sourceBean = sourceBean;
    }

    public IndicatorAdapter(TimeSeries timeSeries, int barCount) {
        this.timeSeries = timeSeries;
        this.barCount = barCount;
    }

    public IndicatorAdapter(TimeSeries timeSeries, int barCount, int barCount2, BuyAndSellIndicatorTo.SourceBean sourceBean) {
        this.timeSeries = timeSeries;
        this.barCount = barCount;
        this.barCount2 = barCount2;
        this.sourceBean = sourceBean;
    }

    public IndicatorAdapter(TimeSeries timeSeries, int barCount, int barCount2) {
        this.timeSeries = timeSeries;
        this.barCount = barCount;
        this.barCount2 = barCount2;
    }

    /**
     * 获取指标从souce；
     *
     * @return
     */
    Indicator defaultIndicatorFromSource() throws IndicatorException {
        final Indicator indicator;

        if (sourceBean == null) {
            return new ClosePriceIndicator(timeSeries);
        }
        if (sourceBean.getValue() == null) {
            //不是指标策略
            indicator = defaultIndicator(sourceBean.getSource());
        } else {
            indicator = defaultIndicator(sourceBean.getValue());
        }
        if (indicator == null) {
            //指标对象
            String indicatorName = sourceBean.getValue();
            String source = sourceBean.getSource();
            String[] split = sourceBean.getParams().split(",");
            Indicator ind = defaultIndicator(source);
            Integer day = null;
            try {
                day = Integer.parseInt(split[0]);
            } catch (NumberFormatException e) {
                throw new IndicatorException(e);
            }
            Integer day2 = null;
            try {
                day2 = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                throw new IndicatorException(e);
            }
            if (ind == null) {
                throw new IndicatorException(new IllegalArgumentException("不允许多重指标来源！！！"));
            }
            if (indicatorName.equals(RSI)) {
                return new RSIIndicator(ind, day);
            } else if (indicatorName.equals(ADX)) {
                return new ADXIndicator(timeSeries, day);
            } else if (indicatorName.equals(SMA)) {
                return new SMAIndicator(ind, day);
            } else if (indicatorName.equals(EMA)) {
                return new EMAIndicator(ind, day);
            } else if (indicatorName.equals(CCI)) {
                return new CCIIndicator(timeSeries, day);
            } else if (indicatorName.equals(AO)) {
                return new AwesomeOscillatorIndicator(ind, day, day2);
            } else if (indicatorName.equals(STOCHK)) {
                return new StochasticOscillatorKIndicator(timeSeries, day);
            } else if (indicatorName.equals(STOCHD)) {
                return new StochasticOscillatorDIndicator(ind);
            } else if (indicatorName.equals(MACD)) {
                return new MACDIndicator(ind, day, day2);
            }

        }
        return indicator;
    }


    Indicator defaultIndicator(String source) {
        Indicator indicator = null;
        if (source.equals(CLOSE)) {
            indicator = new ClosePriceIndicator(timeSeries);
        } else if (source.equals(OPEN)) {
            indicator = new OpenPriceIndicator(timeSeries);
        } else if (source.equals(LOW)) {
            indicator = new LowPriceIndicator(timeSeries);
        } else if (source.equals(HIGHT)) {
            indicator = new HighPriceIndicator(timeSeries);
        }
        return indicator;
    }

    public abstract Indicator indicatorCalculation();
}
