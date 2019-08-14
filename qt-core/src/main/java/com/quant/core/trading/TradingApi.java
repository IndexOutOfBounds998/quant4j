/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Gareth Jon Lynch
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.quant.core.trading;

import com.quant.common.config.RedisUtil;
import com.quant.common.domain.entity.MarketOrder;
import com.quant.common.enums.OrderType;
import com.quant.common.enums.HBOrderType;
import com.quant.common.exception.ExchangeNetworkException;
import com.quant.common.exception.TradingApiException;
import com.quant.core.config.AccountConfig;
import com.quant.core.config.KlineConfig;
import com.quant.core.config.MarketConfig;
import com.quant.common.domain.response.Kline;
import com.quant.common.domain.response.OrdersDetail;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author yang

 * @desc TradingApi
 * @date 2019/7/9
 */
public interface TradingApi {

    /**
     * Returns the current version of the API.
     *
     * @return the API version.
     * @since 1.0
     */
    default String getVersion() {
        return "1.1";
    }

    /**
     * Returns the API implementation name.
     *
     * @return the API implementation name.
     * @since 1.0
     */
    String getImplName();

    /**
     * 获取市场上买卖订单
     *
     * @param config
     * @return
     */
    MarketOrder getMarketOrders(MarketConfig config, String size);

    /**
     * 查询为完成的订单
     *
     * @param config
     * @return
     * @throws ExchangeNetworkException
     * @throws TradingApiException
     */
    List<OpenOrder> getOpenOrders(MarketConfig config, AccountConfig accountConfig, String size) throws ExchangeNetworkException, TradingApiException;


    Long createOrder(String marketId, String accountId, HBOrderType HBOrderType, BigDecimal quantity, BigDecimal price)
            throws ExchangeNetworkException, TradingApiException;


    boolean cancelOrder(String orderId, String marketId) throws ExchangeNetworkException, TradingApiException;


    /**
     * 获取订单详情
     * @param orderId
     * @return
     */
    OrdersDetail orderDetail(Long orderId);

    BigDecimal getLatestMarketPrice(String marketId) throws ExchangeNetworkException, TradingApiException;

    /**
     * 获取账户的余额信息
     *
     * @since 1.0
     */
    BalanceInfo getBalanceInfo(String accountId, RedisUtil redisUtil) throws ExchangeNetworkException, TradingApiException;


    /**
     * symbol	string	true	NA	交易对	btcusdt, ethbtc...
     * period	string	true	NA	返回数据时间粒度，也就是每根蜡烛的时间区间	1min, 5min, 15min, 30min, 60min, 1day, 1mon, 1week, 1year
     * size	integer	false	150	返回 K 线数据条数	[1, 2000]
     *
     * @return
     */
    List<Kline> getKline(MarketConfig marketConfig, KlineConfig klineConfig);
}
