package com.quant.admin.controller;


import com.quant.common.domain.entity.User;
import com.quant.admin.service.UserService;
import com.quant.core.api.ApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author yang
 * @since 2019-04-08
 */
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(UserController.class);


    @Autowired
    UserService userService;

    /**
     * 登录
     *
     * @param user
     * @return
     */
    @PostMapping(value = "/login")
    public ApiResult login(@RequestBody User user) {
        return userService.login(user);
    }

    /**
     * 退出登录
     *
     * @return
     */
    @PostMapping(value = "/logout")
    public ApiResult logout() {
        return userService.logout();
    }




    @GetMapping(value = "/info")
    public ApiResult info(@NotNull String token) {
        return userService.info(token);
    }

    @GetMapping(value = "/getUserEmail")
    public ApiResult getUserEmail(@NotNull Integer id) {
        return userService.getUserEmail(id);
    }


    @PostMapping(value = "/emailEditer")
    public ApiResult emailEditer(@NotNull @RequestBody User user) {
        return userService.emailEditer(user);
    }



    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        logger.info("method====" + httpServletRequest.getMethod());
        return null;
    }
}

