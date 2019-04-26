package com.qklx.qt.admin.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.qklx.qt.admin.dao.OrdersMapper;
import com.qklx.qt.admin.entity.Orders;
import com.qklx.qt.admin.service.OrdersService;
import com.qklx.qt.core.api.ApiResult;
import com.qklx.qt.core.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yang
 * @since 2019-04-22
 */
@Slf4j
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Override
    public ApiResult getOrderByRobotId(int rid, int page, int limit) {
        try {
            Wrapper<Orders> ordersWrapper = new EntityWrapper<>();
            ordersWrapper.eq("robot_id", rid);
            ordersWrapper.orderDesc(Collections.singleton("create_time"));
            Orders orders = new Orders();
            Page<Orders> ordersPage = orders.selectPage(new Page<>(page, limit), ordersWrapper);
            return new ApiResult(Status.SUCCESS, ordersPage);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询机器人订单列表失败 {}", e.getMessage());
        }
        return new ApiResult(Status.ERROR);
    }
}
