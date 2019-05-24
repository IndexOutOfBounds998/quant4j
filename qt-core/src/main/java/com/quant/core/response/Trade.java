package com.quant.core.response;

import lombok.Data;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 14:52
 */
@Data
public class Trade<T> {

    /**
     * id : 600848670
     * ts : 1489464451000
     * data : [{"id":600848670,"price":7962.62,"amount":0.0122,"direction":"buy","ts":1489464451000}]
     */

    private String id;
    private long ts;
    private T data;

}
