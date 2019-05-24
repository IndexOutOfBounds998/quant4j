package com.quant.core.trading;

import com.quant.common.response.TradeBean;
import lombok.Data;

import java.util.List;

@Data
public class MarketOrder {

    List<TradeBean> buy;
    List<TradeBean> sell;

}
