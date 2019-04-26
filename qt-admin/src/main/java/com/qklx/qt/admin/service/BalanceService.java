package com.qklx.qt.admin.service;

import com.baomidou.mybatisplus.service.IService;
import com.qklx.qt.admin.entity.Balance;
import com.qklx.qt.core.api.ApiResult;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yang
 * @since 2019-04-15
 */
public interface BalanceService extends IService<Balance> {

    ApiResult getBalanceListByAccountId(String accountId);
}
