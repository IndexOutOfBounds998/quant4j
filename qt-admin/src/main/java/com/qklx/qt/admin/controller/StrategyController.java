package com.qklx.qt.admin.controller;

import com.qklx.qt.admin.service.StrategyService;
import com.qklx.qt.core.enums.Status;
import com.qklx.qt.core.vo.StrategyVo;
import com.qklx.qt.core.api.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/strategy")
public class StrategyController extends BaseController{
    @Autowired
    StrategyService strategyService;

    /**
     * 添加一个策略
     *
     * @return
     */
    @PostMapping("/addOrUpdateStrategy")
    public ApiResult addOrUpdateStrategy(@RequestBody StrategyVo strategyVo, HttpServletRequest httpRequest) {
        String uid = getUid(httpRequest);
        if (uid == null) {
            return new ApiResult(Status.Login_out);
        } else {
            return strategyService.addOrUpdateStrategy(strategyVo, uid);
        }
    }

    /**
     * 获取我的策略列表
     *
     * @return
     */
    @GetMapping("/strategyList")
    public ApiResult strategyList(HttpServletRequest httpRequest) {
        String uid = getUid(httpRequest);
        if (uid == null) {
            return new ApiResult(Status.Login_out);
        } else {
            return strategyService.strategyList(uid);
        }
    }

    /**
     * 获取我的策略列表
     *
     * @return
     */
    @GetMapping("/simpleStrategyList")
    public ApiResult simpleStrategyList(HttpServletRequest httpRequest) {
        String uid = getUid(httpRequest);
        if (uid == null) {
            return new ApiResult(Status.Login_out);
        } else {
            return strategyService.simpleStrategyList(uid);
        }
    }


   @GetMapping("/getStrategyById")
   public ApiResult getStrategyById(HttpServletRequest httpRequest,int id) {
       String uid = getUid(httpRequest);
       if (uid == null) {
           return new ApiResult(Status.Login_out);
       } else {
           return strategyService.getStrategyById(id,uid);
       }
   }
}
