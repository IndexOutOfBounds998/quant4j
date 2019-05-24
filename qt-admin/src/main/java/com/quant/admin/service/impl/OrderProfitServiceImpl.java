package com.quant.admin.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.quant.admin.dao.OrderProfitMapper;
import com.quant.admin.entity.OrderProfit;
import com.quant.admin.service.OrderProfitService;
import com.quant.core.api.ApiResult;
import com.quant.core.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yang
 * @since 2019-04-26
 */
@Slf4j
@Service
public class OrderProfitServiceImpl extends ServiceImpl<OrderProfitMapper, OrderProfit> implements OrderProfitService {

    @Override
    public ApiResult getProfitList(int rid, int page, int limit) {
        try {
            Wrapper<OrderProfit> orderProfitEntityWrapper = new EntityWrapper<>();
            orderProfitEntityWrapper.eq("robot_id", rid);
            orderProfitEntityWrapper.orderDesc(Collections.singleton("create_time"));
            OrderProfit orders = new OrderProfit();
            Page<OrderProfit> ordersPage = orders.selectPage(new Page<>(page, limit), orderProfitEntityWrapper);
            return new ApiResult(Status.SUCCESS, ordersPage);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询盈亏订单列表失败 {}", e.getMessage());
        }
        return new ApiResult(Status.ERROR);
    }
}
