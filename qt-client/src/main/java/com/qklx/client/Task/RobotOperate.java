package com.qklx.client.Task;

import com.qklx.qt.common.config.RedisUtil;
import com.qklx.qt.core.api.ApiClient;
import com.qklx.qt.core.config.AccountConfig;
import com.qklx.qt.core.config.MarketConfig;
import com.qklx.qt.core.config.StrategyConfig;
import com.qklx.qt.core.config.imp.HuoBiAccountConfigImpl;
import com.qklx.qt.core.config.imp.HuoBiMarketConfigImpl;
import com.qklx.qt.core.domain.Market;
import com.qklx.qt.core.exchangeAdapter.HuobiExchangeAdapter;
import com.qklx.qt.core.strategy.StrategyException;
import com.qklx.qt.core.strategy.TradingStrategy;
import com.qklx.qt.core.strategy.impl.HuoBiStrategyImpl;
import com.qklx.qt.core.trading.TradingApi;
import com.qklx.qt.core.vo.RobotStrategyVo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RobotOperate {

    private RedisUtil redisUtil;


    private String ip;

    private int port;

    public RobotOperate(RedisUtil redisUtil, String ip, int port) {
        this.redisUtil = redisUtil;
        this.ip = ip;
        this.port = port;
    }

    public void doRobotTask(RobotStrategyVo vo) {
        log.info("启动机器人==================================================");
        TradingStrategy huobiStrategy = new HuoBiStrategyImpl(redisUtil, vo.getRobotId());
        ApiClient apiClient = new ApiClient(vo.getAppKey(), vo.getAppSecret(), this.ip, this.port);
        TradingApi api = new HuobiExchangeAdapter(apiClient);
        MarketConfig marketConfig = new HuoBiMarketConfigImpl(new Market(vo.getSymbol()));
        StrategyConfig strategyConfig = new StrategyConfig(vo.getStrategyVo());
        AccountConfig accountConfig = new HuoBiAccountConfigImpl(vo.getAccountConfig());
        huobiStrategy.init(api, marketConfig, strategyConfig, accountConfig);
        try {
            log.info("开启线程==================================================");
            huobiStrategy.execute();
        } catch (StrategyException e) {
            e.printStackTrace();
        }

    }


}
