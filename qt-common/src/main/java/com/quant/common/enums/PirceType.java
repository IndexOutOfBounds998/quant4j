package com.quant.common.enums;

public enum PirceType {
    isLimit(1),
    notLimit(0);


    Integer type;

    PirceType(Integer type) {
        this.type = type;
    }

    public Integer getType() {
        return type;
    }
}
