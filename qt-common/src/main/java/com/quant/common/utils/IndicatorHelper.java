package com.quant.common.utils;

import com.quant.common.exception.IndicatorException;
import com.quant.common.factory.StaticIndicatorFactory;
import com.quant.common.response.Kline;
import com.quant.common.to.BuyAndSellIndicatorTo;
import com.quant.common.to.IndicatorBean;
import com.quant.common.to.RuleBean;
import com.quant.common.vo.IndicatorCalParam;
import org.ta4j.core.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 指标计算 帮助类
 * Created by yang on 2019/5/24.
 */
public class IndicatorHelper {

    public static TimeSeries buildSeries(List<Kline> lines) {

        TimeSeries series = new BaseTimeSeries();
        Collections.reverse(lines);

        for (Kline kline : lines) {
            ZonedDateTime time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(kline.getId() * 1000), ZoneId.systemDefault());
            // build a bar
            Bar bar = new BaseBar(time,
                    kline.getOpen(),
                    kline.getHigh(),
                    kline.getLow(),
                    kline.getClose(),
                    kline.getVol(),
                    series.function());
            series.addBar(bar);
        }
        return series;

    }

    /**
     * builder a Indicator
     *
     * @param bean
     * @return
     */
    public static Indicator builderIndicator(RuleBean bean, TimeSeries timeSeries) {
        try {
            StaticIndicatorFactory factory = new StaticIndicatorFactory(timeSeries);
            String value = bean.getValue();
            String params = bean.getParams();
            String[] strings = params.split(",");
            //构建一个指标
            IndicatorCalParam indicatorCalParam = new IndicatorCalParam();
            indicatorCalParam.setIndicatorName(value);
            indicatorCalParam.setParams(strings);
            return factory.getIndicator(indicatorCalParam);
        } catch (Exception e) {
            throw new IndicatorException(e);
        }
    }

}
