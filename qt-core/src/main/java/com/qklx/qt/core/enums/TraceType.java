package com.qklx.qt.core.enums;

public enum TraceType {
    up("1"),
    down("2");


    String str;

    TraceType(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }
}
