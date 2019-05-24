package com.quant.common.utils;

import com.quant.common.response.Kline;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;

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


}
