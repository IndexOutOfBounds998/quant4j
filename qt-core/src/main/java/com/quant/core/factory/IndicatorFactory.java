package com.quant.core.factory;

import com.quant.core.indicatorAdapter.*;
import com.quant.common.domain.vo.IndicatorCalParam;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;

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
        if (indicatorName.equals("RSI")) {
            //产生rsi 指标
            return getRSI(timeSeries, 14);
        }
        if (indicatorName.equals("ADX")) {
            return getADX(timeSeries, 14);
        }
        if (indicatorName.equals("price")) {
            return getPriceIndicator(timeSeries);
        }
        if (indicatorName.equals("amount")) {
            return getAmount(timeSeries);
        }
        if (indicatorName.equals("SMA")) {
            return getSma(timeSeries, 5);
        }
        if (indicatorName.equals("EMA")) {
            return getEma(timeSeries, 5);
        }
        if (indicatorName.equals("CCI")) {
            return getCCI(timeSeries, 20);
        }
        if (indicatorName.equals("AO")) {
            return getAo(timeSeries, 5, 34);
        }
        return null;
    }

    @Override
    public Indicator getIndicator(IndicatorCalParam indicatorCalParam) {
        if (indicatorCalParam.getIndicatorName().equals("RSI")) {
            int day = Integer.parseInt(indicatorCalParam.getParams()[0]);
            return getRSI(timeSeries, day);
        } else if (indicatorCalParam.getIndicatorName().equals("ADX")) {
            int day = Integer.parseInt(indicatorCalParam.getParams()[0]);
            return getADX(timeSeries, day);
        } else if (indicatorCalParam.getIndicatorName().equals("price")) {
            return getPriceIndicator(timeSeries);
        } else if (indicatorCalParam.getIndicatorName().equals("amount")) {
            return getAmount(timeSeries);
        } else if (indicatorCalParam.getIndicatorName().equals("SMA")) {
            int day = Integer.parseInt(indicatorCalParam.getParams()[0]);
            return getSma(timeSeries, day);
        } else if (indicatorCalParam.getIndicatorName().equals("EMA")) {
            int day = Integer.parseInt(indicatorCalParam.getParams()[0]);
            return getEma(timeSeries, day);
        } else if (indicatorCalParam.getIndicatorName().equals("CCI")) {
            int day = Integer.parseInt(indicatorCalParam.getParams()[0]);
            return getCCI(timeSeries, day);
        } else if (indicatorCalParam.getIndicatorName().equals("AO")) {
            Integer day = null;
            try {
                day = Integer.parseInt(indicatorCalParam.getParams()[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            Integer day2 = null;
            try {
                day2 = Integer.parseInt(indicatorCalParam.getParams()[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return getAo(timeSeries, day, day2);
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
    private Indicator getRSI(TimeSeries timeSeries, int day) {
        IndicatorAdapter rsiIndicator = new RsiIndicatorAdapter(timeSeries, day);
        return rsiIndicator.indicatorCalculation();

    }

    /**
     * 计算sma 指标
     *
     * @param timeSeries
     * @param i
     * @return
     */
    private Indicator getSma(TimeSeries timeSeries, int i) {
        IndicatorAdapter smaIndicator = new SmaIndicatorAdapter(timeSeries, i);
        return smaIndicator.indicatorCalculation();
    }

    /**
     * 获取ADX
     *
     * @param timeSeries
     * @param day
     * @return
     */
    private Indicator getADX(TimeSeries timeSeries, int day) {
        IndicatorAdapter adxIndicator = new ADXIndicatorAdapter(timeSeries, day);
        return adxIndicator.indicatorCalculation();

    }

    /**
     * 计算ema
     *
     * @param timeSeries
     * @param i
     * @return
     */
    private Indicator getEma(TimeSeries timeSeries, int i) {
        IndicatorAdapter emaIndicatorCalculation = new EmaIndicatorAdapter(timeSeries, i);
        return emaIndicatorCalculation.indicatorCalculation();
    }

    /**
     * 计算cci
     *
     * @param timeSeries
     * @param i
     * @return
     */
    private Indicator getCCI(TimeSeries timeSeries, int i) {
        IndicatorAdapter cciIndicatorAdapter = new CciIndicatorAdapter(timeSeries, i);
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
}
