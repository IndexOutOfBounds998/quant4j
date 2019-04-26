package com.qklx.qt.core.mq;

import com.alibaba.fastjson.JSON;
import com.qklx.qt.common.config.RedisUtil;
import com.qklx.qt.common.constans.RobotRedisKeyConfig;
import com.qklx.qt.core.config.AccountConfig;
import com.qklx.qt.core.vo.OrderTaskMessage;
import com.qklx.qt.core.vo.ProfitMessage;

public class OrderProfitRedisMqServiceImpl implements RedisMqService {

    private RedisUtil redisUtil;



    public OrderProfitRedisMqServiceImpl(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;

    }

    @Override
    public void sendMsg(Object msg) {
        redisUtil.convertAndSend(RobotRedisKeyConfig.getOrder_profit(), JSON.toJSONString(msg));
    }
}
