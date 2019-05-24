package com.quant.core.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 15:33
 */
@Data
public class Details {

    /**
     * amount : 4316.4346
     * open : 8090.54
     * close : 7962.62
     * high : 8119
     * ts : 1489464451000
     * id : 1489464451
     * count : 9595
     * low : 7875
     * vol : 3.449727690576E7
     */

    private BigDecimal amount;
    private BigDecimal open;
    private BigDecimal close;
    private BigDecimal high;
    private String ts;
    private String id;
    private String count;
    private BigDecimal low;
    private BigDecimal vol;

}
