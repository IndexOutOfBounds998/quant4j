package com.quant.core.factory;


import com.quant.common.domain.vo.IndicatorCalParam;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;

/**
 * 指标工厂基类
 * Created by yang on 2019/5/26.
 */
public abstract class AbsIndicatorFactory {

    protected TimeSeries timeSeries;

    public AbsIndicatorFactory(TimeSeries timeSeries) {
        this.timeSeries = timeSeries;
    }

    /**
     * 通过指标名称获取指标
     *
     * @param indicatorName
     * @return
     */
    public abstract Indicator getIndicator(String indicatorName);

    public abstract Indicator getIndicator(IndicatorCalParam simpleIndicatorVo);


}
