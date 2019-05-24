package com.quant.common.domain;

import lombok.Data;

@Data
public class MineConfig {

    /**
     * 订单一段时间出现的买卖量
     */
    int buySize;
    int sellSize;

    /**
     * 他们出现后分别各自的权重
     */
    int buyWights;
    int sellWights;

    /**
     * 总的买入权重
     */
    int allBuyWights;

    /**
     * 总的卖出权重
     */
    int allSellWights;
}
