package com.quant.client.controller;

import com.quant.client.service.RobotService;
import com.quant.common.domain.to.BuyAndSellIndicatorTo;
import com.quant.common.domain.vo.IndicatorStrategyVo;
import com.quant.core.api.ApiResult;
import com.quant.common.domain.vo.RobotStrategyVo;
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

    @PostMapping("/operatingIndicatorRobot")
    public ApiResult operatingIndicatorRobot(@RequestBody IndicatorStrategyVo vo) {
        log.info("获取到机器人的配置信息:{}", vo.toString());
        return robotService.operatingIndicatorRobot(vo);
    }



}
