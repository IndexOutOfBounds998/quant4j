package com.qklx.qt.admin.component;

import com.alibaba.fastjson.JSON;
import com.qklx.qt.admin.entity.Orders;
import com.qklx.qt.admin.entity.User;
import com.qklx.qt.admin.service.IMailService;
import com.qklx.qt.common.config.RedisUtil;
import com.qklx.qt.common.config.VpnProxyConfig;
import com.qklx.qt.common.constans.RobotRedisKeyConfig;
import com.qklx.qt.core.api.ApiClient;
import com.qklx.qt.core.response.OrdersDetail;
import com.qklx.qt.core.response.OrdersDetailResponse;
import com.qklx.qt.core.vo.OrderTaskMessage;
import io.netty.util.internal.ConcurrentSet;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.qklx.qt.common.utils.JsonFormate.parseJsonToString;

/***
 * 消息接收者（订阅者）  需要注入到springboot中 @Value(value = "${landen.ip}")
 *     private String ip;
 *     @Value(value = "${landen.port}")
 *     private int port;
 */
@Slf4j
public class OrderIdReceiver {

    /**
     * 历史的订单id放到redis的set里
     */
    private final String orderIdsKey = "history_order_ids";

    private VpnProxyConfig vpnProxyConfig;
    private IMailService iMailService;
    private RedisUtil redisUtil;

    public OrderIdReceiver(VpnProxyConfig vpnProxyConfig,
                           IMailService iMailService,
                           RedisUtil util) {
        this.vpnProxyConfig = vpnProxyConfig;
        this.iMailService = iMailService;
        this.redisUtil = util;
    }

    /**
     * 收到通道的消息之后执行的方法
     *
     * @param message
     */
    public void receiveMessage(String message) {
        try {
            OrderTaskMessage msg = JSON.parseObject(parseJsonToString(message), OrderTaskMessage.class);
            //处理成功的订单
            ApiClient apiClient = new ApiClient(msg.getAccessKey(), msg.getSecretKey(), vpnProxyConfig);
            OrdersDetailResponse<OrdersDetail> detail = null;
            try {
                detail = apiClient.ordersDetail(msg.getOrderId().toString());
            } catch (Exception e) {
                e.printStackTrace();
                //重试一次
                detail = apiClient.ordersDetail(msg.getOrderId().toString());
            }
            //只保存已经成功的订单
            if (detail != null && detail.getStatus().equals(RobotRedisKeyConfig.ok) && detail.getData().getState().contains("filled")) {
                OrdersDetail data = detail.getData();
                Orders order = new Orders();
                order.setOrderId(Long.parseLong(data.getId()));
                order.setAccountId(Long.parseLong(data.getAccountId()));
                order.setAmount(new BigDecimal(data.getAmount()));
                order.setCreateTime(data.getCreatedAt());
                order.setFieldAmount(new BigDecimal(data.getFieldAmount()));
                order.setFieldCashAmount(new BigDecimal(data.getFieldCashAmount()).setScale(8, RoundingMode.DOWN));
                order.setFinishedTime(data.getFinishedAt());
                if (data.getState().equals("filled")) {
                    order.setOrderState("全部成交");
                } else {
                    order.setOrderState("部分成交");
                }
//                order.setOrderState(data.getState());
                order.setFieldFees(new BigDecimal(data.getFieldFees()));
                order.setSymbol(data.getSymbol());
                order.setOrderType(data.getType());
                order.setRobotId((msg.getRobotId()));
                if (data.getType().contains("market")) {
                    BigDecimal price = new BigDecimal(data.getFieldCashAmount()).divide(new BigDecimal(data.getFieldAmount()), 8, RoundingMode.DOWN);
                    order.setPrice(price);
                } else {
                    order.setPrice(new BigDecimal(data.getPrice()));
                }
                if (order.insertOrUpdate()) {
                    log.info("订单id{}插入或更新成功", msg.getOrderId());
                    User user = new User();
                    user = user.selectById(msg.getUserId());
                    //发送邮件
                    if (!redisUtil.sHasKey(orderIdsKey, msg.getOrderId())) {
                        if (user != null && user.getEnableMail() == 1) {
                            iMailService.sendSimpleMail(user.getSendMail(), "机器人下单啦！", order.toString());
                            log.info("发送邮件{}", order.toString());
                        }
                    }
                }
                //保存进set
                redisUtil.sSet(orderIdsKey, msg.getOrderId());
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            log.error("订单插入失败{}", e.getMessage());
        }
    }
}
