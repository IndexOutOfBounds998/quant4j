package com.quant.common.domain.bo;

import lombok.Data;

import java.util.Date;

@Data
public class RobotBo {
    private static final long serialVersionUID = 1L;


    private Integer id;

    private String robotName;

    private String symbol;

    private String userId;

    private Integer strategyId;

    private String clientAddress;

    private Date createTime;

    private Integer isDelete;

    private Integer isRun;

    private String strategyName;


}
