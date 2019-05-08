package com.qklx.qt.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainTest {
    static final String access_key = "ce9f0744-3fc8eea2-6bc64c6f-865ff";
    static final String secret_key = "f1f2003f-4c8ee5d3-9178be8a-25d54";
    private static Integer a = 0;
    private static Integer b = 0;

    private static void total(Integer a, Integer b) {
        a = a + 1;
        b = b + 1;
    }

    public static void main(String[] args) {

        BigDecimal buyPrice = new BigDecimal(164.0001);
        BigDecimal sellPrice = new BigDecimal(162.0002);
        BigDecimal diff = sellPrice.subtract(buyPrice).divide(buyPrice, 4, RoundingMode.DOWN).multiply(new BigDecimal(100));
        System.out.println("abs " + diff.abs());

        System.out.println(diff.toPlainString());

    }


}
