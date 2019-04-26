package com.qklx.qt.admin.rest;

import com.qklx.qt.core.api.ApiResult;
import com.qklx.qt.core.vo.RobotStrategyVo;

public interface RobotClientService {

    ApiResult operatingRobot(String url, RobotStrategyVo vo);
}
