package com.quant.admin.service;

import com.baomidou.mybatisplus.service.IService;
import com.quant.admin.entity.OrderProfit;
import com.quant.core.api.ApiResult;

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
