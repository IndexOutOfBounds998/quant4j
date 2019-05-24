package com.quant.core.config;

public interface KlineConfig {
    /**
     * 每次获取数据的大小
     * @return
     */
    String size();

    /**
     * 返回数据时间粒度，也就是每根蜡烛的时间区间	1min, 5min, 15min, 30min, 60min, 1day, 1mon, 1week, 1year
     * @return
     */
    String period();

}
