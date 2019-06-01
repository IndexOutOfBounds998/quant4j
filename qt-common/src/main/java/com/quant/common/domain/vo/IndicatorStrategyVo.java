package com.quant.common.domain.vo;

import com.quant.common.domain.to.BuyAndSellIndicatorTo;
import com.quant.common.domain.to.llIndicatorTo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class IndicatorStrategyVo {

    //机器人id
    int robotId;

    //机器人托管地址
    String address;

    String appKey;

    String appSecret;

    String symbol;

    llIndicatorTo indicatorTo;

    Account accountConfig;

}
