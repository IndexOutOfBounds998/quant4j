package com.quant.core.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.base.MoreObjects;
import com.quant.common.config.RedisUtil;
import com.quant.common.constans.RobotRedisKeyConfig;
import com.quant.core.config.AccountConfig;
import com.quant.core.config.KlineConfig;
import com.quant.core.config.MarketConfig;
import com.quant.core.config.StrategyConfig;
import com.quant.core.config.imp.HuoBiKlineConfigImpl;
import com.quant.core.enums.TraceType;
import com.quant.core.mq.OrderIdRedisMqServiceImpl;
import com.quant.core.mq.OrderProfitRedisMqServiceImpl;
import com.quant.core.mq.RedisMqService;
import com.quant.core.mq.RobotLogsRedisMqServiceImpl;
import com.quant.core.response.Kline;
import com.quant.core.response.OrdersDetail;
import com.quant.core.response.TradeBean;
import com.quant.core.strategy.AbstractStrategy;
import com.quant.core.strategy.StrategyException;
import com.quant.core.strategy.TradingStrategy;
import com.quant.core.trading.*;
import com.quant.core.vo.ProfitMessage;
import com.quant.core.vo.StrategyVo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * 火币策略
 *
 * @author yangyangchu
 * @Date 19.4.15
 */
@Slf4j
public class HuoBiStrategyImpl extends AbstractStrategy implements TradingStrategy {

    private RedisUtil redisUtil;
    private Integer robotId;

    //当前使用的配额币种
    private String quotaCurrency;

    //当前使用的基础币种
    private String baseCurrency;

    //价格精度
    private int pricePrecision;
    //数量金额精度
    private int amountPrecision;

    //当前账户余额
    private BigDecimal quotaBalance;

    //当前账户base余额
    private BigDecimal baseBalance;

    //精确到小数点的个数
    private static final int decimalPoint = 4;
    //市场买卖信息
    private MarketOrder marketOrder;
    //亏损次数
    private int profitTimes;

    private Weights weights;
    private TradingApi tradingApi;
    private MarketConfig marketConfig;
    //    private OrderState orderState;
    private AccountConfig accountConfig;
    //    private StrategyVo.BaseInfoEntity baseInfo;
    private StrategyVo.Setting1Entity setting1;
    private StrategyVo.Setting2Entity setting2;
    private StrategyVo.Setting3Entity setting3;
    private StrategyVo.Setting4Entity setting4;
    private StrategyVo.Setting5Entity setting5;
    private StrategyVo.Setting6Entity setting6;
    private long runTimes = 0;

    //标志机器人已经启动
    private volatile String startkey;
    //标志机器人是否在运行
    private volatile String isRunKey;

    private String quotaBalanceKey;

    private String baseBalanceKey;

    private String lastOrderState = "lastOrderState_";


    private String orderProfitIds = "order_Profit_Ids_";
    /**
     * 当前的最新购买价格
     */
    private BigDecimal currentNewBuyPrice;
    /**
     * 当前的最新出售价格
     */
    private BigDecimal currentNewSellPrice;
    /**
     * 是否到达了亏损次数
     */
    private volatile boolean profitArrive = false;

    //redismq 日志推送服务
    private RedisMqService redisMqService, orderMqService, orderProfitService;

    public HuoBiStrategyImpl(RedisUtil redisUtil, Integer robotId) {
        this.redisUtil = redisUtil;
        this.robotId = robotId;
        this.startkey = RobotRedisKeyConfig.getRobotIsStartStateKey() + robotId;
        this.isRunKey = RobotRedisKeyConfig.getRobotIsRunStateKey() + robotId;
    }

    @Override
    public void init(TradingApi tradingApi, MarketConfig market, StrategyConfig config, AccountConfig accountConfig) {
        log.info("===============初始化参数" + config.getStrategyVo().toString());
        this.tradingApi = tradingApi;
        this.marketConfig = market;
        this.accountConfig = accountConfig;
        this.baseInfo = config.getStrategyVo().getBaseInfo();
        this.setting1 = config.getStrategyVo().getSetting1();
        this.setting2 = config.getStrategyVo().getSetting2();
        this.setting3 = config.getStrategyVo().getSetting3();
        this.setting4 = config.getStrategyVo().getSetting4();
        this.setting5 = config.getStrategyVo().getSetting5();
        this.setting6 = config.getStrategyVo().getSetting6();

        this.orderState = new OrderState();
        this.weights = new Weights();
        this.redisMqService = new RobotLogsRedisMqServiceImpl(this.redisUtil, this.robotId, Integer.parseInt(this.accountConfig.getUserId()));
        this.orderMqService = new OrderIdRedisMqServiceImpl(this.redisUtil, accountConfig, robotId);
        this.orderProfitService = new OrderProfitRedisMqServiceImpl(this.redisUtil);
        //限价 市价方式 redis key
        this.lastOrderState = lastOrderState + "type_" + this.baseInfo.getIsLimitPrice() + "_";
        this.orderProfitIds = orderProfitIds + "type_" + this.baseInfo.getIsLimitPrice() + "_";
    }

    @Override
    public void execute() throws StrategyException {
        try {
            //设置机器人已经启动状态
            log.info("机器人{}启动了 配置加载>>>", robotId);
            redisUtil.set(startkey, true);
            //加载获取当前机器人的基础币种 配额币种 价格的精度 数量的精度
            boolean quotaPriceAndPrecision = getQuotaPriceAndPrecision();

            if (!quotaPriceAndPrecision) {
                redisMqService.sendMsg("获取机器人的基础币种 配额币种 价格的精度 数量的精度失败！机器人退出任务....");
                return;
            }
            //获取当前机器人的最后一次状态
            Object o = redisUtil.get(lastOrderState + robotId);
            if (o != null) {
                try {
                    log.info("机器人{}上一次运行的状态是{}", robotId, o.toString());
                    //恢复上一次的运行状态
                    this.orderState = JSON.parseObject(o.toString(), OrderState.class);
                    redisUtil.set(lastOrderState + robotId, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("恢复机器人{}状态失败{}", robotId, e.getMessage());
                    redisMqService.sendMsg("机器人上一次的运行状态恢复失败！机器人将以重新的状态启动......");
                }
            }
        } catch (Exception e) {
            log.error("机器人运行中发生异常：异常信息{}", e);
            redisMqService.sendMsg("机器人运行中发生异常：异常信息" + e.getMessage());
        }
        // 任务循环执行
        while (true) {
            try {
                //设置机器人的运行状态 在休眠+20s之后没响应 就认为该机器人已经死亡
                redisUtil.set(isRunKey, "running", (long) (baseInfo.getSleep() + 20));
                //重置权重
                weights.reSet();
                //获取市场订单 最大支持2000条
                this.marketOrder = this.tradingApi.getMarketOrders(this.marketConfig, "2000");
                if (this.marketOrder == null) {
                    log.info("获取市场订单数据失败。。。重试ing");
                    redisMqService.sendMsg("获取市场行情数据失败！重试.......");
                    Thread.sleep(100);
                    continue;
                }
                //设置当前买价和当前卖价 (价格设置的位数来自huobi获取的价格小数位)
                this.currentNewBuyPrice = this.marketOrder.getBuy().get(0).getPrice().setScale(pricePrecision, RoundingMode.DOWN);
                this.currentNewSellPrice = this.marketOrder.getSell().get(0).getPrice().setScale(pricePrecision, RoundingMode.DOWN);
                //计算设置买卖权重
                weightsCalculation();
                //判断买卖
                if (orderState.type == OrderType.BUY) {
                    if (profitArrive) {
                        redisMqService.sendMsg("到达盈亏次数,自动退出ing");
                        break;
                    }
                    //卖的情况下 先判断是否开启了止盈止损
                    if (takeProfitStopLoss()) {
                        //已经卖出 重新循环
                        redisMqService.sendMsg("止盈止损卖出！");
                        long v = (long) (this.baseInfo.getSleep() * 1000L);
                        Thread.sleep(v);
                        continue;
                    } else {
                        redisMqService.sendMsg("卖出止盈止损策略：策略未达到设置的盈亏点,执行计算卖出权重策略....");
                    }
                    //查看是否达到卖的信号
                    if (this.weights.getSellTotal() != 0 && this.weights.getSellTotal() >= this.baseInfo.getSellAllWeights()
                            && orderState.type == OrderType.BUY) {
                        try {
                            createSellOrder();
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("下单失败{},{}", this.orderState.toString(), e.getMessage());
                            redisMqService.sendMsg("当前下单信息【" + this.orderState.toString() + "】==下单失败 重新下单！");
                        }
                    } else {
                        redisMqService.sendMsg(this.weights.getSellTotal() == 0 ? "当前策略设置不进行卖出操作!!!" : "当前策略计算卖出权重:" + this.weights.getSellTotal() + ",未达到策略卖出总权重【" + baseInfo.getSellAllWeights() + "】不进行操作。。。");
                    }
                } else if (orderState.type == OrderType.SELL) {
                    if (profitArrive) {
                        redisMqService.sendMsg("到达盈亏次数,自动退出ing");
                        break;
                    }
                    //查看是否到达买的信号
                    if (this.weights.getBuyTotal() != 0 && this.weights.getBuyTotal() >= this.baseInfo.getBuyAllWeights()
                            && orderState.type == OrderType.SELL) {
                        try {
                            createBuyOrder();
                        } catch (Exception e) {
                            redisMqService.sendMsg("当前下单信息【" + this.orderState.toString() + "】==下单失败 重新下单！");
                            e.printStackTrace();
                            log.error("下单失败{},{}", this.orderState.toString(), e.getMessage());
                        }
                    } else {
                        redisMqService.sendMsg(this.weights.getBuyTotal() == 0 ? "当前策略设置不进行买入操作!!!" : "当前策略计算买入权重:" + this.weights.getBuyTotal() + ",未达到策略买入总权重【" + baseInfo.getBuyAllWeights() + "】不进行操作。。。");
                    }

                } else if (orderState.type == null) {
                    if (profitArrive) {
                        redisMqService.sendMsg("到达盈亏次数,自动退出ing");
                        break;
                    }
                    //查看当前订单状态 订单不存在的情况下 首先要出现买的信号 有了买的信号 进行购买
                    if (this.weights.getBuyTotal() != 0 && this.weights.getBuyTotal() >= this.baseInfo.getBuyAllWeights()) {
                        createBuyOrder();
                    } else {
                        redisMqService.sendMsg("当前策略计算购买权重:" + this.weights.getBuyTotal() + ",未达到策略购买总权重【" + baseInfo.getBuyAllWeights() + "】不进行操作。。。");
                    }
                }

                try {
                    ++runTimes;
                    redisMqService.sendMsg("机器人已经运行了>>>" + runTimes + "次");
                    log.info("=========================================");
                    //休眠几秒
                    Thread.sleep((long) (baseInfo.getSleep() * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!checkRobotIsStop(startkey)) {
                    redisMqService.sendMsg("机器人被取消任务!!!退出ing");
                    checkAndSet();
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

    /**
     * 针对策略6的计算方法
     * 止盈止损
     *
     * @return true 需要直接卖 不走其他流程
     */
    private boolean takeProfitStopLoss() {
        if (this.orderState.type == OrderType.BUY) {//当前订单是买入
            if (this.setting6.getIsAble() == 1) {//开启状态下
                BigDecimal diff;
                //只拿当前的卖出价格
                if (this.baseInfo.getIsLimitPrice() == 1) {//限价方式\
                    //买入的价格
                    BigDecimal buyPrice = this.orderState.price;
                    if (buyPrice.compareTo(BigDecimal.ZERO) == 0) {
                        return false;
                    }
                    //计算盈亏率
                    diff = currentNewSellPrice.subtract(buyPrice).divide(buyPrice, decimalPoint, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
                    redisMqService.sendMsg("止盈止损策略：买入订单价格为："
                            + buyPrice.setScale(pricePrecision, RoundingMode.DOWN)
                            + ",当前卖出价格为：" + currentNewSellPrice.setScale(pricePrecision, RoundingMode.DOWN)
                            + "，计算后的盈亏率为：" + diff + "%");
                } else {
                    //市价方式
                    //当前卖出价格计算
                    BigDecimal buyPrice = this.orderState.price;
                    //当前价格 通过获取订单详情来获取
                    try {
                        if (this.orderState.price.compareTo(BigDecimal.ZERO) == 0) {
                            //为0的情况下 算出购买时候的价格 （总的交易额/总的数量）=价格;
                            OrdersDetail ordersDetail = this.tradingApi.orderDetail(this.orderState.id);
                            if (new BigDecimal(ordersDetail.getFieldAmount()).compareTo(BigDecimal.ZERO) == 0) {
                                log.info("消费总数量为0");
                                return false;
                            }
                            buyPrice = new BigDecimal(ordersDetail.getFieldCashAmount()).divide(new BigDecimal(ordersDetail.getFieldAmount()), pricePrecision, RoundingMode.DOWN);
                            log.info("市价buyPrice{}", buyPrice);
                            this.orderState.price = buyPrice;
                        }
                        if (buyPrice.compareTo(BigDecimal.ZERO) == 0) {
                            log.info("购买价格为0");
                            return false;
                        }
                    } catch (Exception e) {
                        log.error("计算市价购买价格错误：{}", e);
                        //获取失败
                        return false;
                    }
                    //计算盈亏率(忽略相同数量的情况下 只对价格做盈亏率计算)
                    diff = currentNewSellPrice.subtract(buyPrice)
                            .divide(buyPrice, decimalPoint, RoundingMode.DOWN)
                            .multiply(new BigDecimal(100));
                    log.info("当前参与计算的buyPrice:{},sellPrice:{},diff{}", buyPrice, currentNewSellPrice, diff);
                    redisMqService.sendMsg("当前止盈止损策略：购买价格计算为:"
                            + buyPrice + ",当前市场最新卖出价格为" + currentNewSellPrice + ",价格盈亏计算得到的百分比为"
                            + diff.toPlainString() + "%");
                }
                if (diff.compareTo(BigDecimal.ZERO) > 0) {
                    //盈利
                    if (this.setting6.getTakeProfit().compareTo(BigDecimal.ZERO) != 0) {
                        log.info("策略设置的盈利点为{}", this.setting6.getTakeProfit());
                        int i = diff.compareTo(this.setting6.getTakeProfit());
                        log.info("策略比较后的i值{}", i);
                        if (i >= 0) {
                            log.info("设置的策略赢利点大于diff,开始卖出");
                            redisMqService.sendMsg("达到设置的止盈点,开始卖出");
                            log.info("达到设置的止盈点,开始卖出");
                            //止盈的百分比达到,设置的值,需要卖出
                            createSellOrder();
                            return true;
                        } else {
                            redisMqService.sendMsg("未达到设置的止盈点");
                        }
                    }
                } else {
                    //亏损
                    if (this.setting6.getStopLoss().compareTo(BigDecimal.ZERO) != 0) {

                        if (diff.abs().compareTo(this.setting6.getStopLoss()) >= 0) {
                            //止损的百分比达到 设置的值 需要卖出
                            redisMqService.sendMsg("达到设置的止损点,开始卖出");
                            createSellOrder();
                            return true;
                        } else {
                            redisMqService.sendMsg("未达到设置的止损点");
                        }
                    }
                }
            }
        }
        return false;
    }

    private void checkAndSet() {
        checkOrder(this.tradingApi);
        //记录当前机器人的最后一次状态
        redisUtil.set(lastOrderState + robotId, JSON.toJSONString(this.orderState));
    }

    /**
     * //获取用户的余额 价格的精度 数量的精度获取quote的交易对 quota 是当前的配额 例如usdt
     */
    private boolean getQuotaPriceAndPrecision() {
        String quota_price_amount = String.valueOf(redisUtil.get(RobotRedisKeyConfig.getSymbol() + marketConfig.markName()));
        String[] split = quota_price_amount.split("_");
        if (split.length < 3) {
            return false;
        }
        this.baseCurrency = split[0];
        this.quotaCurrency = split[1];
        this.pricePrecision = Integer.parseInt(split[2]);
        this.amountPrecision = Integer.parseInt(split[3]);
        quotaBalanceKey = quotaCurrency + "_balance_" + accountConfig.accountId();
        baseBalanceKey = baseCurrency + "_balance_" + accountConfig.accountId();
        log.info("当前机器人的基础币种{} 配额币种{} 价格的精度{} 数量的精度{}", baseCurrency, quotaCurrency, pricePrecision, amountPrecision);
        redisMqService.sendMsg("当前机器人的基础币种{" + baseCurrency + "} 配额币种{" + quotaCurrency + "} 价格的精度{" + pricePrecision + "} 数量的精度{" + amountPrecision + "}");
        return true;
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
        if (this.orderState.id == null) {
            log.info("当前账户{}没有任何订单,开始下单", accountConfig.accountId());
            return true;
        }
        try {
            List<OpenOrder> openOrders = tradingApi.getOpenOrders(this.marketConfig, this.accountConfig, "10");
            Optional<OpenOrder> first = openOrders.stream().filter(openOrder -> openOrder.getId().equals(String.valueOf(this.orderState.id))).findFirst();
            if (first.isPresent()) {
                redisMqService.sendMsg("当前订单状态:【" + this.orderState.type.getStringValue() + "】======");
                //比较上次的下单价格和这次的价格 如果相同的话 说明购买或者卖出订单可以被吃 等待被吃
                if (this.orderState.type == OrderType.BUY
                        && this.orderState.price.compareTo(this.currentNewBuyPrice) == 0) {
                    return false;
                }
                if (this.orderState.type == OrderType.SELL
                        && this.orderState.price.compareTo(this.currentNewSellPrice) == 0) {
                    return false;
                }
                //取消刚刚下的订单
                boolean cancel = tradingApi.cancelOrder(first.get().getId(), first.get().getMarketId());
                if (cancel) {
                    redisMqService.sendMsg("查询到未成功的订单开始取消订单,orderId【" + first.get().getId() + "】, 取消【" + this.orderState.type.getStringValue() + "】订单成功!!!");
                    if (this.orderState.type == OrderType.BUY) {
                        //如果当前订单是购买订单  取消了 应该继续购买
                        this.orderState.type = null;
                    }
                    if (this.orderState.type == OrderType.SELL) {
                        //如果是卖出 应该继续卖出
                        this.orderState.type = OrderType.BUY;
                    }
                    this.orderState.id = null;
                    return false;
                }
            } else {
                //将成功的订单信息传回admin
                orderMqService.sendMsg(this.orderState.id);
                String result = this.orderState.id + "_" + this.orderState.type.getStringValue();
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
            if (this.orderState.type == OrderType.SELL) {

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
                if (this.orderState.orderType == com.quant.core.enums.OrderType.SELL_MARKET) {
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
                log.info("当前的订单状态{},计算后的差价{}", this.orderState.type.getStringValue(), diff);
                divide = diff.divide(allBuyBalance, decimalPoint, RoundingMode.DOWN);
                log.info("盈亏率:{}", divide);
                if (diff.compareTo(BigDecimal.ZERO) < 0) {
                    profitTimes++;
                }
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

                if (profitTimes >= this.baseInfo.getProfit()) {
                    //如果亏损次数已经达到预设值 机器人退出线程
                    redisMqService.sendMsg("=======当前亏损次数【" + profitTimes + "】==已经达到预设值,机器人退出任务ing,请修改此策略重新来！！");
                    log.info("当前亏损次数达到了！结束任务。。。");
                    //记录当前机器人的最后一次状态
                    redisUtil.set(lastOrderState + robotId, JSON.toJSONString(this.orderState));
                    this.profitArrive = true;
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            log.error("计算盈亏率发生异常{}", e.getMessage());
        }

    }

    /**
     * 重试5次
     *
     * @param orderId
     * @return
     */
    private OrdersDetail getOrderDetail(Long orderId, int i) {
        try {
            return this.tradingApi.orderDetail(orderId);
        } catch (Exception e) {
            if (i == 5) {
                return null;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } finally {
                i++;
            }
            return getOrderDetail(orderId, i);
        }
    }

    /**
     * 检查是否已经取消了任务
     *
     * @param key
     * @return
     */
    private boolean checkRobotIsStop(String key) {
        Object o = this.redisUtil.get(key);
        return (boolean) o;
    }


    /**
     * 计算上涨或下跌 百分比 因为前台是 传 百分比的值 去掉了百分比 所以这里需要乘以100 作为百分比 和前台数据进行对比
     * 计算公式 (现在的价格-之前的价格)/ 之前的价格 * 100%
     * 保留4位小数
     *
     * @param nowPrice
     * @param beforePrice
     * @return
     */
    private BigDecimal calculationFallOrRise(BigDecimal nowPrice, BigDecimal beforePrice) {
        return nowPrice.subtract(beforePrice)
                .divide(beforePrice, decimalPoint, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));
    }


    /**
     * 创建订单
     *
     * @param amount
     * @param price
     * @param orderType
     */
    private void order(
            BigDecimal amount,
            BigDecimal price,
            com.quant.core.enums.OrderType orderType,
            TradingApi tradingApi, OrderType type) {
        //下订单
        try {
            redisMqService.sendMsg("叮叮叮>>>开始下单,下单信息 价格:" + price + "数量:" + amount + "订单类型:" + orderType.getTyoe());
            this.orderState.id = tradingApi.createOrder(this.marketConfig.markName(), this.accountConfig.accountId(), orderType, amount, price);
            if (this.orderState.id != null) {
                redisMqService.sendMsg("下单成功>>>订单信息【" + this.orderState.toString() + "】");
                Thread.sleep(1000);
                this.orderState.type = type;
                //将订单信息传送到后台 此时的订单可能未成功
                orderMqService.sendMsg(this.orderState.id);
            }
        } catch (ExchangeNetworkException | TradingApiException e) {
            e.printStackTrace();
            log.error("下订单发生异常{}", e.getMessage());
            redisMqService.sendMsg("下单异常>>>重新尝试下单......");
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("线程异常{}", e.getMessage());
            redisMqService.sendMsg("当前机器人异常>>>请通知管理员......");
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
        if (profitArrive) {
            log.info("机器人退出");
            return;
        }
        //当前无订单 创建购买订单
        BigDecimal buyAmount = BigDecimal.ZERO;
        BigDecimal buyPrice = BigDecimal.ZERO;
        com.quant.core.enums.OrderType orderType;
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
            //是限价的方式购买 需要计算价格
            BigDecimal currentBuyPrice = this.currentNewBuyPrice;
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
            orderType = com.quant.core.enums.OrderType.BUY_LIMIT;
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
            orderType = com.quant.core.enums.OrderType.BUY_MARKET;
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
        if (profitArrive) {
            return;
        }
        BigDecimal sellAmount = BigDecimal.ZERO;
        BigDecimal sellPrice = BigDecimal.ZERO;
        com.quant.core.enums.OrderType orderType;
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
            orderType = com.quant.core.enums.OrderType.SELL_LIMIT;
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
            orderType = com.quant.core.enums.OrderType.SELL_MARKET;
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
    private void orderPlace(TradingApi tradingApi, BigDecimal sellAmount, BigDecimal sellPrice, com.quant.core.enums.OrderType orderType, OrderType type) {

        this.orderState.amount = sellAmount;
        this.orderState.price = sellPrice;
        this.orderState.orderType = orderType;
        this.order(sellAmount, sellPrice, orderType, tradingApi, type);
    }

    /**
     * 获取余额
     *
     * @return
     */
    private boolean getBalance() {
        try {
            //获取用户的账户余额 knc_balance_6688515
            this.tradingApi.getBalanceInfo(this.accountConfig.accountId(), this.redisUtil);
            //基础货币和对价货币
            this.baseBalance = new BigDecimal(String.valueOf(redisUtil.get(baseBalanceKey)));
            this.quotaBalance = new BigDecimal(String.valueOf(redisUtil.get(quotaBalanceKey)));
            redisMqService.sendMsg("获取当前账户余额：baseBalance" + baseBalance + ",quotaBalance" + quotaBalance);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取账户{}的余额失败,失败信息{}", this.accountConfig.accountId(), e.getMessage());
        }
        return false;
    }

    @Override
    protected void buyCalculation() {
        redisMqService.sendMsg("机器人当前状态>>>>>>【待买入】");
        redisMqService.sendMsg("当前市场最新买入价格:" + currentNewBuyPrice);
        setting1Buy();
        setting2Buy();
        setting3Buy();
        setting4Buy();
        for (StrategyVo.Setting5Entity.BuyStrategyBean buyStrategyBean : this.setting5.getBuyStrategy()) {
            if (buyStrategyBean.getBuyWeights() != 0) {
                setting5Buy(buyStrategyBean, getBuyKline(buyStrategyBean));
            }
        }

    }

    @Override
    protected void sellCalculation() {
        redisMqService.sendMsg("机器人当前状态>>>>>>【待卖出】");
        redisMqService.sendMsg("当前市场最新卖出价格:" + currentNewSellPrice);
        setting1Sell();
        setting2Sell();
        setting3Sell();
        setting4Sell();
        for (StrategyVo.Setting5Entity.SellStrategyBean sellStrategyBean : this.setting5.getSellStrategy()) {
            if (sellStrategyBean.getSellWeights() != 0) {
                setting5Sell(sellStrategyBean, getSellKline(sellStrategyBean));
            }
        }

    }

    /**
     * 策略1购买权重计算
     */
    private void setting1Buy() {
        final StrategyVo.Setting1Entity config = this.setting1;
        if (config.getBuyWeights() != 0) {
            log.info("====开始计算策略1买入权重====");
            //计算买入权重
            if (setting1BuyCalculation(marketOrder, config)) {
                //设置权重
                weights.AddBuyTotal(config.getBuyWeights());
            }
        }
    }

    /**
     * 策略2卖出权重计算
     */
    private void setting1Sell() {
        final StrategyVo.Setting1Entity config = this.setting1;
        if (config.getSellWeights() != 0) {
            //计算卖
            log.info("====开始计算策略1卖出权重====");
            if (setting1SellCalculation(marketOrder, config)) {
                weights.AddSellTotal(config.getSellWeights());
            }
        }
    }

    /**
     * 策略2购买计算
     */
    private void setting2Buy() {
        final StrategyVo.Setting2Entity config = this.setting2;
        if (config.getBuyWeights() != 0) {
            //计算买
            if (setting2BuyCalculation(marketOrder, config)) {
                weights.AddBuyTotal(config.getBuyWeights());
            }
        }
    }

    /**
     * 策略2卖出计算
     */
    private void setting2Sell() {
        final StrategyVo.Setting2Entity config = this.setting2;
        if (config.getSellWeights() != 0) {
            //计算卖
            if (setting2SellCalculation(marketOrder, config)) {
                weights.AddSellTotal(config.getSellWeights());
            }
        }
    }

    /**
     * 计算最新购买订单价格与几秒前的价格 下跌超出百分比
     * ((V2-V1)/V1) × 100
     * 策略3购买计算
     */
    private void setting3Buy() {
        final StrategyVo.Setting3Entity config = this.setting3;
        if (config.getBuyWeights() != 0) {
            setting3BuyCalculation(config);
        }
    }

    /**
     * 计算权重3的卖出权重
     */
    private void setting3Sell() {
        final StrategyVo.Setting3Entity config = this.setting3;
        if (config.getSellWeights() != 0) {
            setting3SellCalculation(config);
        }
    }

    /**
     * 计算setting4的买入权重
     */
    private void setting4Buy() {
        final StrategyVo.Setting4Entity config = this.setting4;
        //计算几秒前的 时间
        if (config.getBuyWeights() != 0) {
            setting4BuyCalculation(config);
        }
    }

    /**
     * 计算setting4的卖出权重
     */
    private void setting4Sell() {
        final StrategyVo.Setting4Entity config = this.setting4;
        if (config.getSellWeights() != 0) {
            //计算卖出订单
            setting4SellCalculation(config);
        }
    }

    /**
     * 策略5的购买权重
     */
    private void setting5Buy(StrategyVo.Setting5Entity.BuyStrategyBean buyStrategyBean, List<Kline> lines) {
        if (lines != null && !lines.isEmpty()) {
            //买的权重
            setting5BuyCalculation(buyStrategyBean, lines);
        }
    }

    /**
     * 策略5的出售权重
     */
    private void setting5Sell(StrategyVo.Setting5Entity.SellStrategyBean sellStrategyBean, List<Kline> lines) {
        if (lines != null && !lines.isEmpty()) {
            //卖
            setting5SellCalculation(sellStrategyBean, lines);
        }
    }

    /**
     * 获取k线
     *
     * @return
     */
    private List<Kline> getBuyKline(StrategyVo.Setting5Entity.BuyStrategyBean cfg) {
        final TradingApi tradingApi = this.tradingApi;
        final MarketConfig marketConfig = this.marketConfig;
        //计算买的权重 获取k线
        String buyKline = cfg.getBuyKline();
        return getKlines(tradingApi, marketConfig, buyKline);
    }


    /**
     * 获取k线
     *
     * @return
     */
    private List<Kline> getSellKline(StrategyVo.Setting5Entity.SellStrategyBean cfg) {
        final TradingApi tradingApi = this.tradingApi;
        final MarketConfig marketConfig = this.marketConfig;
        //计算买的权重 获取k线
        String sellKline = cfg.getSellKline();
        return getKlines(tradingApi, marketConfig, sellKline);
    }

    private List<Kline> getKlines(TradingApi tradingApi, MarketConfig marketConfig, String buyKline) {
        KlineConfig klineConfig = new HuoBiKlineConfigImpl("50", buyKline);
        List<Kline> lines = null;
        try {
            lines = tradingApi.getKline(marketConfig, klineConfig);
        } catch (Exception e) {
            log.error("获取k线失败:{}", e.getMessage());
            redisMqService.sendMsg("获取k线数据失败,策略5失效!");
        }
        return lines;
    }

    private void setting3BuyCalculation(StrategyVo.Setting3Entity config) {
        //计算几秒前的 时间
        if (!marketOrder.getBuy().isEmpty()) {
            //计算购买订单
            TradeBean nowTrade = marketOrder.getBuy().get(0);
            final long buyNowTime = nowTrade.getTs();
            final long buyBeforeTime = buyNowTime - (config.getBuyDownSecond() * 1000);
            log.info("策略3：buyNowTime:{},buyBeforeTime:{}", buyNowTime, buyBeforeTime);
            Optional<TradeBean> beforeTrade = marketOrder.getBuy().stream()
                    .filter(tradeBean -> tradeBean.getTs() <= buyBeforeTime)
                    .findFirst();
            //找到相隔多少时间之前的数据
            if (beforeTrade.isPresent()) {
                //当前的价格
                BigDecimal nowPrice = nowTrade.getPrice().setScale(pricePrecision, RoundingMode.DOWN);
                //多少秒之前的价格
                BigDecimal beforePrice = beforeTrade.get().getPrice().setScale(pricePrecision, RoundingMode.DOWN);

                log.info("策略3：find time：{}", beforeTrade.get().getTs());
                //计算是否是跌了
                if (beforePrice.compareTo(nowPrice) > 0) {
                    //之前的价格大于现在的价格 价格在下跌 计算下跌百分比
                    BigDecimal down = calculationFallOrRise(nowPrice, beforePrice);
                    redisMqService.sendMsg("策略3:最新买入订单成交价格:" + nowPrice + " [对比] " + config.getBuyDownSecond() + "秒之前的订单价格:" + beforePrice + "下跌了" + down + "%");
                    //如果下跌超过
                    if (down.abs().compareTo(new BigDecimal(config.getBuyDownPercent())) > 0) {
                        redisMqService.sendMsg("策略3:计算后的下跌百分比：" + down.abs() + "%, [大于] 配置的百分比:" + config.getBuyDownPercent() + "%,策略3权重生效!");
                        weights.AddBuyTotal(config.getBuyWeights());
                    } else {
                        redisMqService.sendMsg("策略3:计算后的下跌百分比：" + down.abs() + "%, [小于] 配置的百分比:" + config.getBuyDownPercent() + "%,策略3权重不生效!");
                    }
                } else {
                    redisMqService.sendMsg("策略3最新购买订单成交记录价格" + nowPrice + " [对比] " + config.getBuyDownSecond() + "秒之前的订单价格" + beforePrice + ",没有下跌。");
                }
            }
        }

    }

    private void setting3SellCalculation(StrategyVo.Setting3Entity config) {
        if (!marketOrder.getSell().isEmpty()) {
            //计算卖出订单
            TradeBean tradeSellBeanNow = marketOrder.getSell().get(0);
            Optional<TradeBean> bfSellTradeBean = getBeforeTrade(tradeSellBeanNow, config);
            if (bfSellTradeBean.isPresent()) {
                //当前的价格
                BigDecimal nowPrice = tradeSellBeanNow.getPrice().setScale(pricePrecision, RoundingMode.DOWN);
                //多少秒之前的价格
                BigDecimal beforePrice = bfSellTradeBean.get().getPrice().setScale(pricePrecision, RoundingMode.DOWN);

                //计算是否是跌了
                if (bfSellTradeBean.get().getPrice().compareTo(tradeSellBeanNow.getPrice()) > 0) {

                    //之前的价格大于现在的价格 下跌 计算下跌百分比
                    BigDecimal down = calculationFallOrRise(nowPrice, beforePrice);
                    redisMqService.sendMsg("策略3：最新卖出订单成交记录价格" + nowPrice + " [对比] " + config.getSellDownSecond() + "秒之前的订单价格" + beforePrice + "下跌了" + down + "%");
                    //如果下跌超过
                    if (down.abs().compareTo(new BigDecimal(config.getSellDownPercent())) > 0) {
                        redisMqService.sendMsg("策略3:计算后的下跌百分比：" + down.abs() + "%, [大于] 配置的百分比:" + config.getSellDownSecond() + "%,策略3权重生效!");
                        weights.AddSellTotal(config.getSellWeights());
                    } else {
                        redisMqService.sendMsg("策略3:计算后的下跌百分比：" + down.abs() + "%, [小于] 配置的百分比:" + config.getSellDownSecond() + "%,策略3权重不生效!");
                    }
                } else {
                    redisMqService.sendMsg("策略3最新卖出订单成交记录价格" + nowPrice + "[对比]" + config.getSellDownSecond() + "秒之前的订单价格" + beforePrice + ",没有下跌");
                }
            }
        }
    }

    /**
     * 策略4买入权重计算
     *
     * @param config
     */
    private void setting4BuyCalculation(StrategyVo.Setting4Entity config) {
        if (!marketOrder.getBuy().isEmpty()) {
            //计算购买订单
            TradeBean tradeNow = marketOrder.getBuy().get(0);
            final long now = tradeNow.getTs();
            final long before = now - (config.getBuyUpSecond() * 1000);
            Optional<TradeBean> tradeBefore = marketOrder.getBuy().stream()
                    .filter(tradeBean -> tradeBean.getTs() <= before)
                    .findFirst();
            if (tradeBefore.isPresent()) {

                //当前的价格
                BigDecimal nowPrice = tradeNow.getPrice().setScale(pricePrecision, RoundingMode.DOWN);
                //多少秒之前的价格
                BigDecimal beforePrice = tradeBefore.get().getPrice().setScale(pricePrecision, RoundingMode.DOWN);

                //计算是否是涨了
                if (tradeBefore.get().getPrice().compareTo(tradeNow.getPrice()) < 0) {
                    //之前的价格小于现在的价格 上涨 计算上涨百分比
                    BigDecimal down = calculationFallOrRise(nowPrice, beforePrice);
                    redisMqService.sendMsg("策略4：最新买入订单成交记录价格:" + nowPrice
                            + "和" + config.getBuyUpSecond() + "秒之前的订单价格" + beforePrice + "上涨了" + down.abs() + "%");
                    //如果上涨超过
                    if (down.abs().compareTo(new BigDecimal(config.getBuyUpPercent())) > 0) {
                        weights.AddBuyTotal(config.getBuyWeights());
                        redisMqService.sendMsg("策略4：上涨百分比:" + down.abs() + "%, [大于] 配置的百分比:" + config.getBuyUpPercent() + "%,策略4权重生效!");
                    } else {
                        redisMqService.sendMsg("策略4：上涨百分比:" + down.abs() + "%, [小于] 配置的百分比:" + config.getBuyUpPercent() + "%,策略4权重不生效!");
                    }
                } else {
                    redisMqService.sendMsg("策略4：最新买入订单成交记录价格" + nowPrice
                            + " [对比] " + config.getBuyUpSecond() + "秒之前的订单价格" + beforePrice + ",没有上涨!");
                }
            }
        }
    }

    /**
     * 设置4的卖出计算
     *
     * @param config
     */
    private void setting4SellCalculation(StrategyVo.Setting4Entity config) {
        TradeBean tradeNow = marketOrder.getSell().get(0);
        final long sellNow = tradeNow.getTs();
        final long sellBefore = sellNow - (config.getSellUpSecond() * 1000);
        Optional<TradeBean> beforeTrade = marketOrder.getSell()
                .stream()
                .filter(tradeBean -> tradeBean.getTs() <= sellBefore)
                .findFirst();
        if (beforeTrade.isPresent()) {

            //当前的价格
            BigDecimal nowPrice = tradeNow.getPrice().setScale(pricePrecision, RoundingMode.DOWN);
            //多少秒之前的价格
            BigDecimal beforePrice = beforeTrade.get().getPrice().setScale(pricePrecision, RoundingMode.DOWN);

            //计算是否上涨了
            if (beforePrice.compareTo(nowPrice) < 0) {
                //之前的价格小于现在的价格 上涨 计算上涨百分比
                BigDecimal down = calculationFallOrRise(nowPrice, beforePrice);
                redisMqService.sendMsg("策略4：最新卖出订单价格：" + nowPrice
                        + "[对比]" + config.getSellUpSecond() + "秒之前的订单价格"
                        + beforePrice + "上涨了" + down.abs() + "%");
                //如果上涨超过
                if (down.abs().compareTo(new BigDecimal(config.getSellUpPercent())) > 0) {
                    weights.AddSellTotal(config.getSellWeights());
                    redisMqService.sendMsg("策略4：上涨百分比:" + down.abs() + "%,大于配置的百分比:" + config.getSellUpPercent() + "%,策略4权重生效!");
                } else {
                    redisMqService.sendMsg("策略4：上涨百分比:" + down.abs() + "%,小于配置的百分比:" + config.getSellUpPercent() + "%,策略4权重不生效!");
                }
            } else {
                redisMqService.sendMsg("策略4：最新卖出订单成交记录价格" + nowPrice
                        + " [对比] " + config.getSellUpSecond() + "秒之前的订单价格"
                        + beforePrice + ",没有上涨！");
            }
        }
    }

    /**
     * 策略5的买入权重计算
     *
     * @param config
     * @param lines
     */
    private void setting5BuyCalculation(StrategyVo.Setting5Entity.BuyStrategyBean config, List<Kline> lines) {
        //当前闭盘价
        Double nowClosePrice = lines.get(0).getClose();
        //上一次的闭盘价
        Double lastClosePrice = lines.get(1).getClose();

        if (config.getBuyKlineOption().equals(TraceType.up.getStr())) {

            //如果当前收盘价大于上一个线的收盘价 则有上升趋势（simple》？）
            if (nowClosePrice.compareTo(lastClosePrice) > 0) {
                //计算上涨的百分比 (当前最新成交价（或收盘价）-开盘参考价)÷开盘参考价×100%
                buyUpPercentageCompare(config, lines);
            } else {
                redisMqService.sendMsg("策略5：" + config.getBuyKline()
                        + "k线,当前闭盘价" + nowClosePrice
                        + "[对比]上一次的闭盘价" + lastClosePrice + ",暂无上涨趋势!");
            }
        }
        if (config.getBuyKlineOption().equals(TraceType.down.getStr())) {
            //如果当前收盘价小于上一个线的收盘价 则有下降趋势（simple》？）
            if (nowClosePrice.compareTo(lastClosePrice) < 0) {
                //计算下架的百分比 (当前最新成交价（或收盘价）-开盘参考价)÷开盘参考价×100%
                buyDownPercentageCompare(config, lines);
            } else {
                redisMqService.sendMsg("策略5：" + config.getBuyKline()
                        + "k线,当前闭盘价" + nowClosePrice
                        + "[对比]上一次的闭盘价" + lastClosePrice + ",暂无下降趋势!");
            }
        }
    }

    /**
     * 策略5计算卖出权重
     *
     * @param config
     * @param lines
     */
    private void setting5SellCalculation(StrategyVo.Setting5Entity.SellStrategyBean config, List<Kline> lines) {
        //当前闭盘价
        Double nowClosePrice = lines.get(0).getClose();
        //上一次的闭盘价
        Double lastClosePrice = lines.get(1).getClose();

        //卖的权重
        if (config.getSellKlineOption().equals(TraceType.up.getStr())) {
            //上涨趋势
            if (nowClosePrice.compareTo(lastClosePrice) > 0) {
                //计算上涨的涨幅
                sellUpPercentageCompare(config, lines);
            } else {
                redisMqService.sendMsg("策略5：" + config.getSellKline()
                        + "k线,当前闭盘价" + nowClosePrice
                        + " [对比] 上一次的闭盘价" + lastClosePrice + ",暂无上涨趋势!");
            }
        }
        if (config.getSellKlineOption().equals(TraceType.down.getStr())) {
            //如果当前收盘价小于上一个线的收盘价 则有下降趋势（simple》？）
            if (nowClosePrice.compareTo(lastClosePrice) < 0) {
                //计算跌幅
                sellDownPercentageCompare(config, lines);
            } else {
                redisMqService.sendMsg("策略5：" + config.getSellKline()
                        + "k线,当前闭盘价" + nowClosePrice
                        + " [对比] 上一次的闭盘价" + lastClosePrice + ",暂无下降趋势!");
            }
        }

    }

    /**
     * 获取策略3的sell before trade
     *
     * @param tradeSellBeanNow
     * @param config
     * @return
     */
    private Optional<TradeBean> getBeforeTrade(TradeBean tradeSellBeanNow, StrategyVo.Setting3Entity config) {
        try {
            final long sellNow = tradeSellBeanNow.getTs();
            final long sellBefore = sellNow - (config.getSellDownSecond() * 1000);
            return marketOrder.getSell()
                    .stream()
                    .filter(tradeBean -> tradeBean.getTs() <= sellBefore)
                    .findFirst();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获取策略3前几秒的数据失败");
            return Optional.empty();
        }
    }

    /**
     * 买：上涨or下架幅度
     * 计算购买时候的涨跌幅
     * 计算公式 (当前的close-上一个close)/上一个close * 100%
     * 计算的方式是将幅度转成绝对值进行比较
     *
     * @param config
     * @param lines
     */
    private void buyUpPercentageCompare(StrategyVo.Setting5Entity.BuyStrategyBean config, List<Kline> lines) {
        BigDecimal percentage = percentageCalculation(lines);
        if (percentage.compareTo(new BigDecimal(config.getBuyPercent())) > 0) {
            //涨跌幅大于设置值后
            redisMqService.sendMsg("策略5：" + config.getBuyKline()
                    + "k线,涨幅：" + percentage + "%, [大于] 设置值：" + config.getBuyPercent() + "%,策略5权重生效!");
            this.weights.AddBuyTotal(config.getBuyWeights());
        } else {
            redisMqService.sendMsg("策略5：" + config.getBuyKline()
                    + "k线,涨幅：" + percentage + "%, [小于] 设置值：" + config.getBuyPercent() + "%,策略5权重不生效!");
        }
    }


    /**
     * 买：上涨or下架幅度
     * 计算购买时候的涨跌幅
     * 计算公式 (当前的close-上一个close)/上一个close * 100%
     * 计算的方式是将幅度转成绝对值进行比较
     *
     * @param config
     * @param lines
     */
    private void buyDownPercentageCompare(StrategyVo.Setting5Entity.BuyStrategyBean config, List<Kline> lines) {
        BigDecimal percentage = percentageCalculation(lines);
        if (percentage.compareTo(new BigDecimal(config.getBuyPercent())) > 0) {
            //涨跌幅大于设置值后
            redisMqService.sendMsg("策略5：" + config.getBuyKline()
                    + "k线,跌幅：" + percentage + "%, [大于] 设置值：" + config.getBuyPercent() + "%,策略5权重生效!");
            this.weights.AddBuyTotal(config.getBuyWeights());
        } else {
            redisMqService.sendMsg("策略5：" + config.getBuyKline()
                    + "k线,跌幅：" + percentage + "%, [小于] 设置值：" + config.getBuyPercent() + "%,策略5权重不生效!");
        }
    }

    private void sellUpPercentageCompare(StrategyVo.Setting5Entity.SellStrategyBean config, List<Kline> lines) {
        BigDecimal percentage = percentageCalculation(lines);
        if (percentage.compareTo(new BigDecimal(config.getSellPercent())) > 0) {
            //涨跌幅大于设置值后
            redisMqService.sendMsg("策略5：" + config.getSellKline()
                    + "k线,涨幅：" + percentage + "%, [大于] 设置值：" + config.getSellPercent() + "%,策略5权重生效!");
            this.weights.AddSellTotal(config.getSellWeights());
        } else {
            redisMqService.sendMsg("策略5：" + config.getSellKline()
                    + "k线,涨幅：" + percentage + "%, [小于] 设置值：" + config.getSellPercent() + "%,策略5权重不生效!");
        }
    }

    private void sellDownPercentageCompare(StrategyVo.Setting5Entity.SellStrategyBean config, List<Kline> lines) {
        BigDecimal percentage = percentageCalculation(lines);
        if (percentage.compareTo(new BigDecimal(config.getSellPercent())) > 0) {
            //涨跌幅大于设置值后
            redisMqService.sendMsg("策略5：" + config.getSellKline()
                    + "k线,跌幅：" + percentage + "%, [大于] 设置值：" + config.getSellPercent() + "%,策略5权重生效!");
            this.weights.AddSellTotal(config.getSellWeights());
        } else {
            redisMqService.sendMsg("策略5：" + config.getSellKline()
                    + "k线,跌幅：" + percentage + "%, [小于] 设置值：" + config.getSellPercent() + "%,策略5权重不生效!");
        }
    }

    /**
     * 计算涨跌幅
     *
     * @param lines
     * @return
     */
    private BigDecimal percentageCalculation(List<Kline> lines) {
        BigDecimal quoteChange = (new BigDecimal(lines.get(0).getClose())
                .subtract(new BigDecimal(lines.get(1).getClose())))
                .divide(new BigDecimal(lines.get(1).getClose()), decimalPoint, RoundingMode.HALF_UP);
        return quoteChange.abs().multiply(new BigDecimal(100));
    }

    /**
     * 获取订单量超出某个usdt的数量 （买20位）
     * 对于设置一
     *
     * @param marketOrder
     * @return 返回当前买的价格
     */
    public Boolean setting1BuyCalculation(MarketOrder marketOrder, StrategyVo.Setting1Entity config) {
        if (marketOrder != null && !marketOrder.getBuy().isEmpty()) {
            Optional<TradeBean> res = marketOrder.getBuy()
                    .stream()
                    .limit(20)
                    .filter(m -> m.getPrice().compareTo(config.getBuyOrdersUsdt()) > 0)
                    .findFirst();
            if (res.isPresent()) {
                redisMqService.sendMsg("策略1：前20位购买订单中出现价格" + res.get().getPrice() + " [大于] 策略1 配置的价格" + config.getBuyOrdersUsdt());
                return true;
            }
            redisMqService.sendMsg("策略1：前20位购买订单中未出现大于策略1配置的价格" + config.getBuyOrdersUsdt() + "策略1不生效!");
            return false;
        }
        return false;
    }


    /**
     * 获取卖订单量超出某个(usdt)的数量 （卖20wei）
     *
     * @param marketOrder
     * @return 存在并返回当前卖的价格
     */
    public Boolean setting1SellCalculation(MarketOrder marketOrder, StrategyVo.Setting1Entity config) {
        if (marketOrder != null && !marketOrder.getSell().isEmpty()) {
            Optional<TradeBean> res = marketOrder.getSell()
                    .stream()
                    .limit(20)
                    .filter(m -> m.getPrice().compareTo(config.getSellOrdersUsdt()) > 0)
                    .findFirst();
            if (res.isPresent()) {
                redisMqService.sendMsg("策略1：前20位卖出订单中出现的价格" + res.get().getPrice() + "大于策略1 配置的价格" + config.getSellOrdersUsdt() + "策略1生效");
                return true;
            }
            redisMqService.sendMsg("策略1：前20位卖出订单中未出现大于策略1配置的价格" + config.getSellOrdersUsdt() + "策略1不生效!");
            return false;
        }
        return false;
    }

    /**
     * 获取当前买订单的usdt超出某个usdt的数量 （买）
     *
     * @param marketOrder
     * @return 返回当前买的价格
     */
    public Boolean setting2BuyCalculation(MarketOrder marketOrder, StrategyVo.Setting2Entity config) {
        if (marketOrder != null && !marketOrder.getBuy().isEmpty()) {
            this.currentNewBuyPrice = marketOrder.getBuy().get(0).getPrice();
            if (currentNewBuyPrice.compareTo(config.getBuyOrderUsdt()) > 0) {
                redisMqService.sendMsg("策略2：当前最新购买订单价格" + currentNewBuyPrice + "超过策略2配置的价格:" + config.getBuyOrderUsdt() + ",策略2权重生效！！！");
                return true;
            }
        }
        redisMqService.sendMsg("策略2：当前最新购买订单价格:" + currentNewBuyPrice + "未超过策略2的价格" + config.getBuyOrderUsdt() + ",策略2权重不生效！！！");
        return false;
    }


    /**
     * 获取当前订单卖的usdt超出某个usdt的数量 （卖）
     *
     * @param marketOrder
     * @return 返回当前买的价格
     */
    public Boolean setting2SellCalculation(MarketOrder marketOrder, StrategyVo.Setting2Entity config) {
        if (marketOrder != null && !marketOrder.getSell().isEmpty()) {
            this.currentNewSellPrice = marketOrder.getSell().get(0).getPrice();
            if (currentNewSellPrice.compareTo(config.getSellOrderUsdt()) > 0) {
                redisMqService.sendMsg("策略2：当前最新卖出订单价格" + currentNewSellPrice + "超过策略2的价格" + config.getSellOrderUsdt() + ",策略2权重生效");
                return true;
            }
        }
        redisMqService.sendMsg("策略2：当前最新卖出订单价格：" + currentNewSellPrice + "未超过配置2的价格" + config.getSellOrderUsdt() + ",策略2权重不生效");
        return false;
    }


    /**
     * 订单状态
     */
    @Data
    public static class OrderState {

        /**
         * Id - default to null.
         */
        private Long id = null;

        /**
         * Type: buy/sell. We default to null which means no order has been placed yet, i.e. we've just started!
         */
        private OrderType type = null;

        /**
         * 当前订单的类型
         */
        private com.quant.core.enums.OrderType orderType = null;

        /**
         * Price to buy/sell at - default to zero.
         */
        private BigDecimal price = BigDecimal.ZERO;


        /**
         * Number of units to buy/sell - default to zero.
         */
        private BigDecimal amount = BigDecimal.ZERO;

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("id", id)
                    .add("type", type)
                    .add("price", price)
                    .add("amount", amount)
                    .toString();
        }
    }


}
