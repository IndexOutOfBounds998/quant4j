package com.quant.core.redisMq;

import com.alibaba.fastjson.JSON;
import com.quant.common.config.RedisUtil;
import com.quant.common.constans.RobotRedisKeyConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yang

 * @desc 订单盈利推送到admin
 * @date 2019/7/9
 */
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
