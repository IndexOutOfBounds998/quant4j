package com.quant.client.controller;

import com.quant.common.enums.Status;
import com.quant.common.domain.vo.RobotStrategyVo;
import com.quant.core.api.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    

    @PostMapping("/test")
    public ApiResult test(@RequestBody RobotStrategyVo vo) {
       return new ApiResult(Status.SUCCESS);
    }


}
