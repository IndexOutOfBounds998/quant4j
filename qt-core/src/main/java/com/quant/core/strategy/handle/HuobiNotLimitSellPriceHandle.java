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
 * 火币市价
 * Created by yang on 2019/8/23.
 */
@Slf4j
public class HuobiNotLimitSellPriceHandle extends StrategyHandle {


    public HuobiNotLimitSellPriceHandle(StrategyHandle handle) {
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
        if (baseInfo.getIsLimitPrice() == PirceType.notLimit.getType()) {
            //市价卖出 价格直接填0 计算交易额度
            BigDecimal sellAmount = BigDecimal.ZERO;
            if (baseInfo.getIsAllSell() == SellType.sellAll.getType()) {
                //如果市价全部卖出 卖出为币 卖出所有的币 就是价格就是交易额度 quotaBalance;
                sellAmount = baseBalance.setScale(amountPrecision, RoundingMode.DOWN);
                log.info("市价全部卖出,卖出数量{}", sellAmount);
            } else {
                //不是全部购买 自定义交易额 购买
                sellAmount = sellAmount.add(baseInfo.getSellAmount()).setScale(amountPrecision, RoundingMode.DOWN);
                if (sellAmount.compareTo(baseBalance) > 0) {
                    //余额不足 直接设置成0 下订单会失败
                    sellAmount = BigDecimal.ZERO;
                }
            }
            HBOrderType hbOrderType = HBOrderType.SELL_MARKET;
            //非限价模式的时候 价格不需要填写 交易所会根据最优价格去计算价格
            return new HandleResult(hbOrderType, BigDecimal.ZERO, sellAmount);
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
