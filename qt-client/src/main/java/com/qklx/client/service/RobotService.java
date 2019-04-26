package com.qklx.client.service;

import com.qklx.qt.core.api.ApiResult;
import com.qklx.qt.core.vo.RobotStrategyVo;

public interface RobotService {


    ApiResult operatingRobot(RobotStrategyVo vo);
}
