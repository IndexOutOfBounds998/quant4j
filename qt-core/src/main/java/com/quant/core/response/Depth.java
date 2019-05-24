package com.quant.core.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 14:39
 */

public class Depth {

    /**
     * id : 1489464585407
     * ts : 1489464585407
     * bids : [[7964,0.0678],[7963,0.9162]]
     * asks : [[7979,0.0736],[8020,13.6584]]
     */

    private String id;
    private String ts;
    private List<List<BigDecimal>> bids;
    private List<List<BigDecimal>> asks;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTs() {
        return ts;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public List<List<BigDecimal>> getBids() {
        return bids;
    }

    public void setBids(List<List<BigDecimal>> bids) {
        this.bids = bids;
    }

    public List<List<BigDecimal>> getAsks() {
        return asks;
    }

    public void setAsks(List<List<BigDecimal>> asks) {
        this.asks = asks;
    }
}
