package com.quant.admin.task;

import com.quant.admin.entity.Robot;
import com.quant.common.config.RedisUtil;
import com.quant.common.constans.RobotRedisKeyConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RobotStatusTask {

    @Autowired
    RedisUtil redisUtil;

    /**
     * 更新机器人的运行状态
     */
    @Async
    @Scheduled(fixedDelay = 5000, initialDelay = 1000)
    public void updateRobotRunState() {
        Robot robot = new Robot();
        List<Robot> robots = robot.selectAll();
        for (Robot r : robots) {
            String key = RobotRedisKeyConfig.getRobotIsRunStateKey() + r.getId();
            Object o = redisUtil.get(key);
            if (o == null) {
                //已经取消了机器人的运行
                r.setIsRun(0);
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
