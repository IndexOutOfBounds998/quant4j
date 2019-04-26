package com.qklx.qt.admin.controller;


import com.qklx.qt.admin.service.OrdersService;
import com.qklx.qt.core.api.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author yang
 * @since 2019-04-22
 */
@RestController
@RequestMapping("/order")
public class OrderController extends AbstractController {

    @Autowired
    private OrdersService ordersService;

    /**
     * 订单列表
     *
     * @return
     */
    @GetMapping("/list")
    public ApiResult list(int rid, int page, int limit) {
        return ordersService.getOrderByRobotId(rid,page,limit);
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return null;
    }
}

