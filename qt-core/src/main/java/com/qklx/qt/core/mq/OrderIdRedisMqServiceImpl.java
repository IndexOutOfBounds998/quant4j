package com.qklx.qt.core.mq;

import com.alibaba.fastjson.JSON;
import com.qklx.qt.common.config.RedisUtil;
import com.qklx.qt.common.constans.RobotRedisKeyConfig;
import com.qklx.qt.core.config.AccountConfig;
import com.qklx.qt.core.vo.OrderTaskMessage;

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

    }
}
