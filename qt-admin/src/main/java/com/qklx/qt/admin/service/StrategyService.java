package com.qklx.qt.admin.service;

import com.baomidou.mybatisplus.service.IService;
import com.qklx.qt.admin.entity.Strategy;
import com.qklx.qt.core.api.ApiResult;
import com.qklx.qt.core.vo.StrategyVo;

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
