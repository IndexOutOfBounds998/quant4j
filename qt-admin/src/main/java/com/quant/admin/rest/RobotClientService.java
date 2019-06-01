package com.quant.admin.rest;

import com.quant.common.domain.vo.IndicatorStrategyVo;
import com.quant.core.api.ApiResult;
import com.quant.common.domain.vo.RobotStrategyVo;

public interface RobotClientService {

    ApiResult operatingRobot(String url, RobotStrategyVo vo);

    ApiResult operatingIndicatorRobot(String url, IndicatorStrategyVo strategyVo);
}
