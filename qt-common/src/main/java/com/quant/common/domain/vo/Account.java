package com.quant.common.domain.vo;

import lombok.Data;

@Data
public class Account {

    String id;
    String type;
    String state;

    String accessKey;

    String secretKey;

    String userId;

}
