package com.quant.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class OrderTaskMessage {

    private Long orderId;

    private String accessKey;

    private String secretKey;

    private int robotId;

    private String userId;


}
