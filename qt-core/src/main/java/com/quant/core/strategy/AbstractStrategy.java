package com.quant.core.strategy;

import com.alibaba.fastjson.JSON;
import com.google.common.base.MoreObjects;
import com.quant.common.config.RedisUtil;
import com.quant.common.constans.RobotRedisKeyConfig;
import com.quant.common.domain.response.OrdersDetail;
import com.quant.common.domain.vo.StrategyVo;
import com.quant.core.config.AccountConfig;
import com.quant.core.config.MarketConfig;
import com.quant.core.redisMq.RedisMqService;
import com.quant.core.trading.ExchangeNetworkException;
import com.quant.core.trading.OrderType;
import com.quant.core.trading.TradingApi;
import com.quant.core.trading.TradingApiException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * 策略基类
 */
@Slf4j
public abstract class AbstractStrategy {


    //当前使用的配额币种
    protected String quotaCurrency;

    //当前使用的基础币种
    protected String baseCurrency;

    //价格精度
    protected int pricePrecision;
    //数量金额精度
    protected int amountPrecision;

    protected OrderState orderState;

    protected String lastOrderState = "last_Order_State_";

    protected String orderProfitIds = "order_Profit_Ids_";
    protected RedisUtil redisUtil;
    protected Integer robotId;
    //标志机器人已经启动
    protected volatile String startkey;
    //标志机器人是否在运行
    protected volatile String isRunKey;
    //redismq 日志推送服务
    protected RedisMqService redisMqService, orderMqService, orderProfitService;


    protected String quotaBalanceKey;

    protected String baseBalanceKey;

    //当前账户余额
    protected BigDecimal quotaBalance;

    //当前账户base余额
    protected BigDecimal baseBalance;

    protected TradingApi tradingApi;
    protected MarketConfig marketConfig;
    //    private OrderState orderState;
    protected AccountConfig accountConfig;
    //    private StrategyVo.BaseInfoEntity baseInfo;

    /**
     * 买入权重计算
     */
    protected abstract void buyCalculation();

    /**
     * 卖出权重计算
     */
    protected abstract void sellCalculation();


    public void init() {
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
    }

    /**
     * 创建订单
     *
     * @param amount
     * @param price
     * @param orderType
     */
    protected void order(
            BigDecimal amount,
            BigDecimal price,
            com.quant.common.enums.OrderType orderType,
            TradingApi tradingApi, OrderType type) {
        //下订单
        try {
            redisMqService.sendMsg("叮叮叮>>>开始下单,下单信息 价格:" + price + "数量:" + amount + "订单类型:" + orderType.getTyoe());
            this.orderState.setId(tradingApi.createOrder(this.marketConfig.markName(), this.accountConfig.accountId(), orderType, amount, price));
            if (this.orderState.getId() != null) {
                redisMqService.sendMsg("下单成功>>>订单信息【" + this.orderState.toString() + "】");
                Thread.sleep(1000);
                this.orderState.setType(type);
                //将订单信息传送到后台 此时的订单可能未成功
                orderMqService.sendMsg(this.orderState.getId());
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
     * 检查是否已经取消了任务
     *
     * @param key
     * @return
     */
    protected boolean checkRobotIsStop(String key) {
        Object o = this.redisUtil.get(key);
        return (boolean) o;
    }

    /**
     * 重试5次
     *
     * @param orderId
     * @return
     */
    protected OrdersDetail getOrderDetail(Long orderId, int i) {
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
     * //获取用户的余额 价格的精度 数量的精度获取quote的交易对 quota 是当前的配额 例如usdt
     */
    protected boolean getQuotaPriceAndPrecision() {
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
     * 获取余额
     *
     * @return
     */
    protected boolean getBalance() {
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
        private com.quant.common.enums.OrderType orderType = null;

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

    public static class Weights {
        private volatile Integer buyTotal = 0;
        private volatile Integer sellTotal = 0;


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
