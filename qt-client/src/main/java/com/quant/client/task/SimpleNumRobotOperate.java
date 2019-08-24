package com.quant.client.task;

import com.quant.common.config.RedisUtil;
import com.quant.common.config.VpnProxyConfig;
import com.quant.common.enums.StratrgyType;
import com.quant.core.builder.StrategyBuilder;
import com.quant.core.strategy.StrategyException;
import com.quant.core.strategy.TradingStrategy;
import com.quant.core.strategy.impl.HuoBiSimpleStrategyImpl;
import com.quant.common.domain.vo.RobotStrategyVo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleNumRobotOperate {

    private RedisUtil redisUtil;


    private VpnProxyConfig vpnProxyConfig;

    public SimpleNumRobotOperate(RedisUtil redisUtil, VpnProxyConfig vpnProxyConfig) {
        this.redisUtil = redisUtil;
        this.vpnProxyConfig = vpnProxyConfig;
    }

    public void doRobotTask(RobotStrategyVo vo) {
        log.info("启动机器人{}>>>>>>", vo.getRobotId());
        TradingStrategy strategy = builderStrategy(vo);
        try {
            strategy.execute();
        } catch (StrategyException e) {
            e.printStackTrace();
        }

    }

    private TradingStrategy builderStrategy(RobotStrategyVo vo) {

        StratrgyType simple = StratrgyType.simple;
        StrategyBuilder builder = new StrategyBuilder().setRedisUtil(redisUtil)
                .setStratrgyType(simple)
                .setRobotStrategyVo(vo)
                .setVpnProxyConfig(vpnProxyConfig)
                .buildApiClient()
                .buildTradingApi().buildMarketConfig()
                .buildStrategyConfig().buildAccountConfig();

        TradingStrategy huoBIndicatoryStrategy = new HuoBiSimpleStrategyImpl(redisUtil, vo.getRobotId());

        log.info("加载机器人{}>>>>>>", vo.getRobotId());
        huoBIndicatoryStrategy.init(builder);
        return huoBIndicatoryStrategy;
    }

}
