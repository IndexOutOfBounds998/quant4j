package com.quant.common.domain.vo;

import lombok.Data;

@Data
public class RobotStrategyVo {

    //机器人id
    int robotId;

    //机器人托管地址
    String address;

    String appKey;

    String appSecret;

    String symbol;
    //策略信息
    StrategyVo strategyVo;

    Account accountConfig;

    @Override
    public String toString() {
        return "RobotStrategyVo{" +
                "robotId=" + robotId +
                ", address='" + address + '\'' +
                ", strategyVo=" + strategyVo +
                '}';
    }
}
