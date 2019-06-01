package com.quant.client.service;

import com.quant.common.domain.to.BuyAndSellIndicatorTo;
import com.quant.common.domain.vo.IndicatorStrategyVo;
import com.quant.core.api.ApiResult;
import com.quant.common.domain.vo.RobotStrategyVo;

public interface RobotService {


    ApiResult operatingRobot(RobotStrategyVo vo);

    ApiResult operatingIndicatorRobot(IndicatorStrategyVo vo);
}
