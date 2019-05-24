package com.quant.core.enums;


/**
 * spot：现货账户， margin：杠杆账户，otc：OTC 账户，point：点卡账户
 */

public enum AType {


    SPOT("spot"),
    MARGIN("margin"),
    OTC("otc"),
    POINT("point");


    String str;

    AType(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }}


