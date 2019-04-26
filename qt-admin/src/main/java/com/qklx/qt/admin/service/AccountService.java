package com.qklx.qt.admin.service;

import com.qklx.qt.admin.entity.Account;
import com.baomidou.mybatisplus.service.IService;
import com.qklx.qt.core.vo.AccountVo;
import com.qklx.qt.core.api.ApiResult;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author yang
 * @since 2019-04-08
 */
public interface AccountService extends IService<Account> {

    /**
     * 添加一个账户
     *
     * @param account
     */
    ApiResult addOrUpdate(AccountVo account);

    ApiResult selectListByUid(int uid, int page, int limit);

    ApiResult accounts(int uid);
}
