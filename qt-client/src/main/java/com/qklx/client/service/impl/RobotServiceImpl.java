package com.qklx.client.service.impl;

import com.qklx.client.Task.RobotOperate;
import com.qklx.client.service.RobotService;
import com.qklx.qt.common.config.RedisUtil;
import com.qklx.qt.core.api.ApiResult;
import com.qklx.qt.core.enums.Status;
import com.qklx.qt.core.vo.RobotStrategyVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class RobotServiceImpl implements RobotService {
    @Value(value = "${landen.ip}")
    private String ip;
    @Value(value = "${landen.port}")
    private int port;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    ExecutorService executorService;

    @Override
    public ApiResult operatingRobot(RobotStrategyVo vo) {
        //启动机器人
        try {
            executorService.execute(() -> {
                RobotOperate robotOperate = new RobotOperate(redisUtil, ip, port);
                robotOperate.doRobotTask(vo);
            });
            return new ApiResult(Status.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("client端启动机器人发生错误:机器人编号:{},client端地址:{}", vo.getRobotId(), vo.getAddress());
            log.error("错误信息:" + e.getMessage());
            return new ApiResult(Status.startRobotError);
        }
    }
}
