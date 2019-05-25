package com.quant.core.factory;

import com.quant.common.vo.IndicatorCalParam;
import com.quant.core.indicator.ADXIndicatorCat;
import com.quant.core.indicator.IndicatorCat;
import com.quant.core.indicator.RsiIndicatorCat;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;

/**
 * 静态工厂 生成指标
 * Created by yang on 2019/5/26.
 */
public class StaticIndicatorFactory extends AbsIndicatorFactory {


    public StaticIndicatorFactory(TimeSeries timeSeries) {
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


        return null;
    }

    @Override
    public Indicator getIndicator(IndicatorCalParam indicatorCalParam) {

        if (indicatorCalParam.getIndicatorName().equals("RSI")) {

            return getRSI(timeSeries, indicatorCalParam.getDay());

        }
        if (indicatorCalParam.getIndicatorName().equals("ADX")) {

            return getADX(timeSeries, indicatorCalParam.getDay());

        }
        return null;
    }

    /**
     * 获取rsi
     *
     * @param timeSeries
     * @param day
     * @return
     */
    private Indicator getRSI(TimeSeries timeSeries, int day) {
        IndicatorCat rsiIndicator = new RsiIndicatorCat(timeSeries, day);
        return rsiIndicator.strategCalculation();

    }

    /**
     * 获取ADX
     *
     * @param timeSeries
     * @param day
     * @return
     */
    private Indicator getADX(TimeSeries timeSeries, int day) {
        IndicatorCat adxIndicator = new ADXIndicatorCat(timeSeries, day);
        return adxIndicator.strategCalculation();

    }


}
