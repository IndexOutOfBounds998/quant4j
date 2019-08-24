package com.quant.client.task;

import com.quant.common.config.RedisUtil;
import com.quant.common.config.VpnProxyConfig;
import com.quant.common.domain.vo.IndicatorStrategyVo;
import com.quant.common.enums.StrategyType;
import com.quant.core.builder.StrategyBuilder;
import com.quant.core.strategy.StrategyException;
import com.quant.core.strategy.TradingStrategy;
import com.quant.core.strategy.impl.HuoBIndicatoryStrategyImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * IndicatorRobotOperate 指标策略机器人启动
 * Created by yang on 2019/5/31.
 */
@Slf4j
public class IndicatorRobotOperate {
    private RedisUtil redisUtil;


    private VpnProxyConfig vpnProxyConfig;

    public IndicatorRobotOperate(RedisUtil redisUtil, VpnProxyConfig vpnProxyConfig) {
        this.redisUtil = redisUtil;
        this.vpnProxyConfig = vpnProxyConfig;
    }

    public void doRobotTask(IndicatorStrategyVo vo) {
        log.info("启动机器人{}>>>>>>", vo.getRobotId());
        TradingStrategy strategy = builderStrategy(vo);
        try {
            strategy.execute();
        } catch (StrategyException e) {
            e.printStackTrace();
        }

    }

    private TradingStrategy builderStrategy(IndicatorStrategyVo vo) {

        StrategyType indicator = StrategyType.indicator;
        StrategyBuilder builder = new StrategyBuilder().setRedisUtil(redisUtil)
                .setStratrgyType(indicator)
                .setIndicatorStrategyVo(vo).setVpnProxyConfig(vpnProxyConfig)
                .buildApiClient()
                .buildTradingApi().buildMarketConfig()
                .buildStrategyConfig().buildAccountConfig();

        TradingStrategy huoBIndicatoryStrategy = new HuoBIndicatoryStrategyImpl(redisUtil, vo.getRobotId());

        log.info("加载机器人{}>>>>>>", vo.getRobotId());
        huoBIndicatoryStrategy.init(builder);
        return huoBIndicatoryStrategy;
    }

}
