package com.quant.core.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.quant.common.config.RedisUtil;
import com.quant.common.constans.RobotRedisKeyConfig;
import com.quant.common.domain.response.Kline;
import com.quant.common.domain.response.OrdersDetail;
import com.quant.common.domain.to.BuyAndSellIndicatorTo;
import com.quant.common.domain.to.llIndicatorTo;
import com.quant.common.domain.vo.BaseInfoEntity;
import com.quant.common.domain.vo.ProfitMessage;
import com.quant.common.domain.vo.StrategyVo;
import com.quant.core.config.AccountConfig;
import com.quant.core.config.KlineConfig;
import com.quant.core.config.MarketConfig;
import com.quant.core.config.StrategyConfig;
import com.quant.core.config.imp.HuoBiKlineConfigImpl;
import com.quant.core.factory.IndicatorFactory;
import com.quant.core.helpers.IndicatorHelper;
import com.quant.core.redisMq.OrderIdRedisMqServiceImpl;
import com.quant.core.redisMq.OrderProfitRedisMqServiceImpl;
import com.quant.core.redisMq.RedisMqService;
import com.quant.core.redisMq.RobotLogsRedisMqServiceImpl;
import com.quant.core.strategy.AbstractStrategy;
import com.quant.core.strategy.StrategyException;
import com.quant.core.strategy.TradingStrategy;
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
public class HuoBIndicatoryStrategyImpl extends AbstractStrategy implements TradingStrategy {

    //精确到小数点的个数
    private static final int decimalPoint = 4;

    private llIndicatorTo config;

    private long runTimes = 0;


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
        TradingRecord tradingRecord;
        if (this.orderState.getType() == null) {
            tradingRecord = new BaseTradingRecord();
        } else {
            Order.OrderType type = Order.OrderType.BUY;
            if (this.orderState.getType() == OrderType.BUY) {
                type = Order.OrderType.BUY;
            }
            if (this.orderState.getType() == OrderType.SELL) {
                type = Order.OrderType.SELL;
            }
            tradingRecord = new BaseTradingRecord(type);
        }
        //获取最大的k线数据
        List<Kline> klines = getKlines(tradingApi, marketConfig, this.config.getBaseData().getKline(), "2000");
        TimeSeries timeSeries = IndicatorHelper.buildSeries(klines);
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
                List<Kline> klineList = getKlines(tradingApi, marketConfig, this.config.getBaseData().getKline(), "10");
                if (klineList == null || klineList.size() <= 0) {
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
                if (strategy.shouldExit(endIndex,tradingRecord) && orderState.getType() == OrderType.BUY) {
                    //查看是否达到卖的信号
                    try {
                        this.orderState.setType(OrderType.SELL);
                        tradingRecord.exit(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(1));
//                        createSellOrder();
                        log.error("add sell order price is {}", newBar.getClosePrice().doubleValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("下单失败{},{}", this.orderState.toString(), e.getMessage());
                        redisMqService.sendMsg("当前下单信息【" + this.orderState.toString() + "】==下单失败 重新下单！");
                    }
                }
                //查看是否到达买的信号
                else if (strategy.shouldEnter(endIndex,tradingRecord) && orderState.getType() == OrderType.SELL) {
                    try {
                        tradingRecord.enter(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(1));
//                        createBuyOrder();
                        log.error("add buy order price is {}", newBar.getClosePrice().doubleValue());
                    } catch (Exception e) {
                        redisMqService.sendMsg("当前下单信息【" + this.orderState.toString() + "】==下单失败 重新下单！");
                        e.printStackTrace();
                        log.error("下单失败{},{}", this.orderState.toString(), e.getMessage());
                    }
                } else if (strategy.shouldEnter(endIndex,tradingRecord) && orderState.getType() == null) {
                    //查看当前订单状态 订单不存在的情况下 首先要出现买的信号 有了买的信号 进行购买
                    try {
                        this.orderState.setType(OrderType.BUY);
                        tradingRecord.enter(endIndex, newBar.getClosePrice(), PrecisionNum.valueOf(1));
                        log.error("add buy order price is {}", newBar.getClosePrice().doubleValue());
//                        createBuyOrder();
                    } catch (Exception e) {
                        redisMqService.sendMsg("当前下单信息【" + this.orderState.toString() + "】==下单失败 重新下单！");
                        e.printStackTrace();
                        log.error("下单失败{},{}", this.orderState.toString(), e.getMessage());
                    }
                }
                try {
                    ++runTimes;
//                    redisMqService.sendMsg("机器人已经运行了>>>" + runTimes + "次");
//                    log.info("=========================================");
                    //休眠几秒
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
                //记录当前机器人的最后一次状态
                redisUtil.set(lastOrderState + robotId, JSON.toJSONString(this.orderState));
            }
        }

    }

    Strategy buildStrategyByConfig(TimeSeries series, BuyAndSellIndicatorTo to) {
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
            exit = exit.and(stopGainRule);

        }
        if (to.getStopLoss() != null && to.getStopLoss() != 0) {
            //给卖出条件增加止损
            StopLossRule stopLossRule = new StopLossRule(new ClosePriceIndicator(series), to.getStopLoss());
            exit = exit.and(stopLossRule);

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
                    redisMqService.sendMsg("查询到未成功的订单开始取消订单,orderId【" + first.get().getId() + "】, 取消【" + this.orderState.getType().getStringValue() + "】订单成功!!!");
                    if (this.orderState.getType() == OrderType.BUY) {
                        //如果当前订单是购买订单  取消了 应该继续购买
                        this.orderState.setType(null);
                    }
                    if (this.orderState.getType() == OrderType.SELL) {
                        //如果是卖出 应该继续卖出
                        this.orderState.setType(OrderType.BUY);
                    }
                    this.orderState.setId(null);
                    return false;
                }
            } else {
                //将成功的订单信息传回admin
                orderMqService.sendMsg(this.orderState.getId());
                String result = this.orderState.getId() + "_" + this.orderState.getType().getStringValue();
                this.redisUtil.lPush(orderProfitIds + robotId, result);
                this.CalculateProfit();
                return true;
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
    private void CalculateProfit() {
        try {
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
                if (this.orderState.getOrderType() == com.quant.common.enums.OrderType.SELL_MARKET) {
                    //上一次购买的交易额就是总金额
                    allBuyBalance = new BigDecimal(ordersBuyDetail.getFieldCashAmount()).setScale(pricePrecision, RoundingMode.DOWN);
                    allSellBalance = new BigDecimal(ordersSellDetail.getFieldCashAmount()).setScale(pricePrecision, RoundingMode.DOWN);
                    buyAmount = new BigDecimal(ordersBuyDetail.getAmount()).setScale(8, RoundingMode.DOWN);
                    sellAmount = new BigDecimal(ordersSellDetail.getAmount()).setScale(8, RoundingMode.DOWN);
                    //市价购买价格 按照 已经成交的金额除以已经成交的数量
                    buyPrice = new BigDecimal(ordersBuyDetail.getFieldCashAmount()).divide(new BigDecimal(ordersBuyDetail.getFieldAmount()), pricePrecision, RoundingMode.DOWN);
                    sellPrice = new BigDecimal(ordersSellDetail.getFieldCashAmount()).divide(new BigDecimal(ordersSellDetail.getFieldAmount()), pricePrecision, RoundingMode.DOWN);
                } else {
                    buyPrice = new BigDecimal(ordersBuyDetail.getPrice()).setScale(pricePrecision, RoundingMode.DOWN);
                    sellPrice = new BigDecimal(ordersSellDetail.getPrice()).setScale(pricePrecision, RoundingMode.DOWN);
                    buyAmount = new BigDecimal(ordersBuyDetail.getAmount()).setScale(amountPrecision, RoundingMode.DOWN);
                    sellAmount = new BigDecimal(ordersSellDetail.getAmount()).setScale(amountPrecision, RoundingMode.DOWN);
                    //计算上一次买的总的金额
                    allBuyBalance = new BigDecimal(ordersBuyDetail.getFieldCashAmount());
                    //计算卖的总金额
                    allSellBalance = new BigDecimal(ordersSellDetail.getFieldCashAmount());
                }
                //计算盈亏率 卖出总金额-买入总金额 除以 买入总金额
                diff = allSellBalance.subtract(allBuyBalance).setScale(pricePrecision, RoundingMode.DOWN);
                log.info("当前的订单状态{},计算后的差价{}", this.orderState.getType().getStringValue(), diff);
                divide = diff.divide(allBuyBalance, decimalPoint, RoundingMode.DOWN);
                log.info("盈亏率:{}", divide);
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
        } catch (NumberFormatException e) {
            e.printStackTrace();
            log.error("计算盈亏率发生异常{}", e.getMessage());
        }

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
        BigDecimal buyAmount = BigDecimal.ZERO;
        BigDecimal buyPrice = BigDecimal.ZERO;
        com.quant.common.enums.OrderType orderType;
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
        if (baseInfo.getIsLimitPrice() == 1) {
            MarketOrder marketOrder = tradingApi.getMarketOrders(marketConfig, "500");
            //是限价的方式购买 需要计算价格
            BigDecimal currentBuyPrice = marketOrder.getBuy().get(0).getPrice();
            //计算购买的价格
            buyPrice = baseInfo.getBuyPrice().add(currentBuyPrice).setScale(pricePrecision, RoundingMode.DOWN);
            log.info("限价购买计算价格:当前市场最新的买入的价格:{},计算后的订单价格:{}", currentBuyPrice, buyPrice);
            redisMqService.sendMsg("=========当前策略交易方式:限价交易========");
            redisMqService.sendMsg("限价购买价格计算:当前市场最新买入价格:【" + currentBuyPrice + "】,策略计算后的订单价格:【" + buyPrice + "】");
            //计算购买的数量 是否全部买
            BigDecimal aviAmount = quotaBalance.divide(buyPrice, amountPrecision, RoundingMode.DOWN);
            if (baseInfo.getIsAllBuy() == 1) {
                //计算购买的数量 当前可用余额除以购买价格
                buyAmount = aviAmount;
                log.info("限价购买计算数量:全部购买,购买数量为{}", buyAmount);
                redisMqService.sendMsg("=========当前策略购买方式:全部购买========");
                redisMqService.sendMsg("限价全部购买计算数量后为【" + buyAmount + "】");
            } else {
                //不是全部购买 自定义购买数量
                buyAmount = buyAmount.add(baseInfo.getBuyAmount().setScale(amountPrecision, RoundingMode.DOWN));
                if (buyAmount.compareTo(aviAmount) > 0) {
                    log.info("限价自定义购买:账户{}的余额不足,需要充值......", accountConfig.accountId());
                    redisMqService.sendMsg("限价自定义购买:账户【" + accountConfig.accountId() + "】的余额不足,需要充值......");
                    return;
                }
                log.info("限价自定义购买数量:账户{},购买数量{}", accountConfig.accountId(), buyAmount);
                redisMqService.sendMsg("限价自定义购买数量为【" + buyAmount + "】");
            }
            //设置当前订单的type 为限价买入
            orderType = com.quant.common.enums.OrderType.BUY_LIMIT;
        } else {
            redisMqService.sendMsg("=========当前策略交易方式:市价购买交易========");
            //市价买 价格直接填0 交易额的精度固定为8
            //计算购买的数量 是否全部买
            if (baseInfo.getIsAllBuy() == 1) {
                redisMqService.sendMsg("=========当前策略购买方式:全部购买========");
                //如果市价全部买 就是价格就是交易额度 quotaBalance; 默认固定为8位
                buyAmount = this.quotaBalance.setScale(8, RoundingMode.DOWN);
                if (buyAmount.compareTo(BigDecimal.ONE) < 0) {
                    redisMqService.sendMsg("市价交易,交易额不能低于1个");
                    log.info("市价全部购买交易额不能低于1");
                    return;
                }
                redisMqService.sendMsg("市价全部购买,交易额为:" + buyAmount);
            } else {
                //不是全部购买 自定义交易额 购买
                buyAmount = baseInfo.getBuyQuotaPrice().setScale(8, RoundingMode.DOWN);
                if (buyAmount.compareTo(BigDecimal.ONE) < 0) {
                    redisMqService.sendMsg("市价自定义购买数量:账户【" + accountConfig.accountId() + "】余额不足,需要充值......");
                    log.info("市价自定义购买交易额:账户{}的余额不足,需要充值......", accountConfig.accountId());
                    return;
                }
                log.info("市价自定义购买交易额:账户id{},购买数量{}", accountConfig.accountId(), buyAmount);
                redisMqService.sendMsg("市价自定义购买数量为【" + buyAmount + "】");
            }
            orderType = com.quant.common.enums.OrderType.BUY_MARKET;
        }

        //设置当前订单状态为购买
        OrderType type = OrderType.BUY;
        //记录当前的价格和数量
        orderPlace(tradingApi, buyAmount, buyPrice, orderType, type);
    }

    /**
     * 创建卖出订单
     * 检查上一次的买入订单是否成功 如果不成功就取消订单
     */
    private void createSellOrder() {
        if (!checkOrder(tradingApi)) {
            return;
        }
        BigDecimal sellAmount = BigDecimal.ZERO;
        BigDecimal sellPrice = BigDecimal.ZERO;
        com.quant.common.enums.OrderType orderType;
        if (!getBalance()) {
            return;
        }
        if (this.baseBalance.compareTo(BigDecimal.ZERO) < 0) {
            log.info("账户{},{}没有余额 请及时充值", this.accountConfig.accountId(), this.baseCurrency);
            redisMqService.sendMsg("账户id【" + this.accountConfig.accountId() + "】,【" + this.baseCurrency + "】没有余额 请及时充值");
            return;
        }
        //是否是限价
        if (baseInfo.getIsLimitPrice() == 1) {
            //从当前的20个卖出订单里找出最优的价格 （限价卖出）
            MarketOrder marketOrder = tradingApi.getMarketOrders(marketConfig, "500");
            BigDecimal currentSellPrice = marketOrder.getSell().get(0).getPrice();
            //计算卖出的价格
            sellPrice = baseInfo.getSellPrice().add(currentSellPrice).setScale(pricePrecision, RoundingMode.UP);
            log.info("限价卖出:当前卖出的价格:{},计算后的订单卖出价格:{}", currentSellPrice, sellPrice);
            redisMqService.sendMsg("限价卖出:当前卖出的价格:【" + currentSellPrice + "】,计算后的订单卖出价格:【" + sellPrice + "】");
            //计算购买的数量 是否全部卖出
            if (baseInfo.getIsAllSell() == 1) {
                //从用户api的表里查询到他的账户相应的base 火币的数量全部购买
                sellAmount = sellAmount.add(this.baseBalance).setScale(this.amountPrecision, RoundingMode.DOWN);
                log.info("限价全部卖出:账户id{}卖出数量为{}", this.accountConfig.accountId(), sellAmount);
                redisMqService.sendMsg("限价全部卖出:账户id【" + this.accountConfig.accountId() + "】卖出数量为【" + sellAmount + "】");
            } else {
                sellAmount = sellAmount.add(this.baseInfo.getSellAmount().setScale(this.amountPrecision, RoundingMode.DOWN));
                if (sellAmount.compareTo(this.baseBalance) > 0) {
                    log.info("限价自定义卖出余额{}大于账户余额{}:账户id{}的余额不足,需要充值......", sellAmount, this.baseBalance, accountConfig.accountId());
                    redisMqService.sendMsg("限价自定义卖出余额【" + sellAmount + "】大于账户余额【" + this.baseBalance + "】>>>账户id【" + this.accountConfig.accountId() + "】的余额不足,需要充值......");
                    return;
                }
                log.info("限价自定义卖出数量:账户id{},卖出数量{}", this.accountConfig.accountId(), sellAmount);
                redisMqService.sendMsg("限价自定义卖出数量:账户id【" + this.accountConfig.accountId() + "】,卖出数量【" + sellAmount + "】");
            }
            orderType = com.quant.common.enums.OrderType.SELL_LIMIT;
        } else {
            //市价卖出 价格直接填0 计算交易额度
            if (baseInfo.getIsAllSell() == 1) {
                //如果市价全部卖出 卖出为币 卖出所有的币 就是价格就是交易额度 quotaBalance;
                sellAmount = this.baseBalance.setScale(this.amountPrecision, RoundingMode.DOWN);
                log.info("市价全部卖出,卖出数量{}", sellAmount);
                redisMqService.sendMsg("市价全部卖出,卖出数量【" + sellAmount + "】");
            } else {
                //不是全部购买 自定义交易额 购买
                sellAmount = sellAmount.add(baseInfo.getSellAmount()).setScale(this.amountPrecision, RoundingMode.DOWN);
                if (sellAmount.compareTo(this.baseBalance) > 0) {
                    log.info("市价自定义卖出:账户id{}的余额不足,需要充值......", this.accountConfig.accountId());
                    redisMqService.sendMsg("市价自定义卖出:账户id【" + this.accountConfig.accountId() + "】的余额不足,需要充值......");
                    return;
                }
                log.info("市价自定义卖出数量:账户id{},卖出币种{},数量{}", this.accountConfig.accountId(), this.baseCurrency, sellAmount);
                redisMqService.sendMsg("市价自定义卖出数量:账户id【" + this.accountConfig.accountId() + "】,卖出币种【" + this.baseCurrency + "】,数量【" + sellAmount + "】");
            }
            orderType = com.quant.common.enums.OrderType.SELL_MARKET;
        }
        //设置当前订单状态为卖出
        OrderType type = OrderType.SELL;
        orderPlace(tradingApi, sellAmount, sellPrice, orderType, type);
    }

    /**
     * 记录订单状态和每次的交易额和数量
     *
     * @param tradingApi
     * @param sellAmount
     * @param sellPrice
     * @param orderType
     * @param type
     */
    private void orderPlace(TradingApi tradingApi, BigDecimal sellAmount, BigDecimal sellPrice, com.quant.common.enums.OrderType orderType, OrderType type) {

        this.orderState.setAmount(sellAmount);
        this.orderState.setPrice(sellPrice);
        this.orderState.setOrderType(orderType);
        this.order(sellAmount, sellPrice, orderType, tradingApi, type);
    }


    @Override
    protected void buyCalculation() {

    }

    @Override
    protected void sellCalculation() {
    }

    private List<Kline> getKlines(TradingApi tradingApi, MarketConfig marketConfig, String buyKline, String size) {
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
