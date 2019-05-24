package com.quant.common.domain;

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
