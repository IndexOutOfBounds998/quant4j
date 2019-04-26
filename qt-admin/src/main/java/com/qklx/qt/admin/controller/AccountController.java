package com.qklx.qt.admin.controller;


import com.qklx.qt.admin.service.AccountService;
import com.qklx.qt.core.vo.AccountVo;
import com.qklx.qt.core.api.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author yang
 * @since 2019-04-08
 */
@RestController
@RequestMapping("/account")
public class AccountController extends BaseController {

    @Autowired
    AccountService accountService;

    @GetMapping("/accountsByUid")
    public ApiResult getAccountsByUid(int uid, int page, int limit) {
        return accountService.selectListByUid(uid, page, limit);
    }

    @GetMapping("/accounts")
    public ApiResult getAccountsByUid(int uid) {
        return accountService.accounts(uid);
    }

    /**
     * 添加或者更新一个账户
     *
     * @return
     */
    @PostMapping("/addOrUpdate")
    public ApiResult addAccount(@RequestBody AccountVo account) {
        return accountService.addOrUpdate(account);
    }


    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        return null;
    }
}

