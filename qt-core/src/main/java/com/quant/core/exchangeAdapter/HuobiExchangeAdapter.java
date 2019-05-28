package com.quant.core.exchangeAdapter;

import com.alibaba.fastjson.JSON;
import com.quant.common.config.RedisUtil;
import com.quant.common.constans.RobotRedisKeyConfig;
import com.quant.core.api.ApiClient;
import com.quant.common.exception.ApiException;
import com.quant.core.config.AccountConfig;
import com.quant.core.config.KlineConfig;
import com.quant.core.config.MarketConfig;
import com.quant.common.enums.OrderType;
import com.quant.common.domain.request.CreateOrderRequest;
import com.quant.common.domain.response.*;
import com.quant.core.trading.*;
import com.quant.core.trading.impl.HuoBiOpenOrderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 火币交换器
 */
public class HuobiExchangeAdapter extends BaseExchangeAdapter implements TradingApi {
    private static Logger logger = LoggerFactory.getLogger(HuobiExchangeAdapter.class);

    private ApiClient apiClient;

    public HuobiExchangeAdapter(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private void buyAndSellTemp(List<TradeBean> buy, List<TradeBean> tradeBeans, String buySign) {
        List<TradeBean> buyTemp = tradeBeans.stream()
                .filter(tradeBean -> tradeBean.getDirection().equals(buySign))
                .collect(Collectors.toList());
        if (!buyTemp.isEmpty()) {
            buy.addAll(buyTemp);
        }
    }


    @Override
    public String getImplName() {
        return "huo bi";
    }


    /**
     * 获取市场买卖订单量
     *
     * @param config the id of the market.
     * @return
     * @throws ExchangeNetworkException
     * @throws TradingApiException
     */
    @Override
    public MarketOrder getMarketOrders(MarketConfig config, String size) {
        HistoryTradeResponse<List<Trade<List<TradeBean>>>> historyTradeResponse = null;
        try {
            historyTradeResponse = apiClient.historyTrade(config.markName(), size);
        } catch (Exception e) {
            logger.error("请求获取市场买卖订单发生异常........." + e.getMessage());
            return null;
        }
        MarketOrder marketOrder = new MarketOrder();
        List<TradeBean> buy = new ArrayList<>();
        List<TradeBean> sell = new ArrayList<>();
        if (historyTradeResponse != null) {
            if (status.equals(historyTradeResponse.getStatus())) {
                // get data success
                List<Trade<List<TradeBean>>> trade = historyTradeResponse.getData();
                for (Trade<List<TradeBean>> listTrade : trade) {
                    List<TradeBean> tradeBeans = listTrade.getData();
                    buyAndSellTemp(buy, tradeBeans, buySign);
                    buyAndSellTemp(sell, tradeBeans, sellSign);
                }
                marketOrder.setBuy(buy);
                marketOrder.setSell(sell);
            }

        }

        return marketOrder;
    }

    /**
     * 获取自己未完成的订单
     *
     * @param config
     * @param accountConfig
     * @return
     */
    @Override
    public List<OpenOrder> getOpenOrders(MarketConfig config, AccountConfig accountConfig, String size) {
        ApiResponse<List<OrdersDetail>> res;
        try {
            res = apiClient.openAllOrders(accountConfig.accountId(), config.markName(), size);
        } catch (Exception e) {
            logger.error("获取个人未完成的订单失败===========" + e.getMessage());
            return null;
        }
        List<OpenOrder> list = new ArrayList<>();
        for (OrdersDetail ordersDetail : res.getData()) {
            OpenOrder openOrder = new HuoBiOpenOrderImpl(ordersDetail);
            list.add(openOrder);
        }

        return list;

    }

    /**
     * 创建订单
     *
     * @param marketId  the id of the market.
     * @param orderType 订单对应的类型
     * @param quantity  amount of units you are buying/selling in this order.
     * @param price     the price per unit you are buying/selling at.
     * @return
     */
    @Override
    public Long createOrder(String marketId, String accountId, OrderType orderType, BigDecimal quantity, BigDecimal price) {

            CreateOrderRequest createOrderRequest = new CreateOrderRequest();
            createOrderRequest.setAmount(quantity.toPlainString());
            createOrderRequest.setAccountId(accountId);
            createOrderRequest.setSource("api");
            if (!orderType.getTyoe().equals(OrderType.SELL_MARKET.getTyoe()) || !orderType.getTyoe().equals(OrderType.BUY_MARKET.getTyoe())) {
                createOrderRequest.setPrice(price.toPlainString());
            }
            createOrderRequest.setSymbol(marketId);
            createOrderRequest.setType(orderType.getTyoe());
            logger.info("创建订单详情:{}", JSON.toJSONString(createOrderRequest));
            return apiClient.createOrder(createOrderRequest);

    }

    /**
     * 取消订单
     *
     * @param orderId  your order Id.
     * @param marketId the id of the market the order was placed on, e.g. btc_usd
     * @return
     */
    @Override
    public boolean cancelOrder(String orderId, String marketId) {
        SubmitcancelResponse submitcancelResponse = null;
        try {
            submitcancelResponse = apiClient.submitcancel(orderId);
        } catch (Exception e) {
            logger.error("交易对: " + marketId + " 取消订单: " + orderId + " 失败=========" + e.getMessage());
            throw new ApiException(e);
        }
        if (submitcancelResponse != null && submitcancelResponse.getStatus().equals("ok")) {
            return true;
        }
        return false;
    }

    @Override
    public OrdersDetail orderDetail(Long orderId) {
        try {
            OrdersDetailResponse<OrdersDetail> detail = apiClient.ordersDetail(orderId.toString());
            if (detail.getStatus().equals(RobotRedisKeyConfig.ok)) {
                return detail.getData();
            }
        } catch (Exception e) {
            logger.error("获取订单详情失败====" + e.getMessage());
            throw new ApiException(e);
        }
        return null;
    }


    /**
     * 获取市场最新价格
     *
     * @param marketId the id of the market.
     * @return
     * @throws ExchangeNetworkException
     * @throws TradingApiException
     */
    @Override
    public BigDecimal getLatestMarketPrice(String marketId) throws ExchangeNetworkException, TradingApiException {
        DetailResponse<Details> detailsDetailResponse = apiClient.detail(marketId);

        if (detailsDetailResponse.getStatus().equals("ok")) {
            return detailsDetailResponse.getTick().getClose();
        }
        return null;
    }

    @Override
    public BalanceInfo getBalanceInfo(String accountId, RedisUtil redisUtil) throws ExchangeNetworkException, TradingApiException {
        BalanceResponse<Balance<List<BalanceBean>>> response = null;
        try {
            response = apiClient.balance(String.valueOf(accountId));
        } catch (Exception e) {
            logger.error("获取账户余额失败，账户id{}", accountId);
            throw new ApiException(e);
        }
        if (response != null && response.getStatus().equals("ok")) {
            CopyOnWriteArrayList<BalanceBean> balances = new CopyOnWriteArrayList<>(response.getData().getList());
            if (!balances.isEmpty()) {
                for (BalanceBean bean : balances) {
                    if (bean.getType().equals("trade")) {
                        redisUtil.set(bean.getCurrency() + "_balance_" + accountId, bean.getBalance());
                    }
                }
            }
        }
        return null;
    }

    @Override
    public BigDecimal getPercentageOfBuyOrderTakenForExchangeFee(String marketId) throws TradingApiException, ExchangeNetworkException {
        return null;
    }

    @Override
    public BigDecimal getPercentageOfSellOrderTakenForExchangeFee(String marketId) throws TradingApiException, ExchangeNetworkException {
        return null;
    }

    /**
     * 获取k线
     *
     * @return
     */
    @Override
    public List<Kline> getKline(MarketConfig marketConfig, KlineConfig klineConfig) {
        KlineResponse<List<Kline>> klineResponse = null;
        try {
            klineResponse = apiClient.kline(marketConfig.markName(),
                    klineConfig.period(),
                    klineConfig.size());
            if (klineResponse != null && klineResponse.getStatus().equals("ok")) {
                return klineResponse.getData();
            }
        } catch (Exception e) {
            logger.error("获取k线数据symbol{} ,period {} ,size {} 失败", marketConfig.markName(),
                    klineConfig.period(),
                    klineConfig.size());
            throw new ApiException(e);
        }
        return null;
    }
}
