package com.qklx.qt.core.mq;

import com.alibaba.fastjson.JSON;
import com.qklx.qt.common.config.RedisUtil;
import com.qklx.qt.common.constans.RobotRedisKeyConfig;
import com.qklx.qt.core.config.AccountConfig;
import com.qklx.qt.core.vo.OrderTaskMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderIdRedisMqServiceImpl implements RedisMqService {

    private RedisUtil redisUtil;

    private AccountConfig accountConfig;

    private int robotId;

    public OrderIdRedisMqServiceImpl(RedisUtil redisUtil, AccountConfig accountConfig, int robotId) {
        this.redisUtil = redisUtil;
        this.accountConfig = accountConfig;
        this.robotId = robotId;
    }

    @Override
    public void sendMsg(Object orderId) {
        OrderTaskMessage message = new OrderTaskMessage(Long.parseLong(orderId.toString()), this.accountConfig.getAccessKey(), this.accountConfig.getSecretKey(), this.robotId, this.accountConfig.getUserId());
        redisUtil.convertAndSend(RobotRedisKeyConfig.getQueue(), JSON.toJSONString(message));
        log.info("机器人订单信息日志:" + JSON.toJSONString(message));
    }
}
