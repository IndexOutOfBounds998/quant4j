package com.quant.common.enums;

/**
 * Created by yang on 2019/8/24.
 */
public enum StrategyType {
    /**
     * 简单数值
     */
    simple(0),
    /**
     * 指标
     */
    indicator(1);


    Integer type;

    StrategyType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }
}
