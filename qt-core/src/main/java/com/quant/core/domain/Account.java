package com.quant.core.domain;

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
