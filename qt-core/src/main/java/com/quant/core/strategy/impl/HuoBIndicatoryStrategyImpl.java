package com.quant.core.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.quant.common.config.RedisUtil;
import com.quant.common.constans.RobotRedisKeyConfig;
import com.quant.common.enums.OrderType;
import com.quant.common.domain.response.Kline;
import com.quant.common.domain.response.OrdersDetail;
import com.quant.common.domain.to.BuyAndSellIndicatorTo;
import com.quant.common.domain.to.llIndicatorTo;
import com.quant.common.domain.vo.BaseInfoEntity;
import com.quant.common.domain.vo.ProfitMessage;
import com.quant.common.enums.HBOrderType;
import com.quant.common.exception.ExchangeNetworkException;
import com.quant.common.exception.TradingApiException;
import com.quant.core.config.AccountConfig;
import com.quant.core.config.KlineConfig;
import com.quant.core.config.MarketConfig;
import com.quant.core.config.StrategyConfig;
import com.quant.core.config.imp.HuoBiKlineConfigImpl;
import com.quant.core.factory.IndicatorFactory;
import com.quant.core.helpers.IndicatorHelper;
import com.quant.core.redisMq.OrderIdRedisMqServiceImpl;
import com.quant.core.redisMq.OrderProfitRedisMqServiceImpl;
import com.quant.core.redisMq.RobotLogsRedisMqServiceImpl;
import com.quant.core.strategy.AbstractStrategy;
import com.quant.core.strategy.StrategyException;
import com.quant.core.strategy.TradingStrategy;
import com.quant.core.strategy.handle.*;
import com.quant.core.trading.*;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.PrecisionNum;
import org.ta4j.core.trading.rules.StopGainRule;
import org.ta4j.core.trading.rules.StopLossRule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 火币 指标策略
 *
 * @author yangyangchu
 * @Date 19.4.15
 */
@Slf4j
public class HuoBIndicatoryStrategyImpl extends AbstractStrategy implements TradingStrategy, ProfitCall {

    //精确到小数点的个数
    private static final int decimalPoint = 4;

    private llIndicatorTo config;


    private BaseInfoEntity baseInfo;
    /**
     * 检测买入指标是否只有一条规则
     */
    private static final ThreadLocal<Boolean> buyOnlyOneRule = new ThreadLocal<>();

    /**
     * 检测卖出指标是否只有一条规则
     */
    private static final ThreadLocal<Boolean> sellOnlyOneRule = new ThreadLocal<>();


    public HuoBIndicatoryStrategyImpl(RedisUtil redisUtil, Integer robotId) {
        this.redisUtil = redisUtil;
        this.robotId = robotId;
        this.startkey = RobotRedisKeyConfig.getRobotIsStartStateKey() + robotId;
        this.isRunKey = RobotRedisKeyConfig.getRobotIsRunStateKey() + robotId;
    }

    @Override
    public void init(TradingApi tradingApi, MarketConfig market, StrategyConfig config, AccountConfig accountConfig) {
        log.info("===============初始化参数" + config.getIndicatorStrategy().toString());
        this.strategyConfig = config;
        this.tradingApi = tradingApi;
        this.marketConfig = market;
        this.accountConfig = accountConfig;
        this.config = config.getIndicatorStrategy();
        this.baseInfo = this.config.getBaseInfo();
        this.orderState = new OrderState();
        this.redisMqService = new RobotLogsRedisMqServiceImpl(this.redisUtil, this.robotId, Integer.parseInt(this.accountConfig.getUserId()));
        this.orderMqService = new OrderIdRedisMqServiceImpl(this.redisUtil, accountConfig, robotId);
        this.orderProfitService = new OrderProfitRedisMqServiceImpl(this.redisUtil);
    }

    @Override
    public void execute() throws StrategyException {
        init();
        TradingRecord tradingRecord = builderTradingRecord();
        TimeSeries timeSeries = IndicatorHelper.buildSeries(builderLine());
        Strategy strategy = buildStrategyByConfig(timeSeries, this.config.getBaseData());
        if (strategy == null) {
            throw new StrategyException(new IllegalArgumentException("策略未设置！机器人退出任务。。。"));
        }
        //开始时候的时间
        ZonedDateTime beginTime = timeSeries.getBar(timeSeries.getEndIndex()).getEndTime();
        // 任务循环执行
        while (true) {
            try {
                //设置机器人的运行状态 在休眠+20s之后没响应 就认为该机器人已经死亡
                redisUtil.set(isRunKey, "running", (long) (baseInfo.getSleep() + 20));
                // 这里每次获取k line 取最新的一条 计算指标的变化
                List<Kline> klineList = getKlines(tradingApi, marketConfig, this.config.getBaseData().getKline(), "10");
                if (klineList == null || klineList.isEmpty()) {
                    continue;
                }
                Kline line = klineList.get(0);
                ZonedDateTime nowTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli((line.getId()) * 1000), ZoneId.systemDefault());
                Bar newBar = new BaseBar(nowTime, line.getOpen(), line.getHigh(), line.getLow(), line.getClose(), line.getVol(), timeSeries.function());
                if (nowTime.isAfter(beginTime)) {
                    timeSeries.addBar(newBar);
                    beginTime = nowTime;
                } else {
                    timeSeries.addBar(newBar, true);
                }
                int endIndex = timeSeries.getEndIndex();
                //判断买卖
                if (strategy.shouldExit(endIndex, tradingRecord) && orderState.getType() == OrderType.BUY) {
                    //查看是否达到卖的信号
                    boolean exit = tradingRecord.exit(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(1));
                    if (exit) {
                        try {
                            createSellOrder();
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("下单失败{},{}", this.orderState.toString(), e.getMessage());
                            redisMqService.sendMsg("当前下单信息【" + this.orderState.toString() + "】==下单失败 重新下单！");
                            continue;
                        }
                        setStatus(OrderType.SELL, new BigDecimal(newBar.getClosePrice().doubleValue()));
                        log.error("add sell order price is {}", newBar.getClosePrice().doubleValue());
                    }

                }
                //查看是否到达买的信号
                else if (strategy.shouldEnter(endIndex, tradingRecord) && orderState.getType() == OrderType.SELL) {

                    boolean enter = tradingRecord.enter(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(1));
                    if (enter) {
                        try {
                            createBuyOrder();
                        } catch (Exception e) {
                            e.printStackTrace();
                            redisMqService.sendMsg("当前下单信息【" + this.orderState.toString() + "】==下单失败 重新下单！");
                            log.error("下单失败{},{}", this.orderState.toString(), e.getMessage());
                            continue;
                        }
                        log.error("add buy order price is {}", newBar.getClosePrice().doubleValue());
                        setStatus(OrderType.BUY, new BigDecimal(newBar.getClosePrice().doubleValue()));

                    }

                } else if (strategy.shouldEnter(endIndex, tradingRecord) && orderState.getType() == null) {
                    //查看当前订单状态 订单不存在的情况下 首先要出现买的信号 有了买的信号 进行购买

                    boolean enter = tradingRecord.enter(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(1));
                    if (enter) {
                        try {
                            createBuyOrder();
                        } catch (Exception e) {
                            redisMqService.sendMsg("当前下单信息【" + this.orderState.toString() + "】==下单失败 重新下单！");
                            log.error("下单失败{},{}", this.orderState.toString(), e.getMessage());
                            continue;
                        }
                        log.error("add buy order price is {}", newBar.getClosePrice().doubleValue());
                        setStatus(OrderType.BUY, new BigDecimal(newBar.getClosePrice().doubleValue()));
                    }

                }
                try {
                    //机器人休眠
                    Thread.sleep((long) (baseInfo.getSleep() * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!checkRobotIsStop(startkey)) {
                    redisMqService.sendMsg("机器人被取消任务!!!退出ing");
                    break;
                }

            } catch (Exception e) {
                log.error("机器人{}运行中发生异常：异常信息{}", robotId, e.getMessage());
                redisMqService.sendMsg("机器人运行中发生异常：异常信息" + e.getMessage());
            } finally {
                if (this.orderState.getType() != null) {
                    //记录当前机器人的最后一次状态 用于下一次恢复数据
                    redisUtil.set(lastOrderState + robotId, JSON.toJSONString(this.orderState));
                }
            }
        }

    }

    private TradingRecord builderTradingRecord() {
        TradingRecord tradingRecord = new BaseTradingRecord();
        if (this.orderState.getType() != null) {
            //最近的2000条k线数据 计算指标
            if (this.orderState.getType() == OrderType.BUY) {
                tradingRecord = new BaseTradingRecord();
                if (this.orderState.getPrice() == null) {
                    tradingRecord.enter(1999, null, null);
                } else {
                    tradingRecord.enter(1999, PrecisionNum.valueOf(this.orderState.getPrice().setScale(pricePrecision, RoundingMode.DOWN).doubleValue()), PrecisionNum.valueOf(1));
                }
            }
            if (this.orderState.getType() == OrderType.SELL) {
                tradingRecord = new BaseTradingRecord(Order.OrderType.SELL);
            }
        }
        return tradingRecord;
    }

    private List<Kline> builderLine() {

        List<Kline> klines;
        while (true) {
            //获取最大的k线数据
            try {
                klines = getKlines(tradingApi, marketConfig, this.config.getBaseData().getKline(), "2000");
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                continue;
            }
            if (klines == null) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                continue;
            }
            break;
        }
        return klines;
    }

    private void setStatus(OrderType type, BigDecimal price) {
        this.orderState.setType(type);
        this.orderState.setPrice(price);
    }

    private void setStatus(OrderType type, BigDecimal price, Long id) {
        this.orderState.setType(type);
        this.orderState.setPrice(price);
        this.orderState.setId(id);
    }

    private Strategy buildStrategyByConfig(TimeSeries series, BuyAndSellIndicatorTo to) {
        IndicatorFactory factory = new IndicatorFactory(series);
        //是否只有一条规则
        buyOnlyOneRule.set(true);
        sellOnlyOneRule.set(true);

        Rule entry = null;
        for (BuyAndSellIndicatorTo.IndicatorBuyBean buyBean : to.getIndicatorBuy()) {
            //指标名称
            entry = IndicatorHelper.simpleBuilder(buyBean, factory, series, entry, buyOnlyOneRule);
        }
        //构建卖出
        Rule exit = null;
        for (BuyAndSellIndicatorTo.IndicatorSellBean sellBean : to.getIndicatorSell()) {
            exit = IndicatorHelper.simpleBuilder(sellBean, factory, series, exit, sellOnlyOneRule);
        }
        if (entry == null || exit == null) {
            log.error("构建策略失败!!! {}", JSON.toJSONString(to));
            return null;
        }
        //构建止盈止损
        if (to.getStopGain() != null && to.getStopGain() != 0) {
            //给卖出条件增加止盈
            StopGainRule stopGainRule = new StopGainRule(new ClosePriceIndicator(series), to.getStopGain());
            exit = exit.or(stopGainRule);

        }
        if (to.getStopLoss() != null && to.getStopLoss() != 0) {
            //给卖出条件增加止损
            StopLossRule stopLossRule = new StopLossRule(new ClosePriceIndicator(series), to.getStopLoss());
            exit = exit.or(stopLossRule);

        }
        //构建策略
        return new BaseStrategy(entry, exit);
    }


    /**
     * 检查订单 如果订单没有撮合成功 直接取消
     * 返回true 可以执行下单操作
     * 返回false 不可执行下单操作
     *
     * @param tradingApi
     */
    private boolean checkOrder(TradingApi tradingApi) {
        log.info("当前订单状态{}", this.orderState.toString());
        if (this.orderState.getId() == null) {
            log.info("当前账户{}没有任何订单,开始下单", accountConfig.accountId());
            return true;
        }
        try {
            List<OpenOrder> openOrders = tradingApi.getOpenOrders(this.marketConfig, this.accountConfig, "10");
            Optional<OpenOrder> first = openOrders.stream().filter(openOrder -> openOrder.getId().equals(String.valueOf(this.orderState.getId()))).findFirst();
            if (first.isPresent()) {
                redisMqService.sendMsg("当前订单状态:【" + this.orderState.getType().getStringValue() + "】======");
                //取消刚刚下的订单
                boolean cancel = tradingApi.cancelOrder(first.get().getId(), first.get().getMarketId());
                if (cancel) {
                    redisMqService.sendMsg("查询到未成功的订单,orderId【" + first.get().getId() + "】, 取消【" + this.orderState.getType().getStringValue() + "】订单成功!!!");
                    if (this.orderState.getType() == OrderType.BUY) {
                        //如果当前订单是购买订单  取消了 应该继续购买
                        setStatus(null, first.get().getPrice(), null);
                    }
                    if (this.orderState.getType() == OrderType.SELL) {
                        //如果是卖出 应该继续卖出
                        setStatus(OrderType.BUY, first.get().getPrice(), null);
                    }
                    return false;
                }
            } else {
                //将成功的订单信息传回admin
                return messageBackAdmin(this);
            }
        } catch (ExchangeNetworkException | TradingApiException e) {
            log.error("账户{}取消订单失败{}", this.accountConfig.accountId(), e.getMessage());
            e.printStackTrace();
            return false;
        }
        return false;
    }


    /**
     * 计算盈利
     */
    public void CalculateProfit() {
        try {
            // 一买一卖才会出现
            if (this.orderState.getType() == OrderType.SELL) {

                Object o = this.redisUtil.lPop(orderProfitIds + robotId);
                if (o == null) {
                    log.info("当前redis 订单id 队列 暂无数据============== ");
                    return;
                }
                log.info("当前订单id 队列取出来的值是{}", o.toString());
                String current = o.toString();
                String[] currentIdAndType = current.split("_");
                if (currentIdAndType.length != 2) {
                    log.error("redis订单id队列存储异常数据{}", current);
                    return;
                }
                if (currentIdAndType[1].equals(OrderType.BUY.getStringValue())) {
                    //如果当前的是购买订单 不计算盈利 重新将值赋值到redis
                    this.redisUtil.lPush(orderProfitIds + robotId, o);
                    return;
                }

                long buyOrderId, sellOrderId;
                BigDecimal allBuyBalance, allSellBalance, buyAmount, sellAmount,
                        buyPrice, sellPrice, diff, divide;
                //获取上一次的购买金额和数量
                Object last = this.redisUtil.lPop(orderProfitIds + robotId);
                if (last == null) {
                    log.error("获取订单id队列上一次的购买记录错误");
                    return;
                }
                log.info("上一次订单id 队列取出来的值是{}", last.toString());

                String[] lastIdAndType = last.toString().split("_");

                //如果当前的订单是市价单 计算盈亏 需要查询这个订单的详情信息
                buyOrderId = Long.parseLong(lastIdAndType[0]);
                sellOrderId = Long.parseLong(currentIdAndType[0]);
                log.info("当前出售订单id{},当前购买订单id{}", sellOrderId, buyOrderId);
                //订单详情
                OrdersDetail ordersBuyDetail, ordersSellDetail;
                //获取订单详情
                ordersBuyDetail = getOrderDetail(buyOrderId, 0);
                if (ordersBuyDetail == null) {
                    log.info("获取购买订单为null");
                    return;
                }
                if (new BigDecimal(ordersBuyDetail.getFieldCashAmount()).compareTo(BigDecimal.ZERO) == 0) {
                    return;
                }
                ordersSellDetail = getOrderDetail(sellOrderId, 0);
                if (ordersSellDetail == null) {
                    log.info("获取出售订单为null");
                    return;
                }
                if (new BigDecimal(ordersSellDetail.getFieldCashAmount()).compareTo(BigDecimal.ZERO) == 0) {
                    return;
                }
                //如果是市价的情况
                Profit profit = new Profit(ordersBuyDetail, ordersSellDetail, orderState).invoke();
                allSellBalance = profit.getAllSellBalance();
                allBuyBalance = profit.getAllBuyBalance();
                buyAmount = profit.getBuyAmount();
                sellAmount = profit.getSellAmount();
                buyPrice = profit.getBuyPrice();
                sellPrice = profit.getSellPrice();
                //计算盈亏率 卖出总金额-买入总金额 除以 买入总金额
                diff = allSellBalance.subtract(allBuyBalance).setScale(pricePrecision, RoundingMode.DOWN);
                log.info("当前的订单状态{},计算后的差价{}", this.orderState.getType().getStringValue(), diff);
                divide = diff.divide(allBuyBalance, decimalPoint, RoundingMode.DOWN);
                log.info("盈亏率:{}", divide);
                profitMessage(buyOrderId, sellOrderId, buyAmount, sellAmount, buyPrice, sellPrice, diff, divide, ordersBuyDetail, ordersSellDetail);

            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            log.error("计算盈亏率发生异常{}", e.getMessage());
        }

    }

    private void profitMessage(long buyOrderId, long sellOrderId, BigDecimal buyAmount, BigDecimal sellAmount, BigDecimal buyPrice, BigDecimal sellPrice, BigDecimal diff, BigDecimal divide, OrdersDetail ordersBuyDetail, OrdersDetail ordersSellDetail) {
        ProfitMessage profitMessage = new ProfitMessage();
        profitMessage.setBuyOrderId(buyOrderId);
        profitMessage.setSellOrderId(sellOrderId);
        profitMessage.setRobot_id(robotId);
        profitMessage.setBuyAmount(buyAmount);
        profitMessage.setSellAmount(sellAmount);
        profitMessage.setDiff(diff);
        profitMessage.setDivide(divide);
        profitMessage.setBuyPrice(buyPrice);
        profitMessage.setSellPrice(sellPrice);
        profitMessage.setBuyCashAmount(new BigDecimal(ordersBuyDetail.getFieldCashAmount()).setScale(pricePrecision, RoundingMode.DOWN));
        profitMessage.setSellCashAmount(new BigDecimal(ordersSellDetail.getFieldCashAmount()).setScale(pricePrecision, RoundingMode.DOWN));
        orderProfitService.sendMsg(profitMessage);
    }


    /**
     * 创建购买订单
     */
    private void createBuyOrder() {
        if (!checkOrder(tradingApi)) {
            log.info("不创建订单");
            return;
        }
        //当前无订单 创建购买订单
        BigDecimal buyAmount, buyPrice;

        HBOrderType HBOrderType;
        //获取余额
        if (!getBalance()) {
            redisMqService.sendMsg("未获取账户【" + this.accountConfig.accountId() + "】的余额信息！！！");
            return;
        }
        if (this.quotaBalance.compareTo(BigDecimal.ZERO) < 0) {
            redisMqService.sendMsg("账户【" + this.accountConfig.accountId() + "】没有余额,请及时充值=======");
            return;
        }
        //是否是限价
        StrategyHandle strategyHandle = new HuobiLimitBuyPriceHandle(new HuobiNotLimitBuyPriceHandle(null));
        StrategyHandle.HandleResult handleResult = strategyHandle.strategyRequest(tradingApi, marketConfig, strategyConfig, accountConfig, pricePrecision, amountPrecision, baseBalance);
        if (handleResult != null) {

            HBOrderType = handleResult.getHbOrderType();
            buyPrice = handleResult.getPrice();
            buyAmount = handleResult.getAmount();
            //设置当前订单状态为购买
            OrderType type = OrderType.BUY;
            //记录当前的价格和数量
            orderPlace(tradingApi, buyAmount, buyPrice, HBOrderType, type);
        }


    }

    /**
     * 创建卖出订单
     * 检查上一次的买入订单是否成功 如果不成功就取消订单
     */
    private void createSellOrder() {
        if (!checkOrder(tradingApi)) {
            return;
        }
        BigDecimal sellAmount, sellPrice;
        HBOrderType HBOrderType;
        if (!getBalance()) {
            return;
        }
        if (this.baseBalance.compareTo(BigDecimal.ZERO) < 0) {
            log.info("账户{},{}没有余额 请及时充值", this.accountConfig.accountId(), this.baseCurrency);
            redisMqService.sendMsg("账户id【" + this.accountConfig.accountId() + "】,【" + this.baseCurrency + "】没有余额 请及时充值");
            return;
        }
        //是否是限价
        StrategyHandle strategyHandle = new HuobiLimitSellPriceHandle(new HuobiNotLimitSellPriceHandle(null));
        StrategyHandle.HandleResult handleResult = strategyHandle.strategyRequest(tradingApi, marketConfig, strategyConfig, accountConfig, pricePrecision, amountPrecision, baseBalance);
        //获取结果
        if (handleResult != null) {
            HBOrderType = handleResult.getHbOrderType();
            sellPrice = handleResult.getPrice();
            sellAmount = handleResult.getAmount();
            //设置当前订单状态为卖出
            OrderType type = OrderType.SELL;
            orderPlace(tradingApi, sellAmount, sellPrice, HBOrderType, type);
        }

    }

    /**
     * 记录订单状态和每次的交易额和数量
     *
     * @param tradingApi
     * @param sellAmount
     * @param sellPrice
     * @param HBOrderType
     * @param type
     */
    private void orderPlace(TradingApi tradingApi, BigDecimal sellAmount, BigDecimal sellPrice, HBOrderType HBOrderType, OrderType type) {
        this.orderState.setAmount(sellAmount);
        this.orderState.setPrice(sellPrice);
        this.orderState.setHBOrderType(HBOrderType);
        this.order(sellAmount, sellPrice, HBOrderType, tradingApi, type);
    }


    @Override
    protected void buyCalculation() {

        // 这里不适用

    }

    @Override
    protected void sellCalculation() {
        // 这里不适用
    }

    private List<Kline> getKlines(TradingApi tradingApi,
                                  MarketConfig marketConfig,
                                  String buyKline,
                                  String size) {
        KlineConfig klineConfig = new HuoBiKlineConfigImpl(size, buyKline);
        List<Kline> lines = null;
        try {
            lines = tradingApi.getKline(marketConfig, klineConfig);
        } catch (Exception e) {
            log.error("获取k线失败:{}", e.getMessage());
            redisMqService.sendMsg("获取k线数据失败");
        }
        return lines;
    }


}
