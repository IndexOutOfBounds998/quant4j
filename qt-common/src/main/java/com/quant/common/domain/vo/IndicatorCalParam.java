package com.quant.common.domain.vo;

import com.quant.common.domain.to.BuyAndSellIndicatorTo;
import lombok.Data;

/**
 * Created by yang on 2019/5/26.
 */
@Data
public class IndicatorCalParam {
    private String indicatorName;
    private String[] params;
    private BuyAndSellIndicatorTo.SourceBean sourceBean;
}
