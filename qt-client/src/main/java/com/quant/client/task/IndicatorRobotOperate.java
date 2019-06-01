package com.quant.client.task;

import com.quant.common.config.RedisUtil;
import com.quant.common.config.VpnProxyConfig;
import com.quant.common.domain.vo.IndicatorStrategyVo;
import com.quant.common.domain.vo.Market;
import com.quant.common.domain.vo.RobotStrategyVo;
import com.quant.core.api.ApiClient;
import com.quant.core.config.AccountConfig;
import com.quant.core.config.MarketConfig;
import com.quant.core.config.StrategyConfig;
import com.quant.core.config.imp.HuoBiAccountConfigImpl;
import com.quant.core.config.imp.HuoBiMarketConfigImpl;
import com.quant.core.config.imp.HuoboIndicatorStragegyConfig;
import com.quant.core.config.imp.HuoboSimpleStragegyConfig;
import com.quant.core.exchangeAdapter.HuobiExchangeAdapter;
import com.quant.core.strategy.StrategyException;
import com.quant.core.strategy.TradingStrategy;
import com.quant.core.strategy.impl.HuoBIndicatoryStrategyImpl;
import com.quant.core.strategy.impl.HuoBiStrategyImpl;
import com.quant.core.trading.TradingApi;
import lombok.extern.slf4j.Slf4j;

/**
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
        TradingStrategy huoBIndicatoryStrategy = new HuoBIndicatoryStrategyImpl(redisUtil, vo.getRobotId());
        ApiClient apiClient = new ApiClient(vo.getAppKey(), vo.getAppSecret(), vpnProxyConfig);
        TradingApi api = new HuobiExchangeAdapter(apiClient);
        MarketConfig marketConfig = new HuoBiMarketConfigImpl(new Market(vo.getSymbol()));
        StrategyConfig strategyConfig = new HuoboIndicatorStragegyConfig(vo.getIndicatorTo());
        AccountConfig accountConfig = new HuoBiAccountConfigImpl(vo.getAccountConfig());
        log.info("加载机器人{}>>>>>>", vo.getRobotId());
        huoBIndicatoryStrategy.init(api, marketConfig, strategyConfig, accountConfig);
        try {
            huoBIndicatoryStrategy.execute();
        } catch (StrategyException e) {
            e.printStackTrace();
        }

    }

}
