package com.quant.admin.rest;

import com.quant.core.api.ApiResult;
import com.quant.common.vo.RobotStrategyVo;

public interface RobotClientService {

    ApiResult operatingRobot(String url, RobotStrategyVo vo);
}
