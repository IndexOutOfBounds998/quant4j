package com.quant.client.Task;

import com.quant.common.config.RedisUtil;
import com.quant.common.config.VpnProxyConfig;
import com.quant.core.api.ApiClient;
import com.quant.core.config.AccountConfig;
import com.quant.core.config.MarketConfig;
import com.quant.core.config.StrategyConfig;
import com.quant.core.config.imp.HuoBiAccountConfigImpl;
import com.quant.core.config.imp.HuoBiMarketConfigImpl;
import com.quant.core.domain.Market;
import com.quant.core.exchangeAdapter.HuobiExchangeAdapter;
import com.quant.core.strategy.StrategyException;
import com.quant.core.strategy.TradingStrategy;
import com.quant.core.strategy.impl.HuoBiStrategyImpl;
import com.quant.core.trading.TradingApi;
import com.quant.core.vo.RobotStrategyVo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RobotOperate {

    private RedisUtil redisUtil;


    private VpnProxyConfig vpnProxyConfig;

    public RobotOperate(RedisUtil redisUtil, VpnProxyConfig vpnProxyConfig) {
        this.redisUtil = redisUtil;
        this.vpnProxyConfig = vpnProxyConfig;
    }

    public void doRobotTask(RobotStrategyVo vo) {
        log.info("启动机器人{}>>>>>>", vo.getRobotId());
        TradingStrategy huobiStrategy = new HuoBiStrategyImpl(redisUtil, vo.getRobotId());
        ApiClient apiClient = new ApiClient(vo.getAppKey(), vo.getAppSecret(), vpnProxyConfig);
        TradingApi api = new HuobiExchangeAdapter(apiClient);
        MarketConfig marketConfig = new HuoBiMarketConfigImpl(new Market(vo.getSymbol()));
        StrategyConfig strategyConfig = new StrategyConfig(vo.getStrategyVo());
        AccountConfig accountConfig = new HuoBiAccountConfigImpl(vo.getAccountConfig());
        log.info("加载机器人{}>>>>>>", vo.getRobotId());
        huobiStrategy.init(api, marketConfig, strategyConfig, accountConfig);
        try {
            huobiStrategy.execute();
        } catch (StrategyException e) {
            e.printStackTrace();
        }

    }


}
