package com.qklx.qt.core.mq;

import com.alibaba.fastjson.JSON;
import com.qklx.qt.common.config.RedisUtil;
import com.qklx.qt.common.constans.RobotRedisKeyConfig;
import com.qklx.qt.core.to.RobotRunMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RobotLogsRedisMqServiceImpl implements RedisMqService {
    private RedisUtil redisUtil;
    private int robotId;
    private int userId;

    public RobotLogsRedisMqServiceImpl(RedisUtil redisUtil, int robotId, int userId) {
        this.redisUtil = redisUtil;
        this.robotId = robotId;
        this.userId = userId;
    }

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void sendMsg(Object msg) {
        try {
            RobotRunMessage robotRunMessage = new RobotRunMessage();
            robotRunMessage.setMsg(msg.toString());
            robotRunMessage.setRobotId(robotId);
            robotRunMessage.setDate(simpleDateFormat.format(new Date()));
            robotRunMessage.setUserId(userId);
            redisUtil.convertAndSend(RobotRedisKeyConfig.getRobot_msg_queue(), JSON.toJSONString(robotRunMessage));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
