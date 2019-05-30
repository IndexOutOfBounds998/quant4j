package com.quant.core.helpers;

import com.quant.common.domain.to.BuyAndSellIndicatorTo;
import com.quant.common.domain.to.IndicatorBean;
import com.quant.common.exception.IndicatorException;
import com.quant.core.factory.IndicatorFactory;
import com.quant.common.domain.response.Kline;
import com.quant.common.domain.to.RuleBean;
import com.quant.common.domain.vo.IndicatorCalParam;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.trading.rules.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static com.quant.common.constans.IndicatorCons.*;

/**
 * 指标计算 帮助类
 * Created by yang on 2019/5/24.
 */
public class IndicatorHelper {

    /**
     * 构建series
     *
     * @param lines
     * @return
     */
    public static TimeSeries buildSeries(List<Kline> lines) {

        TimeSeries series = new BaseTimeSeries();
        Collections.reverse(lines);

        for (Kline kline : lines) {
            ZonedDateTime time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(kline.getId() * 1000), ZoneId.systemDefault());
            // build a bar
            Bar bar = new BaseBar(time,
                    kline.getOpen(),
                    kline.getHigh(),
                    kline.getLow(),
                    kline.getClose(),
                    kline.getVol(),
                    series.function());
            series.addBar(bar);
        }
        return series;

    }

    /**
     * builder a Indicator
     *
     * @param bean
     * @return
     */
    public static Indicator builderIndicator(RuleBean bean, TimeSeries timeSeries) {
        try {
            IndicatorFactory factory = new IndicatorFactory(timeSeries);
            String value = bean.getValue();
            String params = bean.getParams();
            String[] strings = params.split(",");
            //构建一个指标
            IndicatorCalParam indicatorCalParam = new IndicatorCalParam();
            indicatorCalParam.setIndicatorName(value);
            indicatorCalParam.setParams(strings);
            indicatorCalParam.setSourceBean(bean.getSource());
            return factory.getIndicator(indicatorCalParam);
        } catch (Exception e) {
            throw new IndicatorException(e);
        }
    }

    /**
     * 构建 规则
     *
     * @param bean
     * @param factory
     * @param series
     * @param rule
     * @param onlyOneRule
     * @return
     */
    public static Rule simpleBuilder(IndicatorBean bean, IndicatorFactory factory, TimeSeries series, Rule rule, ThreadLocal<Boolean> onlyOneRule) {
        //指标名称
        String NameIndicator = bean.getRuleFirst().getValue();
        boolean price = NameIndicator.equals(PRICE);
        boolean amount = NameIndicator.equals(VOLUME);
        //获取价格的值
        String value = bean.getRuleSecond().getValue();
        //获取之间的关系
        String condition = bean.getCondition();
        //获取大小关系
        String compare = bean.getCompare().getValue();
        rule = builderRule(price, amount, factory, compare, condition, value, onlyOneRule, bean, series, rule);
        return rule;
    }

    /**
     * 构建rule
     *
     * @param price
     * @param amount
     * @param factory
     * @param compare
     * @param condition
     * @param value
     * @param ruleOnlyOne
     * @param bean
     * @param timeSeries
     * @param entry
     * @return
     */
    public static Rule builderRule(boolean price, boolean amount,
                                   IndicatorFactory factory,
                                   String compare,
                                   String condition,
                                   String value,
                                   ThreadLocal<Boolean> ruleOnlyOne,
                                   IndicatorBean bean,
                                   TimeSeries timeSeries,
                                   Rule entry) {
        if (price) {
            //是价格指标
            Indicator priceIndicator = factory.getIndicator(PRICE);
            entry = AddOrAnd(priceIndicator, entry, compare, ruleOnlyOne, condition, value);
        } else if (amount) {
            //是成交量指标
            Indicator volumeIndicator = factory.getIndicator(VOLUME);
            entry = AddOrAnd(volumeIndicator, entry, compare, ruleOnlyOne, condition, value);
        } else {
            //是指标
            Indicator indicatorFirst = IndicatorHelper.builderIndicator(bean.getRuleFirst(), timeSeries);

            //获取第二个指标的
            String value2 = bean.getRuleSecond().getValue();
            String params2 = bean.getRuleSecond().getParams();

            if (params2 == null) {
                entry = AddOrAnd(indicatorFirst, entry, compare, ruleOnlyOne, condition, value2);
            } else {
                Indicator indicatorSecond = IndicatorHelper.builderIndicator(bean.getRuleSecond(), timeSeries);
                if (compare.equals(indicator_cross_down)) {
                    //大于等于
                    CrossedDownIndicatorRule crossedUpIndicatorRule = new CrossedDownIndicatorRule(indicatorFirst, indicatorSecond);
                    entry = andOr(entry, crossedUpIndicatorRule, ruleOnlyOne.get(), condition);
                }
                if (compare.equals(indicator_cross_up)) {
                    //小于等于
                    CrossedUpIndicatorRule crossedDownIndicatorRule = new CrossedUpIndicatorRule(indicatorFirst, indicatorSecond);
                    entry = andOr(entry, crossedDownIndicatorRule, ruleOnlyOne.get(), condition);
                }
                if (compare.equals(indicator_under)) {
                    //小于的趋势
                    UnderIndicatorRule underIndicatorRule = new UnderIndicatorRule(indicatorFirst, indicatorSecond);
                    entry = andOr(entry, underIndicatorRule, ruleOnlyOne.get(), condition);
                }
                if (compare.equals(indicator_up)) {
                    //大于的趋势
                    OverIndicatorRule overIndicatorRule = new OverIndicatorRule(indicatorFirst, indicatorSecond);
                    entry = andOr(entry, overIndicatorRule, ruleOnlyOne.get(), condition);
                }
                if (compare.equals(indicator_equal)) {
                    //等于
                    IsEqualRule overIndicatorRule = new IsEqualRule(indicatorFirst, indicatorSecond);
                    entry = andOr(entry, overIndicatorRule, ruleOnlyOne.get(), condition);
                }
            }
        }
        ruleOnlyOne.set(false);
        return entry;
    }

    private static Rule AddOrAnd(Indicator volumeIndicator, Rule entry, String compare, ThreadLocal<Boolean> ruleOnlyOne, String condition, String value) {
        if (num_under.equals(compare)) {
            //小于
            UnderIndicatorRule rule = new UnderIndicatorRule(volumeIndicator, new BigDecimal(value));
            entry = andOr(entry, rule, ruleOnlyOne.get(), condition);
        }
        if (num_over.equals(compare)) {
            //大于
            OverIndicatorRule rule = new OverIndicatorRule(volumeIndicator, new BigDecimal(value));
            entry = andOr(entry, rule, ruleOnlyOne.get(), condition);
        }
        if (num_cross_down.equals(compare)) {
            //大于等于
            CrossedDownIndicatorRule rule = new CrossedDownIndicatorRule(volumeIndicator, new BigDecimal(value));
            entry = andOr(entry, rule, ruleOnlyOne.get(), condition);
        }
        if (num_cross_up.equals(compare)) {
            //小于等于
            CrossedUpIndicatorRule rule = new CrossedUpIndicatorRule(volumeIndicator, new BigDecimal(value));
            entry = andOr(entry, rule, ruleOnlyOne.get(), condition);
        }
        if (num_equal.equals(compare)) {
            //等于
            IsEqualRule rule = new IsEqualRule(volumeIndicator, new BigDecimal(value));
            entry = andOr(entry, rule, ruleOnlyOne.get(), condition);
        }
        return entry;
    }

    /**
     * 构建指标之间的关系
     *
     * @param entry
     * @param rule
     * @param firstRuleOnlyOne
     * @param condition
     * @return
     */
    public static Rule andOr(Rule entry, Rule rule, boolean firstRuleOnlyOne, String condition) {
        if (firstRuleOnlyOne) {
            entry = rule;
            return entry;
        } else {
            if (condition.equals(and)) {
                entry = entry.and(rule);
                return entry;
            }
            if (condition.equals(or)) {
                entry = entry.or(rule);
                return entry;
            }
        }
        return entry;
    }


}
