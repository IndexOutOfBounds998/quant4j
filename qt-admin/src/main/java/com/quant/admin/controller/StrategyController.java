package com.quant.admin.controller;

import com.alibaba.fastjson.JSONObject;
import com.quant.admin.service.StrategyService;
import com.quant.common.domain.to.BuyAndSellIndicatorTo;
import com.quant.common.domain.to.llIndicatorTo;
import com.quant.core.api.ApiResult;
import com.quant.common.enums.Status;
import com.quant.common.domain.vo.StrategyVo;
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
     * 添加一个策略
     *
     * @return
     */
    @PostMapping("/addOrUpdateIndicatorStrategy")
    public ApiResult addOrUpdateIndicatorStrategy(@RequestBody llIndicatorTo to, HttpServletRequest httpRequest) {
        String uid = getUid(httpRequest);
        if (uid == null) {
            return new ApiResult(Status.Login_out);
        } else {
            return strategyService.addOrUpdateIndicatorStrategy(to, uid);
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
