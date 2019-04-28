package com.qklx.qt.admin.task;

import com.qklx.qt.admin.entity.Robot;
import com.qklx.qt.common.config.RedisUtil;
import com.qklx.qt.common.constans.RobotRedisKeyConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RobotCheckTask {

    @Autowired
    RedisUtil redisUtil;

    /**
     * 更新机器人的运行状态
     */
    @Async
    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void updateRobotRunState() {
        Robot robot = new Robot();
        List<Robot> robots = robot.selectAll();
        for (Robot r : robots) {
            String key = RobotRedisKeyConfig.getRobotIsRunStateKey() + r.getId();
            String startKey = RobotRedisKeyConfig.getRobotIsStartStateKey() + r.getId();
            Object o = redisUtil.get(key);
            if (o == null) {
                //已经取消了机器人的运行
                r.setIsRun(0);
                redisUtil.set(startKey, false);
            } else {
                r.setIsRun(1);
            }
            try {
                r.updateById();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("更新机器人状态失败");
            }
        }


    }
}
