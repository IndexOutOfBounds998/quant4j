package com.quant.common.domain.to;

/**
 * Created by yang on 2019/5/28.
 */
public interface IndicatorBean {

    String getCondition();

    BuyAndSellIndicatorTo.CompareBean getCompare();

    BuyAndSellIndicatorTo.RuleFirstBean getRuleFirst();

    BuyAndSellIndicatorTo.RuleSecondBean getRuleSecond();
}
