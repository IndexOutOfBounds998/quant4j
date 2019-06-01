package com.quant.client.controller;

import com.quant.common.enums.Status;
import com.quant.common.domain.vo.RobotStrategyVo;
import com.quant.core.api.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    

    @GetMapping("/test")
    public ApiResult test() {
       return new ApiResult(Status.SUCCESS);
    }


}
