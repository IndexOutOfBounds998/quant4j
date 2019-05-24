package com.quant.admin.controller;

import com.quant.admin.service.SymbolService;
import com.quant.core.api.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("symbol")
@RestController
public class SymbolController extends BaseController {

    @Autowired
    SymbolService symbolService;


    @GetMapping("/symbols")
    public ApiResult getSymbols() {
        return symbolService.getSymbols();
    }
}
