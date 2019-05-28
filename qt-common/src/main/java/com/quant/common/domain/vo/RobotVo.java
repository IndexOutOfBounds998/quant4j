package com.quant.common.domain.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RobotVo {
    Integer id;
    @NotNull
    String robotName;
    @NotNull
    String symbol;
    @NotNull
    int strategyId;
    @NotNull
    String nodeAddress;
    @NotNull
    String userId;
    @NotNull
    int accountId;
}
