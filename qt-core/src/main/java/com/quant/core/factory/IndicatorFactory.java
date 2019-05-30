package com.quant.core.factory;

import com.quant.common.domain.to.BuyAndSellIndicatorTo;
import com.quant.core.indicatorAdapter.*;
import com.quant.common.domain.vo.IndicatorCalParam;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;

import static com.quant.common.constans.IndicatorCons.*;

/**
 * 静态工厂 生成指标
 * Created by yang on 2019/5/26.
 */
public class IndicatorFactory extends AbsIndicatorFactory {


    public IndicatorFactory(TimeSeries timeSeries) {
        super(timeSeries);
    }


    @Override
    public Indicator getIndicator(String indicatorName) {
        if (indicatorName.equals(RSI)) {
            //产生rsi 指标
            return getRSI(timeSeries, null, null);
        }
        if (indicatorName.equals(ADX)) {
            return getADX(timeSeries, null, null);
        }
        if (indicatorName.equals(PRICE)) {
            return getPriceIndicator(timeSeries);
        }
        if (indicatorName.equals(VOLUME)) {
            return getAmount(timeSeries);
        }
        if (indicatorName.equals(SMA)) {
            return getSma(timeSeries, null, null);
        }
        if (indicatorName.equals(EMA)) {
            return getEma(timeSeries, null, null);
        }
        if (indicatorName.equals(CCI)) {
            return getCCI(timeSeries, 20);
        }
        if (indicatorName.equals(AO)) {
            return getAo(timeSeries, 5, 34);
        }
        if (indicatorName.equals(MACD)) {
            return getMacd(timeSeries, 12, 26, null);
        }
        if (indicatorName.equals(STOCHK)) {
            return getStochK(timeSeries, 3);
        }
        if (indicatorName.equals(STOCHD)) {
            return getStochD(timeSeries, 3);
        }
        return null;
    }


    @Override
    public Indicator getIndicator(IndicatorCalParam indicatorCalParam) {
        int length = indicatorCalParam.getParams().length;
        Integer day = null;
        try {
            if (length >= 1) {
                day = Integer.parseInt(indicatorCalParam.getParams()[0]);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        Integer day2 = null;
        try {
            if (length >= 2) {
                day2 = Integer.parseInt(indicatorCalParam.getParams()[1]);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (indicatorCalParam.getIndicatorName().equals(RSI)) {
            return getRSI(timeSeries, day, indicatorCalParam.getSourceBean());
        } else if (indicatorCalParam.getIndicatorName().equals(ADX)) {
            return getADX(timeSeries, day, indicatorCalParam.getSourceBean());
        } else if (indicatorCalParam.getIndicatorName().equals(PRICE)) {
            return getPriceIndicator(timeSeries);
        } else if (indicatorCalParam.getIndicatorName().equals(VOLUME)) {
            return getAmount(timeSeries);
        } else if (indicatorCalParam.getIndicatorName().equals(SMA)) {
            return getSma(timeSeries, day, indicatorCalParam.getSourceBean());
        } else if (indicatorCalParam.getIndicatorName().equals(EMA)) {
            return getEma(timeSeries, day, indicatorCalParam.getSourceBean());
        } else if (indicatorCalParam.getIndicatorName().equals(CCI)) {
            return getCCI(timeSeries, day);
        } else if (indicatorCalParam.getIndicatorName().equals(AO)) {
            return getAo(timeSeries, day, day2);
        } else if (indicatorCalParam.getIndicatorName().equals(STOCHK)) {
            return getStochK(timeSeries, day);
        } else if (indicatorCalParam.getIndicatorName().equals(STOCHD)) {
            return getStochD(timeSeries, day);
        } else if (indicatorCalParam.getIndicatorName().equals(MACD)) {
            return getMacd(timeSeries, day, day2, indicatorCalParam.getSourceBean());
        }
        return null;
    }

    @Override
    public void updateTimeSeries(TimeSeries timeSeries) {
        if (timeSeries == null) {
            throw new IllegalArgumentException("timeSeries must be not null");
        }
        this.timeSeries = timeSeries;
    }

    private Indicator getAmount(TimeSeries timeSeries) {
        return new VolumeIndicator(timeSeries);
    }

    private Indicator getPriceIndicator(TimeSeries timeSeries) {
        return new ClosePriceIndicator(timeSeries);
    }

    /**
     * 获取rsi
     *
     * @param timeSeries
     * @param day
     * @return
     */
    private Indicator getRSI(TimeSeries timeSeries, Integer day, BuyAndSellIndicatorTo.SourceBean sourceBean) {
        if (day == null) {
            day = 14;
        }
        IndicatorAdapter rsiIndicator = new RsiIndicatorAdapter(timeSeries, day, sourceBean);
        return rsiIndicator.indicatorCalculation();

    }

    /**
     * 计算sma 指标
     *
     * @param timeSeries
     * @param day
     * @param sourceBean
     * @return
     */
    private Indicator getSma(TimeSeries timeSeries, Integer day, BuyAndSellIndicatorTo.SourceBean sourceBean) {
        if (day == null) {
            day = 5;
        }
        IndicatorAdapter smaIndicator = new SmaIndicatorAdapter(timeSeries, day, sourceBean);
        return smaIndicator.indicatorCalculation();
    }

    /**
     * 获取ADX
     *
     * @param timeSeries
     * @param day
     * @return
     */
    private Indicator getADX(TimeSeries timeSeries, Integer day, BuyAndSellIndicatorTo.SourceBean sourceBean) {
        if (day == null) {
            day = 14;
        }
        IndicatorAdapter adxIndicator = new ADXIndicatorAdapter(timeSeries, day, sourceBean);
        return adxIndicator.indicatorCalculation();

    }

    /**
     * 计算ema
     *
     * @param timeSeries
     * @param day
     * @param sourceBean
     * @return
     */
    private Indicator getEma(TimeSeries timeSeries, Integer day, BuyAndSellIndicatorTo.SourceBean sourceBean) {
        if (day == null) {
            day = 5;
        }
        IndicatorAdapter emaIndicatorCalculation = new EmaIndicatorAdapter(timeSeries, day, sourceBean);
        return emaIndicatorCalculation.indicatorCalculation();
    }

    /**
     * 计算cci
     *
     *
     * @param timeSeries
     * @param day
     * @return
     */
    private Indicator getCCI(TimeSeries timeSeries, Integer day) {
        if (day == null) {
            day = 20;
        }
        IndicatorAdapter cciIndicatorAdapter = new CciIndicatorAdapter(timeSeries, day);
        return cciIndicatorAdapter.indicatorCalculation();
    }

    /**
     * Ao
     *
     * @param timeSeries
     * @param day
     * @param day2
     * @return
     */
    private Indicator getAo(TimeSeries timeSeries, Integer day, Integer day2) {
        IndicatorAdapter aoIndicatorAdapter = new AoIndicatorAdapter(timeSeries, day, day2);
        return aoIndicatorAdapter.indicatorCalculation();
    }

    /**
     * 获取 getStochK
     *
     * @param timeSeries
     * @param day
     * @return
     */
    private Indicator getStochK(TimeSeries timeSeries, Integer day) {
        if (day == null) {
            day = 3;
        }
        return new StochasticKiIndicatorAdapter(timeSeries, day).indicatorCalculation();

    }

    /**
     * getStochD
     *
     * @param timeSeries
     * @param day
     * @return
     */
    private Indicator getStochD(TimeSeries timeSeries, Integer day) {
        if (day == null) {
            day = 3;
        }
        return new StochasticDIndicatorAdapter(timeSeries, day).indicatorCalculation();
    }

    /**
     * @param timeSeries
     * @param day
     * @param day2
     * @return
     */
    private Indicator getMacd(TimeSeries timeSeries, Integer day, Integer day2, BuyAndSellIndicatorTo.SourceBean sourceBean) {

        if (day == null) {
            day = 12;
        }
        if (day2 == null) {
            day2 = 26;
        }


        return new MacdIndicatorAdapter(timeSeries, day, day2, sourceBean).indicatorCalculation();
    }

}
