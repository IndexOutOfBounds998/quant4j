package com.qklx.client.controller;

import com.qklx.client.service.RobotService;
import com.qklx.qt.core.api.ApiResult;
import com.qklx.qt.core.vo.RobotStrategyVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/robot")
public class BotController {

    @Autowired
    RobotService robotService;

    @PostMapping("/operatingRobot")
    public ApiResult operatingRobot(@RequestBody RobotStrategyVo vo) {
        log.info("获取到机器人的配置信息:{}", vo.toString());
        return robotService.operatingRobot(vo);
    }


}
