package com.qklx.qt.admin.service;

import com.baomidou.mybatisplus.service.IService;
import com.qklx.qt.admin.entity.OrderProfit;
import com.qklx.qt.core.api.ApiResult;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yang
 * @since 2019-04-26
 */
public interface OrderProfitService extends IService<OrderProfit> {

    ApiResult getProfitList(int rid, int page, int limit);
}
