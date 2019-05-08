package com.qklx.qt.admin.component;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.qklx.qt.admin.entity.OrderProfit;
import com.qklx.qt.core.vo.ProfitMessage;
import lombok.extern.slf4j.Slf4j;

import static com.qklx.qt.common.utils.JsonFormate.parseJsonToString;

/***
 * 消息接收者（订阅者）  需要注入到springboot中 @Value(value = "${landen.ip}")
 *     private String ip;
 *     @Value(value = "${landen.port}")
 *     private int port;
 */
@Slf4j
public class ProfitReceiver {


    /**
     * 收到通道的消息之后执行的方法
     *
     * @param message
     */
    public void receiveMessage(String message) {
        ProfitMessage msg = JSON.parseObject(parseJsonToString(message), ProfitMessage.class);

        OrderProfit isExist = new OrderProfit();
        Wrapper<OrderProfit> wrapper = new EntityWrapper<>();
        wrapper.eq("sell_order_id", msg.getSellOrderId());
        wrapper.eq("buy_order_id", msg.getBuyOrderId());

        OrderProfit one = isExist.selectOne(wrapper);

        if (one == null) {
            log.info("插入盈利信息，购买订单id{},卖出订单id{}", msg.getBuyOrderId(), msg.getSellOrderId());
            OrderProfit orderProfit = new OrderProfit();
            orderProfit.setSellOrderId(msg.getSellOrderId());
            orderProfit.setBuyOrderId(msg.getBuyOrderId());
            orderProfit.setRobotId(msg.getRobot_id());
            orderProfit.setBuyAmount(msg.getBuyAmount());
            orderProfit.setBuyPrice(msg.getBuyPrice());
            orderProfit.setSellAmount(msg.getSellAmount());
            orderProfit.setSellPrice(msg.getSellPrice());
            orderProfit.setBuyCashAmount(msg.getBuyCashAmount());
            orderProfit.setSellCashAmount(msg.getSellCashAmount());
            orderProfit.setDiff(msg.getDiff());
            orderProfit.setDivide(msg.getDivide());
            if (orderProfit.insert()) {
                log.info("插入盈利数据成功!!!");
            }
        }
    }
}
