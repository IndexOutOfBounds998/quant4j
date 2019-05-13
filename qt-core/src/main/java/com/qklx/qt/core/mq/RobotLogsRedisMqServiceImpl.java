package com.qklx.qt.core.mq;

import com.alibaba.fastjson.JSON;
import com.qklx.qt.common.config.RedisUtil;
import com.qklx.qt.common.constans.RobotRedisKeyConfig;
import com.qklx.qt.core.to.RobotRunMessage;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class RobotLogsRedisMqServiceImpl implements RedisMqService {

    private ExecutorService executorService = Executors.newFixedThreadPool(3);

    private RedisUtil redisUtil;
    private int robotId;
    private int userId;

    public RobotLogsRedisMqServiceImpl(RedisUtil redisUtil, int robotId, int userId) {
        this.redisUtil = redisUtil;
        this.robotId = robotId;
        this.userId = userId;
    }

    private ThreadLocal<SimpleDateFormat> simpleDateFormatThreadLocal = new ThreadLocal<>();


    /**
     * 异步方式提交
     *
     * @param msg
     */
    @Override
    public void sendMsg(Object msg) {
        log.info("机器人{}日志:{}", robotId, msg.toString());
        executorService.execute(() -> {
            if (simpleDateFormatThreadLocal.get() == null) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                simpleDateFormatThreadLocal.set(simpleDateFormat);
            }
            try {
                RobotRunMessage robotRunMessage = new RobotRunMessage();
                robotRunMessage.setMsg(msg.toString());
                robotRunMessage.setRobotId(robotId);
                robotRunMessage.setDate(simpleDateFormatThreadLocal.get().format(new Date()));
                robotRunMessage.setUserId(userId);
                redisUtil.convertAndSend(RobotRedisKeyConfig.getRobot_msg_queue(), JSON.toJSONString(robotRunMessage));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
}
