package com.qklx.temp.service.impl;

import com.qklx.temp.entity.Orders;
import com.qklx.temp.dao.OrdersMapper;
import com.qklx.temp.service.OrdersService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yang
 * @since 2019-05-31
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

}
