package com.quant.core.config.imp;

import com.quant.common.domain.to.BuyAndSellIndicatorTo;
import com.quant.common.domain.to.llIndicatorTo;
import com.quant.common.domain.vo.StrategyVo;
import com.quant.core.config.StrategyConfig;

/**
 * Created by yang on 2019/5/31.
 */
public class HuoboSimpleStragegyConfig implements StrategyConfig {


    public HuoboSimpleStragegyConfig(StrategyVo strategyVo) {
        this.strategyVo = strategyVo;
    }

    private StrategyVo strategyVo;

    @Override
    public StrategyVo getStrategyVo() {
        return strategyVo;
    }

    @Override
    public llIndicatorTo getIndicatorStrategy() {
        return null;
    }
}
