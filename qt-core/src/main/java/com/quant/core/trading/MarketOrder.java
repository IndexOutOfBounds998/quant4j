package com.quant.core.trading;

import com.quant.core.response.TradeBean;
import lombok.Data;

import java.util.List;

@Data
public class MarketOrder {

    List<TradeBean> buy;
    List<TradeBean> sell;

}
