package com.qklx.temp.service.impl;

import com.qklx.temp.entity.Order;
import com.qklx.temp.dao.OrderMapper;
import com.qklx.temp.service.OrderService;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yang
 * @since 2019-04-25
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

}
