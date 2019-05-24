package com.quant.admin.controller;

import com.quant.admin.service.StrategyService;
import com.quant.core.api.ApiResult;
import com.quant.core.enums.Status;
import com.quant.core.vo.StrategyVo;
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

    @GetMapping("/deleteStrategy")
    public ApiResult deleteStrategy(HttpServletRequest httpRequest,int id) {
        String uid = getUid(httpRequest);
        if (uid == null) {
            return new ApiResult(Status.Login_out);
        } else {
            return strategyService.deleteStrategy(id,uid);
        }
    }
}
