package com.quant.core.trading;

import com.quant.common.domain.response.TradeBean;
import lombok.Data;

import java.util.List;

/**
 * @author yang

 * @desc MarketOrder
 * @date 2019/7/9
 */
@Data
public class MarketOrder {

    List<TradeBean> buy;
    List<TradeBean> sell;

}
