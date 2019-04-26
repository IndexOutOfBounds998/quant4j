package com.qklx.qt.admin.service;

import com.baomidou.mybatisplus.service.IService;
import com.qklx.qt.admin.entity.Orders;
import com.qklx.qt.core.api.ApiResult;

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
