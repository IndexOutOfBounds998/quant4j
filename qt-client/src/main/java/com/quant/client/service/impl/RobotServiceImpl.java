package com.quant.client.service.impl;

import com.quant.client.task.IndicatorRobotOperate;
import com.quant.client.task.SimpleNumRobotOperate;
import com.quant.client.service.RobotService;
import com.quant.common.config.RedisUtil;
import com.quant.common.config.VpnProxyConfig;
import com.quant.common.domain.vo.IndicatorStrategyVo;
import com.quant.core.api.ApiResult;
import com.quant.common.enums.Status;
import com.quant.common.domain.vo.RobotStrategyVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class RobotServiceImpl implements RobotService {
    @Autowired
    VpnProxyConfig vpnProxyConfig;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    ExecutorService executorService;

    @Override
    public ApiResult operatingRobot(RobotStrategyVo vo) {
        //启动机器人
        try {
            executorService.execute(() -> {
                SimpleNumRobotOperate robotOperate = new SimpleNumRobotOperate(redisUtil, vpnProxyConfig);
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

    @Override
    public ApiResult operatingIndicatorRobot(IndicatorStrategyVo vo) {
        //启动机器人
        try {
            executorService.execute(() -> {
                IndicatorRobotOperate robotOperate = new IndicatorRobotOperate(redisUtil, vpnProxyConfig);
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
