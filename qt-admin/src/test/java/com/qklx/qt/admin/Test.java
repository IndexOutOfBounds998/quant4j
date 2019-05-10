package com.qklx.qt.admin;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Test {
    public static void main(String[] args) {
        BigDecimal a = new BigDecimal(1);
        BigDecimal b = new BigDecimal(3);
        BigDecimal divide = a.divide(b, 2, RoundingMode.UP);
        System.out.println(divide);
    }
}
