package com.quant.admin.controller;

import com.quant.admin.service.BalanceService;
import com.quant.core.api.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author yang
 * @since 2019-04-15
 */
@RestController
@RequestMapping("/balance")
public class BalanceController extends BaseController {

    @Autowired
    BalanceService balanceService;


    @GetMapping("/getBalanceList")
    public ApiResult getBalanceList(String accountId) {
        return balanceService.getBalanceListByAccountId(accountId);
    }


    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return null;
    }
}

