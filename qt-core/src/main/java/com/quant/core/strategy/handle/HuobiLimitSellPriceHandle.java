package com.quant.core.strategy.handle;

import com.quant.common.domain.entity.MarketOrder;
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
public class HuobiLimitSellPriceHandle extends StrategyHandle {


    public HuobiLimitSellPriceHandle(StrategyHandle handle) {
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

        if (getHandle() == null) {
            return null;
        }

        final BaseInfoEntity baseInfo = config.getIndicatorStrategy().getBaseInfo();
        if (baseInfo.getIsLimitPrice() == PirceType.isLimit.getType()) {
            //从当前的20个卖出订单里找出最优的价格 （限价卖出）
            final MarketOrder marketOrder = tradingApi.getMarketOrders(marketConfig, "500");
            final BigDecimal currentSellPrice = marketOrder.getSell().get(0).getPrice();
            //计算卖出的价格
            final BigDecimal sellPrice = baseInfo.getSellPrice().add(currentSellPrice).setScale(pricePrecision, RoundingMode.UP);
            log.info("限价卖出:当前卖出的价格:{},计算后的订单卖出价格:{}", currentSellPrice, sellPrice);
            //计算购买的数量 是否全部卖出
            BigDecimal sellAmount = BigDecimal.ZERO;
            if (baseInfo.getIsAllSell() == SellType.sellAll.getType()) {
                //从用户api的表里查询到他的账户相应的base 火币的数量全部购买
                sellAmount = sellAmount.add(baseBalance).setScale(amountPrecision, RoundingMode.DOWN);
                log.info("限价全部卖出:账户id{}卖出数量为{}", accountConfig.accountId(), sellAmount);

            } else {
                sellAmount = sellAmount.add(baseInfo.getSellAmount().setScale(amountPrecision, RoundingMode.DOWN));
                if (sellAmount.compareTo(baseBalance) > 0) {
                    log.info("限价自定义卖出余额{}大于账户余额{}:账户id{}的余额不足,需要充值......", sellAmount, baseBalance, accountConfig.accountId());
                }
                log.info("限价自定义卖出数量:账户id{},卖出数量{}", accountConfig.accountId(), sellAmount);
            }
            HBOrderType hbOrderType = HBOrderType.SELL_LIMIT;
            return new HandleResult(hbOrderType, sellPrice, sellAmount);
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
