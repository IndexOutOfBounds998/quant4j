package com.quant.admin.service;

import com.baomidou.mybatisplus.service.IService;
import com.quant.admin.entity.User;
import com.quant.core.api.ApiResult;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author yang
 * @since 2019-04-08
 */
public interface UserService extends IService<User> {

    /**
     * simple登录
     * @param params
     * @return
     */
    ApiResult login(User params);


    ApiResult info(String token);

    ApiResult logout();

    ApiResult getUserEmail(@NotNull Integer id);

    ApiResult emailEditer(@NotNull User user);
}
