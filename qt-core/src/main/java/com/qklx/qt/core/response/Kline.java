package com.qklx.qt.core.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 11:35
 */
@Data
public class Kline {


    private long id;
    private BigDecimal amount;
    private int count;
    private BigDecimal open;
    private BigDecimal close;
    private BigDecimal low;
    private BigDecimal high;
    private BigDecimal vol;

}
