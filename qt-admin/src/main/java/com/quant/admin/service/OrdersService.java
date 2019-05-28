package com.quant.admin.service;

import com.baomidou.mybatisplus.service.IService;
import com.quant.common.domain.entity.Orders;
import com.quant.core.api.ApiResult;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yang
 * @since 2019-04-22
 */
public interface OrdersService extends IService<Orders> {

    ApiResult getOrderByRobotId(int robotId, int page, int limit);
}
