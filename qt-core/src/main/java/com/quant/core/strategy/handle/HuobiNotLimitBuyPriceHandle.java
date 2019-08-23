package com.quant.core.strategy.handle;

import com.quant.common.domain.vo.BaseInfoEntity;
import com.quant.common.enums.HBOrderType;
import com.quant.common.enums.PirceType;
import com.quant.common.enums.SellType;
import com.quant.core.config.AccountConfig;
import com.quant.core.config.MarketConfig;
import com.quant.core.config.StrategyConfig;
import com.quant.core.trading.TradingApi;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 火币限价
 * Created by yang on 2019/8/23.
 */
@Slf4j
public class HuobiNotLimitBuyPriceHandle extends StrategyHandle {


    public HuobiNotLimitBuyPriceHandle(StrategyHandle handle) {
        super(handle);
    }

    @Override
    public HandleResult strategyRequest(TradingApi tradingApi,
                                        MarketConfig marketConfig,
                                        StrategyConfig config,
                                        AccountConfig accountConfig,
                                        int pricePrecision,
                                        int amountPrecision,
                                        BigDecimal baseBalance) {

        final BaseInfoEntity baseInfo = config.getIndicatorStrategy().getBaseInfo();
        if (baseInfo.getIsLimitPrice() == PirceType.notLimit.getType()) {
            //市价卖出 价格直接填0 计算交易额度
            BigDecimal buyAmount = BigDecimal.ZERO;
            if (baseInfo.getIsAllSell() == SellType.sellAll.getType()) {
                //如果市价全部卖出 卖出为币 卖出所有的币 就是价格就是交易额度 quotaBalance;
                buyAmount = baseBalance.setScale(amountPrecision, RoundingMode.DOWN);
            } else {
                //不是全部购买 自定义交易额 购买
                buyAmount = buyAmount.add(baseInfo.getSellAmount()).setScale(amountPrecision, RoundingMode.DOWN);
            }
            HBOrderType hbOrderType = HBOrderType.BUY_MARKET;
            //非限价模式的时候 价格不需要填写 交易所会根据最优价格去计算价格
            return new HandleResult(hbOrderType, BigDecimal.ZERO, buyAmount);
        } else {

            return getHandle().strategyRequest(tradingApi,
                    marketConfig,
                    config,
                    accountConfig,
                    pricePrecision,
                    amountPrecision,
                    baseBalance);

        }


    }
}
