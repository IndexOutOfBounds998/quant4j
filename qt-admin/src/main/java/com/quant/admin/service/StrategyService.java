package com.quant.admin.service;

import com.baomidou.mybatisplus.service.IService;
import com.quant.common.domain.entity.Strategy;
import com.quant.core.api.ApiResult;
import com.quant.common.domain.vo.StrategyVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yang
 * @since 2019-04-15
 */
public interface StrategyService extends IService<Strategy> {

    ApiResult addOrUpdateStrategy(StrategyVo strategyVo, String uid);

    ApiResult strategyList(String uid);

    ApiResult simpleStrategyList(String uid);

    ApiResult getStrategyById(int id, String uid);

    ApiResult deleteStrategy(int id, String uid);
}
