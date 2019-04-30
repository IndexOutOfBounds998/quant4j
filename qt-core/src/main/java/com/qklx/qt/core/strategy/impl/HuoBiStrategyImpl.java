package com.qklx.qt.core.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.base.MoreObjects;
import com.qklx.qt.common.config.RedisUtil;
import com.qklx.qt.common.constans.RobotRedisKeyConfig;
import com.qklx.qt.core.config.AccountConfig;
import com.qklx.qt.core.config.KlineConfig;
import com.qklx.qt.core.config.MarketConfig;
import com.qklx.qt.core.config.StrategyConfig;
import com.qklx.qt.core.config.imp.HuoBiKlineConfigImpl;
import com.qklx.qt.core.enums.TraceType;
import com.qklx.qt.core.mq.OrderIdRedisMqServiceImpl;
import com.qklx.qt.core.mq.OrderProfitRedisMqServiceImpl;
import com.qklx.qt.core.mq.RedisMqService;
import com.qklx.qt.core.mq.RobotLogsRedisMqServiceImpl;
import com.qklx.qt.core.response.Kline;
import com.qklx.qt.core.response.OrdersDetail;
import com.qklx.qt.core.response.TradeBean;
import com.qklx.qt.core.strategy.AbstractStrategy;
import com.qklx.qt.core.strategy.StrategyException;
import com.qklx.qt.core.strategy.TradingStrategy;
import com.qklx.qt.core.trading.*;
import com.qklx.qt.core.vo.ProfitMessage;
import com.qklx.qt.core.vo.StrategyVo;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 火币策略
 *
 * @author yangyangchu
 * @Date 19.4.15
 */
public class HuoBiStrategyImpl extends AbstractStrategy implements TradingStrategy {

    private static Logger logger = LoggerFactory.getLogger(HuoBiStrategyImpl.class);

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

    //huobi交易手续费
    private static final BigDecimal fee = new BigDecimal(0.002);

    //亏损次数
    private int profitTimes;

    private Weights weights;
    private TradingApi tradingApi;
    private MarketConfig marketConfig;
    private OrderState orderState;
    private AccountConfig accountConfig;
    private StrategyVo.BaseInfoEntity baseInfo;
    private StrategyVo.Setting1Entity setting1;
    private StrategyVo.Setting2Entity setting2;
    private StrategyVo.Setting3Entity setting3;
    private StrategyVo.Setting4Entity setting4;
    private StrategyVo.Setting5Entity setting5;
    private StrategyVo.Setting6Entity setting6;
    private int runTimes = 0;

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
        logger.info("===============初始化参数" + config.getStrategyVo().toString());
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
        redisMqService = new RobotLogsRedisMqServiceImpl(this.redisUtil, this.robotId, Integer.parseInt(this.accountConfig.getUserId()));
        orderMqService = new OrderIdRedisMqServiceImpl(this.redisUtil, accountConfig, robotId);
        orderProfitService = new OrderProfitRedisMqServiceImpl(this.redisUtil);
    }

    @Override
    public void execute() throws StrategyException {
        try {
            //设置机器人已经启动状态
            logger.info("机器人启动了=========机器人启动了=========机器人启动了============================");
            redisUtil.set(startkey, true);
            //加载获取当前机器人的基础币种 配额币种 价格的精度 数量的精度
            boolean quotaPriceAndPrecision = getQuotaPriceAndPrecision();
            quotaBalanceKey = quotaCurrency + "_balance_" + accountConfig.accountId();
            baseBalanceKey = baseCurrency + "_balance_" + accountConfig.accountId();

            if (!quotaPriceAndPrecision) {
                logger.error("获取当前机器人的基础币种 配额币种 价格的精度 数量的精度失败！！！");
                redisMqService.sendMsg("获取当前机器人的基础币种 配额币种 价格的精度 数量的精度失败！！！");
                return;
            }
            //获取当前机器人的最后一次状态
            Object o = redisUtil.get(lastOrderState + robotId);
            if (o != null) {
                try {
                    logger.info("==========上一次机器人运行的状态是{}", o.toString());
                    //恢复上一次的运行状态
                    this.orderState = JSON.parseObject(o.toString(), OrderState.class);
                    redisUtil.set(lastOrderState + robotId, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("恢复机器人状态失败{}", e.getMessage());
                }
            }

            while (true) {
                try {
                    //设置机器人的运行状态 在休眠+15s之后没响应 就认为该机器人已经死亡
                    redisUtil.set(isRunKey, "isRunning", (long) (baseInfo.getSleep() + 15));
                    //重置权重
                    weights.reSet();
                    //获取市场订单
                    this.marketOrder = this.tradingApi.getMarketOrders(this.marketConfig, "2000");
                    if (this.marketOrder == null) {
                        logger.info("获取市场订单数据失败。。。重试ing");
                        redisMqService.sendMsg("获取市场订单数据失败。。。重试ing");
                        continue;
                    }
                    //设置当前买价和当前卖价
                    this.currentNewBuyPrice = this.marketOrder.getBuy().get(0).getPrice();
                    this.currentNewSellPrice = this.marketOrder.getSell().get(0).getPrice();
                    redisMqService.sendMsg("当前市场最新卖出订单价格为:【" + currentNewSellPrice + "】当前市场最新买入订单价格为:【" + currentNewBuyPrice + "】");
                    //计算设置买卖权重
                    executeor();
                    //判断买卖
                    if (orderState.type == OrderType.BUY) {
                        if (profitArrive) {
                            break;
                        }
                        //卖的情况下 先判断是否开启了止盈止损
                        boolean takeProfitStopLoss = takeProfitStopLoss();
                        if (takeProfitStopLoss) {
                            long v = (long) (this.baseInfo.getSleep() * 1000L);
                            Thread.sleep(v);
                            continue;
                        }
                        //查看是否达到卖的信号
                        if (this.weights.getSellTotal() >= this.baseInfo.getSellAllWeights()
                                && orderState.type == OrderType.BUY) {
                            try {
                                createSellOrder();
                            } catch (Exception e) {
                                e.printStackTrace();
                                redisMqService.sendMsg("当前下单信息【" + this.orderState.toString() + "】==下单失败 重新下单！");
                                createBuyOrder();
                            }
                        } else {
                            redisMqService.sendMsg("当前策略计算卖出权重:" + this.weights.getSellTotal() + ",未达到策略卖出总权重【" + baseInfo.getSellAllWeights() + "】不进行操作。。。");
                        }
                    } else if (orderState.type == OrderType.SELL) {
                        if (profitArrive) {
                            break;
                        }
                        //查看是否到达买的信号
                        if (this.weights.getBuyTotal() >= this.baseInfo.getBuyAllWeights()
                                && orderState.type == OrderType.SELL) {
                            try {
                                createBuyOrder();
                            } catch (Exception e) {
                                redisMqService.sendMsg("当前下单信息【" + this.orderState.toString() + "】==下单失败 重新下单！");
                                e.printStackTrace();
                                logger.error("下单失败{},{}", this.orderState.toString(), e.getMessage());
                                createBuyOrder();
                            }

                        } else {
                            redisMqService.sendMsg("当前策略计算买入权重:" + this.weights.getBuyTotal() + ",未达到策略买入总权重【" + baseInfo.getBuyAllWeights() + "】不进行操作。。。");
                        }

                    } else if (orderState.type == null) {
                        if (profitArrive) {
                            break;
                        }
                        //查看当前订单状态 订单不存在的情况下 首先要出现买的信号 有了买的信号 进行购买
                        if (this.weights.getBuyTotal() >= this.baseInfo.getBuyAllWeights()) {
                            createBuyOrder();
                        } else {
                            redisMqService.sendMsg("当前策略计算购买权重:" + this.weights.getBuyTotal() + ",未达到策略购买总权重【" + baseInfo.getBuyAllWeights() + "】不进行操作。。。");
                        }
                    }

                    try {
                        ++runTimes;
                        logger.info("机器人{}已经运行了{}次", robotId, runTimes);
                        logger.info("=========================================");
                        //休眠几秒
                        Thread.sleep((long) (baseInfo.getSleep() * 1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (checkIsStop(startkey)) {
                        redisMqService.sendMsg("机器人" + robotId + "已经被取消了任务 退出ing");
                        logger.info("机器人{}已经被取消了任务", robotId);
                        checkAndSet();
                        break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                    logger.error("机器人运行中发生异常：异常信息{}", e.getMessage());
                    redisMqService.sendMsg("机器人运行中发生异常：异常信息" + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            redisMqService.sendMsg("机器人运行中发生异常：异常信息" + e.getMessage());
        }
    }

    /**
     * 针对策略6的计算方法
     * 止盈止损
     *
     * @return true 需要直接卖 不走其他流程
     */
    private boolean takeProfitStopLoss() {

        if (this.setting6.getIsAble() == 1) {//开启状态下

            if (this.orderState.type == OrderType.BUY) {//当前订单是买入 计算卖出的盈利率
                BigDecimal diff;
                if (this.baseInfo.getIsLimitPrice() == 1) {//限价方式\

                    //只拿当前的卖出价格
                    BigDecimal sellPrice = this.marketOrder.getSell().get(0).getPrice().setScale(pricePrecision, RoundingMode.DOWN);
                    //买入的价格
                    BigDecimal buyPrice = this.orderState.price;
                    //计算盈亏率
                    diff = sellPrice.subtract(buyPrice).divide(buyPrice, pricePrecision, RoundingMode.DOWN);

                } else {
                    //市价方式
                    //当前卖出价格计算 深度最多的 作为卖出价格
                    BigDecimal sellPrice, buyPrice;
                    try {
                        sellPrice = this.marketOrder.getSell().stream()
                                .limit(20)
                                .max(Comparator.comparing(TradeBean::getAmount))
                                .get().getPrice();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                    //当前价格 通过获取订单详情来获取
                    try {
                        OrdersDetail ordersDetail = this.tradingApi.orderDetail(this.orderState.id);
                        buyPrice = new BigDecimal(ordersDetail.getPrice()).setScale(pricePrecision, RoundingMode.DOWN);
                    } catch (Exception e) {
                        e.printStackTrace();
                        //获取失败
                        return false;
                    }
                    //计算盈亏率(忽略相同数量的情况下 只对价格做盈亏率计算)
                    diff = sellPrice.subtract(buyPrice).divide(buyPrice, pricePrecision, RoundingMode.DOWN);
                }
                if (diff.compareTo(BigDecimal.ZERO) > 0) {
                    //盈利
                    if (this.setting6.getTakeProfit().compareTo(BigDecimal.ZERO) != 0) {

                        if (this.setting6.getTakeProfit().compareTo(diff) > 0) {
                            //止盈的百分比达到 设置的值 需要卖出
                            createSellOrder();
                            return true;
                        }
                    }
                } else {
                    //亏损
                    if (this.setting6.getStopLoss().compareTo(BigDecimal.ZERO) != 0) {

                        if (this.setting6.getStopLoss().compareTo(diff.abs()) > 0) {
                            //止损的百分比达到 设置的值 需要卖出
                            createSellOrder();
                            return true;
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
        logger.info("当前机器人的基础币种{} 配额币种{} 价格的精度{} 数量的精度{}", baseCurrency, quotaCurrency, pricePrecision, amountPrecision);
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
        logger.info("当前订单状态{}", this.orderState.toString());
        if (this.orderState.id == null) {
            logger.info("当前账户{}没有任何订单,开始下单", accountConfig.accountId());
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
            logger.error("账户{}取消订单失败{}", this.accountConfig.accountId(), e.getMessage());
            e.printStackTrace();
            checkOrder(tradingApi);
        }
        return false;
    }

    /**
     * 计算盈利
     */
    private void CalculateProfit() {
        try {
            Object o = this.redisUtil.lPop(orderProfitIds + robotId);

            if (o == null) {
                logger.info("当前redis 订单id 队列 暂无数据============== ");
                return;
            }
            logger.info("当前订单id 队列取出来的值是{}", o.toString());
            String current = o.toString();
            String[] currentIdAndType = current.split("_");

            if (currentIdAndType.length != 2) {
                logger.error("redis 订单id队列存储异常数据{}", current);
                return;
            }
            if (currentIdAndType[1].equals(OrderType.BUY.getStringValue())) {
                //如果当前的是购买订单 不计算盈利 重新将值赋值到redis
                this.redisUtil.lPush(orderProfitIds + robotId, o);
                return;
            }
            if (this.orderState.type == OrderType.SELL) {
                long buyOrderId, sellOrderId;
                BigDecimal allBuyBalance, allSellBalance, buyAmount, sellAmount, buyPrice, sellPrice, diff, divide;
                //获取上一次的购买金额和数量
                Object last = this.redisUtil.lPop(orderProfitIds + robotId);

                if (last == null) {
                    logger.error("获取订单id队列上一次的购买记录错误");
                    return;
                }
                logger.info("上一次订单id 队列取出来的值是{}", last.toString());

                String[] lastIdAndType = last.toString().split("_");

                //如果当前的订单是市价单 计算盈亏 需要查询这个订单的详情信息
                buyOrderId = Long.parseLong(lastIdAndType[0]);
                sellOrderId = Long.parseLong(currentIdAndType[0]);

                //订单详情
                OrdersDetail ordersBuyDetail, ordersSellDetail;

                ordersBuyDetail = getOrderDetail(buyOrderId);
                if (ordersBuyDetail == null) {
                    logger.info("获取购买订单为null");
                    return;
                }
                if (new BigDecimal(ordersBuyDetail.getFieldCashAmount()).compareTo(BigDecimal.ZERO) == 0) {
                    return;
                }
                ordersSellDetail = getOrderDetail(sellOrderId);
                if (ordersSellDetail == null) {
                    logger.info("获取出售订单为null");
                    return;
                }
                if (new BigDecimal(ordersSellDetail.getFieldCashAmount()).compareTo(BigDecimal.ZERO) == 0) {
                    return;
                }
                //如果是市价的情况
                if (this.orderState.orderType == com.qklx.qt.core.enums.OrderType.SELL_MARKET) {
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
                logger.info("当前的订单状态{},计算后的差价{}", this.orderState.type.getStringValue(), diff);
                divide = diff.divide(allBuyBalance, pricePrecision, RoundingMode.DOWN);
                logger.info("盈亏率:{}", divide);
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
                    logger.info("当前亏损次数达到了！结束任务。。。");
                    //记录当前机器人的最后一次状态
                    redisUtil.set(lastOrderState + robotId, JSON.toJSONString(this.orderState));
                    this.profitArrive = true;
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            logger.error("计算盈亏率发生异常{}", e.getMessage());
        }

    }

    private OrdersDetail getOrderDetail(Long orderId) {
        try {
            return this.tradingApi.orderDetail(orderId);
        } catch (Exception e) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            return getOrderDetail(orderId);
        }
    }

    /**
     * 检查是否已经取消了任务
     *
     * @param startkey
     * @return
     */
    private boolean checkIsStop(String startkey) {
        Object o = this.redisUtil.get(startkey);
        if (o == null) {
            return false;
        } else if (!((boolean) o)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 计算上涨或下跌 百分比 因为前台是 传 百分比的值 去掉了百分比 所以这里需要乘以100 作为百分比 和前台数据进行对比
     *
     * @param tradeBeanNow
     * @param bfTradeBean
     * @return
     */
    private BigDecimal calculationFallOrRise(TradeBean tradeBeanNow, TradeBean bfTradeBean) {
        BigDecimal diff = tradeBeanNow.getPrice().subtract(bfTradeBean.getPrice()).setScale(pricePrecision, RoundingMode.HALF_UP);
        return diff.divide(bfTradeBean.getPrice(), pricePrecision, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
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
            com.qklx.qt.core.enums.OrderType orderType,
            TradingApi tradingApi) {
        //下订单
        try {
            redisMqService.sendMsg("叮叮叮>>>开始下单,下单信息 价格:" + price + "数量:" + amount + "订单类型:" + orderType.getTyoe());
            this.orderState.id = tradingApi.createOrder(this.marketConfig.markName(), this.accountConfig.accountId(), orderType, amount, price);
            if (this.orderState.id != null) {
                redisMqService.sendMsg("下单成功>>>订单信息【" + this.orderState.toString() + "】");
                Thread.sleep(1000);
            }
        } catch (ExchangeNetworkException | TradingApiException e) {
            e.printStackTrace();
            logger.error("下订单发生异常{}", e.getMessage());
            redisMqService.sendMsg("下单异常>>>重新尝试下单......");
            this.order(amount, price, orderType, tradingApi);
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("线程异常{}", e.getMessage());
            redisMqService.sendMsg("当前机器人异常>>>请通知管理员......");
        }
    }

    /**
     * 创建购买订单
     */
    private void createBuyOrder() {
        if (!checkOrder(tradingApi)) {
            return;
        }
        if (profitArrive) {
            return;
        }
        //当前无订单 创建购买订单
        BigDecimal buyAmount = BigDecimal.ZERO;
        BigDecimal buyPrice = BigDecimal.ZERO;
        com.qklx.qt.core.enums.OrderType orderType;
        //获取余额
        if (!getBalance()) {
            logger.error("未获取到余额信息！！！");
            redisMqService.sendMsg("未获取账户【" + this.accountConfig.accountId() + "】的余额信息！！！");
            return;
        }
        if (this.quotaBalance.compareTo(BigDecimal.ZERO) < 0) {
            logger.info("账户{}没有余额 请及时充值", this.accountConfig.accountId());
            redisMqService.sendMsg("账户【" + this.accountConfig.accountId() + "】没有余额,请及时充值=======");
            return;
        }
        //是否是限价
        if (baseInfo.getIsLimitPrice() == 1) {
            //是限价的方式购买 需要计算价格
            BigDecimal currentBuyPrice = this.currentNewBuyPrice;
            //计算购买的价格
            buyPrice = baseInfo.getBuyPrice().add(currentBuyPrice).setScale(pricePrecision, RoundingMode.DOWN);
            logger.info("限价购买计算价格:当前市场最新的买入的价格:{},计算后的订单价格:{}", currentBuyPrice, buyPrice);
            redisMqService.sendMsg("=========当前策略交易方式:限价交易========");
            redisMqService.sendMsg("限价购买价格计算:当前市场最新买入价格:【" + currentBuyPrice + "】,策略计算后的订单价格:【" + buyPrice + "】");
            //计算购买的数量 是否全部买
            BigDecimal aviAmount = quotaBalance.divide(buyPrice, amountPrecision, RoundingMode.DOWN);
            if (baseInfo.getIsAllBuy() == 1) {
                //计算购买的数量 当前可用余额除以购买价格
                buyAmount = aviAmount;
                logger.info("限价购买计算数量:全部购买,购买数量为{}", buyAmount);
                redisMqService.sendMsg("=========当前策略购买方式:全部购买========");
                redisMqService.sendMsg("限价全部购买计算数量后为【" + buyAmount + "】");
            } else {
                //不是全部购买 自定义购买数量
                buyAmount = buyAmount.add(baseInfo.getBuyAmount().setScale(amountPrecision, RoundingMode.DOWN));
                if (buyAmount.compareTo(aviAmount) > 0) {
                    logger.info("限价自定义购买:账户{}的余额不足,需要充值......", accountConfig.accountId());
                    redisMqService.sendMsg("限价自定义购买:账户【" + accountConfig.accountId() + "】的余额不足,需要充值......");
                    return;
                }
                logger.info("限价自定义购买数量:账户{},购买数量{}", accountConfig.accountId(), buyAmount);
                redisMqService.sendMsg("限价自定义购买数量为【" + buyAmount + "】");
            }
            //设置当前订单的type 为限价买入
            orderType = com.qklx.qt.core.enums.OrderType.BUY_LIMIT;
        } else {
            redisMqService.sendMsg("=========当前策略交易方式:市价交易========");
            //市价买 价格直接填0 交易额的精度固定为8
            //计算购买的数量 是否全部买
            if (baseInfo.getIsAllBuy() == 1) {
                redisMqService.sendMsg("=========当前策略购买方式:全部购买========");
                //如果市价全部买 就是价格就是交易额度 quotaBalance;
                buyAmount = this.quotaBalance.setScale(8, RoundingMode.DOWN);
                if (buyAmount.compareTo(BigDecimal.ONE) < 0) {
                    redisMqService.sendMsg("市价交易,交易额不能低于1个");
                    logger.info("市价全部购买交易额不能低于1");
                    return;
                }
                logger.info("市价全部购买:交易额:{}", buyAmount);
            } else {
                //不是全部购买 自定义交易额 购买
                buyAmount = baseInfo.getBuyQuotaPrice().setScale(8, RoundingMode.DOWN);
                if (buyAmount.compareTo(BigDecimal.ONE) < 0) {
                    redisMqService.sendMsg("市价自定义购买数量:账户【" + accountConfig.accountId() + "】余额不足,需要充值......");
                    logger.info("市价自定义购买交易额:账户{}的余额不足,需要充值......", accountConfig.accountId());
                    return;
                }
                logger.info("市价自定义购买交易额:账户id{},购买数量{}", accountConfig.accountId(), buyAmount);
                redisMqService.sendMsg("市价自定义购买数量为【" + buyAmount + "】");
            }
            orderType = com.qklx.qt.core.enums.OrderType.BUY_MARKET;
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
        com.qklx.qt.core.enums.OrderType orderType;
        if (!getBalance()) {
            return;
        }
        if (this.baseBalance.compareTo(BigDecimal.ZERO) < 0) {
            logger.info("账户{},{}没有余额 请及时充值", this.accountConfig.accountId(), this.baseCurrency);
            redisMqService.sendMsg("账户id【" + this.accountConfig.accountId() + "】,【" + this.baseCurrency + "】没有余额 请及时充值");
            return;
        }
        //是否是限价
        if (baseInfo.getIsLimitPrice() == 1) {
            //从当前的20个卖出订单里找出最优的价格 （限价卖出）
            BigDecimal currentSellPrice = marketOrder.getSell().get(0).getPrice();
            //计算卖出的价格
            sellPrice = baseInfo.getSellPrice().add(currentSellPrice).setScale(pricePrecision, RoundingMode.UP);
            logger.info("限价卖出:当前卖出的价格:{},计算后的订单卖出价格:{}", currentSellPrice, sellPrice);
            redisMqService.sendMsg("限价卖出:当前卖出的价格:【" + currentSellPrice + "】,计算后的订单卖出价格:【" + sellPrice + "】");
            //计算购买的数量 是否全部卖出
            if (baseInfo.getIsAllSell() == 1) {
                //从用户api的表里查询到他的账户相应的base 火币的数量全部购买
                sellAmount = sellAmount.add(this.baseBalance).setScale(this.amountPrecision, RoundingMode.DOWN);
                logger.info("限价全部卖出:账户id{}卖出数量为{}", this.accountConfig.accountId(), sellAmount);
                redisMqService.sendMsg("限价全部卖出:账户id【" + this.accountConfig.accountId() + "】卖出数量为【" + sellAmount + "】");
            } else {
                sellAmount = sellAmount.add(this.baseInfo.getSellAmount().setScale(this.amountPrecision, RoundingMode.DOWN));
                if (sellAmount.compareTo(this.baseBalance) > 0) {
                    logger.info("限价自定义卖出余额{}大于账户余额{}:账户id{}的余额不足,需要充值......", sellAmount, this.baseBalance, accountConfig.accountId());
                    redisMqService.sendMsg("限价自定义卖出余额【" + sellAmount + "】大于账户余额【" + this.baseBalance + "】>>>账户id【" + this.accountConfig.accountId() + "】的余额不足,需要充值......");
                    return;
                }
                logger.info("限价自定义卖出数量:账户id{},卖出数量{}", this.accountConfig.accountId(), sellAmount);
                redisMqService.sendMsg("限价自定义卖出数量:账户id【" + this.accountConfig.accountId() + "】,卖出数量【" + sellAmount + "】");
            }
            orderType = com.qklx.qt.core.enums.OrderType.SELL_LIMIT;
        } else {
            //市价卖出 价格直接填0 计算交易额度
            if (baseInfo.getIsAllSell() == 1) {
                //如果市价全部卖出 就是价格就是交易额度 quotaBalance;
                sellAmount = this.baseBalance.setScale(this.amountPrecision, RoundingMode.DOWN);
                logger.info("市价全部卖出,卖出数量{}", sellAmount);
                redisMqService.sendMsg("市价全部卖出,卖出数量【" + sellAmount + "】");
            } else {
                //不是全部购买 自定义交易额 购买
                sellAmount = sellAmount.add(baseInfo.getSellAmount()).setScale(this.amountPrecision, RoundingMode.DOWN);
                if (sellAmount.compareTo(this.baseBalance) > 0) {
                    logger.info("市价自定义卖出:账户id{}的余额不足,需要充值......", this.accountConfig.accountId());
                    redisMqService.sendMsg("市价自定义卖出:账户id【" + this.accountConfig.accountId() + "】的余额不足,需要充值......");
                    return;
                }
                logger.info("市价自定义卖出数量:账户id{},卖出币种{},数量{}", this.accountConfig.accountId(), this.baseCurrency, sellAmount);
                redisMqService.sendMsg("市价自定义卖出数量:账户id【" + this.accountConfig.accountId() + "】,卖出币种【" + this.baseCurrency + "】,数量【" + sellAmount + "】");
            }
            orderType = com.qklx.qt.core.enums.OrderType.SELL_MARKET;
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
    private void orderPlace(TradingApi tradingApi, BigDecimal sellAmount, BigDecimal sellPrice, com.qklx.qt.core.enums.OrderType orderType, OrderType type) {
        this.orderState.type = type;
        this.orderState.amount = sellAmount;
        this.orderState.price = sellPrice;
        this.orderState.orderType = orderType;
        this.order(sellAmount, sellPrice, orderType, tradingApi);
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
            BigDecimal quotaBalance = new BigDecimal(String.valueOf(redisUtil.get(quotaBalanceKey)));
            BigDecimal baseBalance = new BigDecimal(String.valueOf(redisUtil.get(baseBalanceKey)));
            this.baseBalance = baseBalance.setScale(pricePrecision, RoundingMode.DOWN);
            this.quotaBalance = quotaBalance.setScale(pricePrecision, RoundingMode.DOWN);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取账户{}的余额失败,失败信息{}", this.accountConfig.accountId(), e.getMessage());
        }
        return false;
    }

    /**
     * 计算第一个设置
     *
     * @param marketOrder
     * @return
     */
    @Override
    public void executeSetting1(MarketOrder marketOrder) {
        final StrategyVo.Setting1Entity setting1Entity = this.setting1;
        BigDecimal firstBuyOrderPrice = setting1BuyCalculation(marketOrder, setting1Entity);
        if (firstBuyOrderPrice != null) {
            //设置权重
            weights.AddBuyTotal(setting1Entity.getBuyWeights());
        }
        //计算卖
        BigDecimal firstSellOrderPrice = setting1SellCalculation(marketOrder, setting1Entity);
        if (firstSellOrderPrice != null) {
            weights.AddSellTotal(setting1Entity.getSellWeights());
        }
    }

    /**
     * 计算第二个设置
     *
     * @param marketOrder
     * @return
     */
    @Override
    public void executeSetting2(MarketOrder marketOrder) {
        final StrategyVo.Setting2Entity setting2Entity = this.setting2;
        //计算买
        BigDecimal currentBuyPrice = setting2BuyCalculation(marketOrder, setting2);
        if (currentBuyPrice != null) {
            weights.AddBuyTotal(setting2Entity.getBuyWeights());
        }
        //计算卖
        BigDecimal currentSellPrice = setting2SellCalculation(marketOrder, setting2);
        if (currentSellPrice != null) {
            weights.AddSellTotal(setting2Entity.getSellWeights());
        }
    }

    /**
     * 计算最新购买订单价格与几秒前的价格 下跌超出百分比
     * ((V2-V1)/V1) × 100
     *
     * @param marketOrder
     * @return
     */
    @Override
    public void executeSetting3(MarketOrder marketOrder) {
        final StrategyVo.Setting3Entity setting3Entity = this.setting3;
        //计算几秒前的 时间
        if (!marketOrder.getBuy().isEmpty()) {
            //计算购买订单
            TradeBean tradeBeanNow = marketOrder.getBuy().get(0);
            final long now = tradeBeanNow.getTs();
            final long before = now - (setting3Entity.getBuyDownSecond() * 1000);
            BigDecimal down;
            Optional<TradeBean> bfTradeBean = marketOrder.getBuy().stream().filter(tradeBean -> tradeBean.getTs() <= before)
                    .findFirst();
            if (bfTradeBean.isPresent()) {
                //计算是否是跌了
                if (bfTradeBean.get().getPrice().compareTo(tradeBeanNow.getPrice()) > 0) {
                    //之前的价格大于现在的价格 下跌 计算下跌百分比
                    down = calculationFallOrRise(tradeBeanNow, bfTradeBean.get());
                    //如果下跌超过
                    if (down.abs().compareTo(new BigDecimal(setting3Entity.getBuyDownPercent())) > 0) {
                        weights.AddBuyTotal(setting3Entity.getBuyWeights());
                    }
                }
            }
        }
        if (!marketOrder.getSell().isEmpty()) {
            //计算卖出订单
            TradeBean tradeBeanNow = marketOrder.getSell().get(0);
            final long sellNow = tradeBeanNow.getTs();
            final long sellBefore = sellNow - (setting3Entity.getSellDownSecond() * 1000);
            Optional<TradeBean> bfSellTradeBean = marketOrder.getSell().stream().filter(tradeBean -> tradeBean.getTs() <= sellBefore)
                    .findFirst();
            if (bfSellTradeBean.isPresent()) {
                //计算是否是跌了
                if (bfSellTradeBean.get().getPrice().compareTo(tradeBeanNow.getPrice()) > 0) {
                    //之前的价格大于现在的价格 下跌 计算下跌百分比
                    BigDecimal down = calculationFallOrRise(tradeBeanNow, bfSellTradeBean.get());
                    //如果下跌超过
                    if (down.abs().compareTo(new BigDecimal(setting3Entity.getSellDownPercent())) > 0) {
                        weights.AddSellTotal(setting3Entity.getSellWeights());
                    }
                }

            }
        }

    }

    /**
     * 计算设置4的权重
     *
     * @param marketOrder
     * @return
     */
    @Override
    public void executeSetting4(MarketOrder marketOrder) {
        final StrategyVo.Setting4Entity setting4Entity = this.setting4;
        //计算几秒前的 时间
        TradeBean tradeBeanNow;
        BigDecimal down;
        if (!marketOrder.getBuy().isEmpty()) {
            //计算购买订单
            tradeBeanNow = marketOrder.getBuy().get(0);
            final long now = tradeBeanNow.getTs();
            final long before = now - (setting4Entity.getBuyUpSecond() * 1000);

            Optional<TradeBean> bfTradeBean = marketOrder.getBuy().stream()
                    .filter(tradeBean -> tradeBean.getTs() <= before)
                    .findFirst();
            if (bfTradeBean.isPresent()) {
                //计算是否是涨了
                if (bfTradeBean.get().getPrice().compareTo(tradeBeanNow.getPrice()) < 0) {
                    //之前的价格小于现在的价格 上涨 计算上涨百分比
                    down = calculationFallOrRise(tradeBeanNow, bfTradeBean.get());
                    //如果下跌超过
                    if (down.abs().compareTo(new BigDecimal(setting4Entity.getBuyUpPercent())) > 0) {
                        weights.AddBuyTotal(setting4Entity.getBuyWeights());
                    }
                }
            }
        }
        //计算卖出订单
        tradeBeanNow = marketOrder.getSell().get(0);
        final long sellNow = tradeBeanNow.getTs();
        final long sellBefore = sellNow - (setting4Entity.getSellUpSecond() * 1000);
        Optional<TradeBean> bfSellTradeBean = marketOrder.getSell().stream().filter(tradeBean -> tradeBean.getTs() <= sellBefore)
                .findFirst();
        if (bfSellTradeBean.isPresent()) {
            //计算是否是跌了
            if (bfSellTradeBean.get().getPrice().compareTo(tradeBeanNow.getPrice()) > 0) {
                //之前的价格大于现在的价格 下跌 计算下跌百分比
                down = calculationFallOrRise(tradeBeanNow, bfSellTradeBean.get());
                //如果下跌超过
                if (down.abs().compareTo(new BigDecimal(setting4Entity.getSellUpPercent())) > 0) {
                    weights.AddSellTotal(setting4Entity.getSellWeights());
                }
            }

        }

    }

    /**
     * simple  to do better last time
     * k线的上涨或者下跌
     */
    @Override
    protected void executeSetting5() {
        final StrategyVo.Setting5Entity setting5Entity = this.setting5;
        final TradingApi tradingApi = this.tradingApi;
        final MarketConfig marketConfig = this.marketConfig;
        //计算买的权重 获取k线
        String buyKline = setting5Entity.getBuyKline();
        KlineConfig klineConfig = new HuoBiKlineConfigImpl("10", buyKline);
        List<Kline> lines = null;
        try {
            lines = tradingApi.getKline(marketConfig, klineConfig);
        } catch (Exception e) {
            lines = tradingApi.getKline(marketConfig, klineConfig);
        }
        if (lines != null && !lines.isEmpty()) {
            //买的权重
            if (setting5Entity.getBuyKlineOption().equals(TraceType.up.getStr())) {
                //如果当前收盘价大于上一个线的收盘价 则有上升趋势（simple》？）
                if (lines.get(0).getClose().compareTo(lines.get(1).getClose()) > 0) {
                    //计算上涨的百分比 (当前最新成交价（或收盘价）-开盘参考价)÷开盘参考价×100%
                    absBuyCompare(setting5Entity, lines);
                }
            }
            if (setting5Entity.getBuyKlineOption().equals(TraceType.down.getStr())) {
                //如果当前收盘价小于上一个线的收盘价 则有下降趋势（simple》？）
                if (lines.get(0).getClose().compareTo(lines.get(1).getClose()) < 0) {
                    //计算下架的百分比 (当前最新成交价（或收盘价）-开盘参考价)÷开盘参考价×100%
                    absBuyCompare(setting5Entity, lines);
                }
            }
            //卖的权重
            if (setting5Entity.getSellKlineOption().equals(TraceType.up.getStr())) {
                //上涨趋势
                if (lines.get(0).getClose().compareTo(lines.get(1).getClose()) > 0) {
//                    计算上涨的涨幅
                    absSellCompare(setting5Entity, lines);
                }
            }
            if (setting5Entity.getSellKlineOption().equals(TraceType.down.getStr())) {
                //如果当前收盘价小于上一个线的收盘价 则有下降趋势（simple》？）
                if (lines.get(0).getClose().compareTo(lines.get(1).getClose()) < 0) {
                    //计算跌幅
                    absSellCompare(setting5Entity, lines);
                }
            }
        }
    }

    /**
     * 买：上涨or下架幅度
     *
     * @param setting5Entity
     * @param lines
     */
    private void absBuyCompare(StrategyVo.Setting5Entity setting5Entity, List<Kline> lines) {
        BigDecimal quoteChange = (lines.get(0).getClose().subtract(lines.get(1).getClose())).divide(lines.get(1).getClose(), pricePrecision, RoundingMode.DOWN);
        BigDecimal abs = quoteChange.abs().multiply(new BigDecimal(100));
        if (abs.compareTo(new BigDecimal(setting5Entity.getBuyPercent())) > 0) {
            //涨跌幅大于设置值后
            this.weights.AddBuyTotal(setting5Entity.getBuyWeights());
        }
    }

    /**
     * 卖：上涨or下架幅度
     *
     * @param setting5Entity
     * @param lines
     */
    private void absSellCompare(StrategyVo.Setting5Entity setting5Entity, List<Kline> lines) {
        BigDecimal quoteChange = (lines.get(0).getClose().subtract(lines.get(1).getClose())).divide(lines.get(1).getClose(), pricePrecision, RoundingMode.DOWN);
        BigDecimal abs = (quoteChange.abs().multiply(new BigDecimal(100)));
        if (abs.compareTo(new BigDecimal(setting5Entity.getSellPercent())) > 0) {
            //计算卖的权重
            this.weights.AddSellTotal(setting5Entity.getSellWeights());
        }
    }


    /**
     * 获取订单量超出某个usdt的数量 （买20位）
     * 对于设置一
     *
     * @param marketOrder
     * @return 返回当前买的价格
     */
    public BigDecimal setting1BuyCalculation(MarketOrder marketOrder, StrategyVo.Setting1Entity config) {
        if (marketOrder != null && !marketOrder.getBuy().isEmpty()) {
            logger.info("当前市场买入订单的价格为:{}", marketOrder.getBuy().get(0).getPrice());
            Optional<TradeBean> res = marketOrder.getBuy()
                    .stream()
                    .limit(20)
                    .filter(m -> m.getPrice().compareTo(config.getBuyOrdersUsdt()) > 0)
                    .findFirst();
            if (res.isPresent()) {
                return marketOrder.getBuy().get(0).getPrice();
            }
        }
        return new BigDecimal(0);
    }


    /**
     * 获取卖订单量超出某个(usdt)的数量 （卖20wei）
     *
     * @param marketOrder
     * @return 存在并返回当前卖的价格
     */
    public BigDecimal setting1SellCalculation(MarketOrder marketOrder, StrategyVo.Setting1Entity config) {

        if (marketOrder != null && !marketOrder.getSell().isEmpty()) {
            logger.info("当前市场卖出订单的价格为:{}", marketOrder.getSell().get(0).getPrice());
            Optional<TradeBean> res = marketOrder.getSell()
                    .stream()
                    .limit(20)
                    .filter(m -> m.getPrice().compareTo(config.getSellOrdersUsdt()) > 0)
                    .findFirst();
            if (res.isPresent()) {
                return marketOrder.getSell().get(0).getPrice();
            }
        }
        return new BigDecimal(0);
    }

    /**
     * 获取当前买订单的usdt超出某个usdt的数量 （买）
     *
     * @param marketOrder
     * @return 返回当前买的价格
     */
    public BigDecimal setting2BuyCalculation(MarketOrder marketOrder, StrategyVo.Setting2Entity config) {
        if (marketOrder != null && !marketOrder.getBuy().isEmpty()) {
            BigDecimal currentBuyPrice = marketOrder.getBuy().get(0).getPrice();
            if (currentBuyPrice.compareTo(config.getBuyOrderUsdt()) > 0) {
                return currentBuyPrice;
            }
        }
        return new BigDecimal(0);
    }


    /**
     * 获取当前订单卖的usdt超出某个usdt的数量 （卖）
     *
     * @param marketOrder
     * @return 返回当前买的价格
     */
    public BigDecimal setting2SellCalculation(MarketOrder marketOrder, StrategyVo.Setting2Entity config) {

        if (marketOrder != null && !marketOrder.getSell().isEmpty()) {
            BigDecimal currentSellPrice = marketOrder.getSell().get(0).getPrice();
            if (currentSellPrice.compareTo(config.getSellOrderUsdt()) > 0) {
                return currentSellPrice;
            }
        }
        return new BigDecimal(0);
    }


    /**
     * 订单状态
     */
    @Data
    private static class OrderState {

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
        private com.qklx.qt.core.enums.OrderType orderType = null;

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
                    .add("订单id", id)
                    .add("type", type)
                    .add("price", price)
                    .add("amount", amount)
                    .toString();
        }
    }


    static class Weights {
        private Integer buyTotal = 0;
        private Integer sellTotal = 0;


        public Integer getBuyTotal() {
            return buyTotal;
        }

        public Integer getSellTotal() {
            return sellTotal;
        }

        public void AddBuyTotal(Integer buyTotal) {
            this.buyTotal += buyTotal;
        }

        public void AddSellTotal(Integer sellTotal) {
            this.sellTotal += sellTotal;
        }

        public void reSet() {
            this.buyTotal = 0;
            this.sellTotal = 0;
        }

    }

}
