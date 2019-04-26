package com.qklx.qt.core.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 14:57
 */
@Data
public class TradeBean {

    /**
     * id : 600848670
     * price : 7962.62
     * amount : 0.0122
     * direction : buy
     * ts : 1489464451000
     */

    private String id;
    private BigDecimal price;
    private BigDecimal amount;
    private String direction;
    private long ts;


}
