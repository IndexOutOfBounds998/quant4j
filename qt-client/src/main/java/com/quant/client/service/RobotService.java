package com.quant.client.service;

import com.quant.common.domain.vo.IndicatorStrategyVo;
import com.quant.common.domain.vo.RobotStrategyVo;
import com.quant.core.api.ApiResult;

public interface RobotService {


    ApiResult operatingRobot(RobotStrategyVo vo);

    ApiResult operatingIndicatorRobot(IndicatorStrategyVo vo);
}
