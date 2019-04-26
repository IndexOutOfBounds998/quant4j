package com.qklx.qt.admin.component;

import com.alibaba.fastjson.JSON;
import com.qklx.qt.admin.entity.Orders;
import com.qklx.qt.common.constans.RobotRedisKeyConfig;
import com.qklx.qt.common.utils.DateUtils;
import com.qklx.qt.core.api.ApiClient;
import com.qklx.qt.core.response.OrdersDetail;
import com.qklx.qt.core.response.OrdersDetailResponse;
import com.qklx.qt.core.vo.OrderTaskMessage;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import static com.qklx.qt.common.utils.JsonFormate.parseJsonToString;

/***
 * 消息接收者（订阅者）  需要注入到springboot中 @Value(value = "${landen.ip}")
 *     private String ip;
 *     @Value(value = "${landen.port}")
 *     private int port;
 */
@Slf4j
public class OrderIdReceiver {


    private String ip;

    private int port;

    public OrderIdReceiver(String ip, int port) {
        this.ip = ip;
        this.port = port;
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
            ApiClient apiClient = new ApiClient(msg.getAccessKey(), msg.getSecretKey(), this.ip, this.port);
            OrdersDetailResponse<OrdersDetail> detail = apiClient.ordersDetail(msg.getOrderId().toString());
            if (detail.getStatus().equals(RobotRedisKeyConfig.ok)) {
                OrdersDetail data = detail.getData();
                Orders order = new Orders();
                order.setAccountId(Long.parseLong(data.getAccountId()));
                order.setAmount(new BigDecimal(data.getAmount()));
                order.setCreateTime(data.getCreatedAt());
                order.setFieldAmount(new BigDecimal(data.getFieldAmount()));

                order.setFieldCashAmount(new BigDecimal(data.getFieldCashAmount()));

                order.setFinishedTime(data.getFinishedAt());

                order.setOrderState(data.getState());

                order.setFieldFees(new BigDecimal(data.getFieldFees()));

                order.setOrderId(Long.parseLong(data.getId()));
                order.setPrice(new BigDecimal(data.getPrice()));
                order.setSymbol(data.getSymbol());
                order.setOrderType(data.getType());
                order.setRobotId((msg.getRobotId()));
                if (order.insertOrUpdate()) {
                    log.info("订单{}插入成功", msg.getOrderId());
                }

            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            log.error("订单插入失败{}", e.getMessage());
        }
    }
}
