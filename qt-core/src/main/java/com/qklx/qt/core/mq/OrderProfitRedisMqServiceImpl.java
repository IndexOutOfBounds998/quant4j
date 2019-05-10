package com.qklx.qt.core.mq;

import com.alibaba.fastjson.JSON;
import com.qklx.qt.common.config.RedisUtil;
import com.qklx.qt.common.constans.RobotRedisKeyConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderProfitRedisMqServiceImpl implements RedisMqService {

    private RedisUtil redisUtil;


    public OrderProfitRedisMqServiceImpl(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;

    }

    @Override
    public void sendMsg(Object msg) {
        redisUtil.convertAndSend(RobotRedisKeyConfig.getOrder_profit(), JSON.toJSONString(msg));
        log.info("机器人盈利信息日志:" + JSON.toJSONString(msg));
    }
}
