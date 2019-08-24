package com.quant.core.builder;

import com.quant.common.config.RedisUtil;
import com.quant.common.config.VpnProxyConfig;
import com.quant.common.domain.vo.IndicatorStrategyVo;
import com.quant.common.domain.vo.Market;
import com.quant.common.domain.vo.RobotStrategyVo;
import com.quant.common.enums.StratrgyType;
import com.quant.core.api.ApiClient;
import com.quant.core.config.AccountConfig;
import com.quant.core.config.MarketConfig;
import com.quant.core.config.StrategyConfig;
import com.quant.core.config.imp.HuoBiAccountConfigImpl;
import com.quant.core.config.imp.HuoBiMarketConfigImpl;
import com.quant.core.config.imp.HuoboIndicatorStragegyConfig;
import com.quant.core.config.imp.HuoboSimpleStragegyConfig;
import com.quant.core.exchangeAdapter.HuobiExchangeAdapter;
import com.quant.core.trading.TradingApi;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Created by yang on 2019/8/24.
 */
@ToString
@Accessors(chain = true)
public class StrategyBuilder {

    @Getter
    @Setter
    private RedisUtil redisUtil;
    @Setter
    private StratrgyType stratrgyType;


    @Getter
    @Setter
    private VpnProxyConfig vpnProxyConfig;
    @Getter
    @Setter
    private IndicatorStrategyVo indicatorStrategyVo;
    @Getter
    @Setter
    private RobotStrategyVo robotStrategyVo;

    @Getter
    private ApiClient apiClient;
    @Getter
    private TradingApi tradingApi;
    @Getter
    private MarketConfig marketConfig;
    @Getter
    private StrategyConfig strategyConfig;
    @Getter
    private AccountConfig accountConfig;


    public StrategyBuilder buildApiClient() {


        if (stratrgyType == StratrgyType.indicator) {
            this.apiClient = new ApiClient(indicatorStrategyVo.getAppKey(), indicatorStrategyVo.getAppSecret(), vpnProxyConfig);

        } else if (stratrgyType == StratrgyType.simple) {
            this.apiClient = new ApiClient(robotStrategyVo.getAppKey(), robotStrategyVo.getAppSecret(), vpnProxyConfig);
        }
        return this;
    }


    public StrategyBuilder buildTradingApi() {

        if (apiClient == null) {
            throw new IllegalArgumentException("请先构建api client");
        }

        this.tradingApi = new HuobiExchangeAdapter(apiClient);

        return this;
    }

    public StrategyBuilder buildMarketConfig() {
        if (stratrgyType == StratrgyType.indicator) {
            this.marketConfig = new HuoBiMarketConfigImpl(new Market(indicatorStrategyVo.getSymbol()));
        } else if (stratrgyType == StratrgyType.simple) {
            this.marketConfig = new HuoBiMarketConfigImpl(new Market(robotStrategyVo.getSymbol()));
        }
        return this;
    }


    public StrategyBuilder buildStrategyConfig() {
        if (stratrgyType == StratrgyType.indicator) {
            this.strategyConfig = new HuoboIndicatorStragegyConfig(indicatorStrategyVo.getIndicatorTo());
        } else if (stratrgyType == StratrgyType.simple) {
            this.strategyConfig = new HuoboSimpleStragegyConfig(robotStrategyVo.getStrategyVo());
        }
        return this;
    }

    public StrategyBuilder buildAccountConfig() {

        if (stratrgyType == StratrgyType.indicator) {
            this.accountConfig = new HuoBiAccountConfigImpl(indicatorStrategyVo.getAccountConfig());
        } else if (stratrgyType == StratrgyType.simple) {
            this.accountConfig = new HuoBiAccountConfigImpl(robotStrategyVo.getAccountConfig());
        }

        return this;
    }


}
