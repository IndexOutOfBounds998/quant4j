package com.qklx.qt.core.trading;

import com.qklx.qt.core.response.TradeBean;
import lombok.Data;

import java.util.List;

@Data
public class MarketOrder {

    List<TradeBean> buy;
    List<TradeBean> sell;

}
