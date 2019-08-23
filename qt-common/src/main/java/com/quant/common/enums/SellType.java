package com.quant.common.enums;

public enum SellType {
    sellAll(1),
    sellCustom(0);


    Integer type;

    SellType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }
}
