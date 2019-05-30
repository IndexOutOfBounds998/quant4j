package com.quant.common.domain.to;

/**
 * Created by yang on 2019/5/28.
 */
public interface RuleBean {

    public String getName();

    public String getValue();

    public String getParams();

    BuyAndSellIndicatorTo.SourceBean getSource();
}
