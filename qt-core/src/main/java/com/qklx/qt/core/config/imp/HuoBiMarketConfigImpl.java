package com.qklx.qt.core.config.imp;

import com.qklx.qt.core.config.MarketConfig;
import com.qklx.qt.core.domain.Market;

public class HuoBiMarketConfigImpl implements MarketConfig {
    private Market market;

    public HuoBiMarketConfigImpl(Market market) {
        this.market = market;
    }


    @Override
    public String markName() {
        return market.getMarketName();
    }


}
