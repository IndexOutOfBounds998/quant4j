package com.quant.core.indicator;

import com.quant.common.domain.to.IndicatorBean;
import com.quant.common.exception.IndicatorException;
import com.quant.core.factory.StaticIndicatorFactory;
import com.quant.common.domain.response.Kline;
import com.quant.common.domain.to.RuleBean;
import com.quant.common.domain.vo.IndicatorCalParam;
import org.ta4j.core.*;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 指标计算 帮助类
 * Created by yang on 2019/5/24.
 */
public class IndicatorHelper {
    private static final String and = "and"; //and
    private static final String or = "or"; //or

    private static final String num_down = "num_down"; //小于
    private static final String num_up = "num_up"; //大于

    private static final String cross_up = "cross_up"; //交叉向上
    private static final String cross_down = "cross_down"; //交叉乡下

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
            StaticIndicatorFactory factory = new StaticIndicatorFactory(timeSeries);
            String value = bean.getValue();
            String params = bean.getParams();
            String[] strings = params.split(",");
            //构建一个指标
            IndicatorCalParam indicatorCalParam = new IndicatorCalParam();
            indicatorCalParam.setIndicatorName(value);
            indicatorCalParam.setParams(strings);
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
    public static Rule simpleBuilder(IndicatorBean bean, StaticIndicatorFactory factory, TimeSeries series, Rule rule, ThreadLocal<Boolean> onlyOneRule) {

        //指标名称
        String NameIndicator = bean.getRuleFirst().getValue();
        boolean price = NameIndicator.equals("price");
        boolean amount = NameIndicator.equals("amount");
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
                                   StaticIndicatorFactory factory,
                                   String compare,
                                   String condition,
                                   String value,
                                   ThreadLocal<Boolean> ruleOnlyOne,
                                   IndicatorBean bean,
                                   TimeSeries timeSeries,
                                   Rule entry) {
        if (price) {
            //是价格指标
            Indicator priceIndicator = factory.getIndicator("price");
            if (num_down.equals(compare)) {
                //小于
                UnderIndicatorRule rule = new UnderIndicatorRule(priceIndicator, new BigDecimal(value));
                entry = andOr(entry, rule, ruleOnlyOne.get(), condition);
            }

            if (num_up.equals(compare)) {
                //大于
                OverIndicatorRule rule = new OverIndicatorRule(priceIndicator, new BigDecimal(value));
                entry = andOr(entry, rule, ruleOnlyOne.get(), condition);
            }
        } else if (amount) {
            //是成交量指标
            Indicator volumeIndicator = factory.getIndicator("amount");
            if (num_down.equals(compare)) {
                //小于
                UnderIndicatorRule rule = new UnderIndicatorRule(volumeIndicator, new BigDecimal(value));
                entry = andOr(entry, rule, ruleOnlyOne.get(), condition);
            }

            if (num_up.equals(compare)) {
                //大于
                OverIndicatorRule rule = new OverIndicatorRule(volumeIndicator, new BigDecimal(value));
                entry = andOr(entry, rule, ruleOnlyOne.get(), condition);

            }
        } else {
            //是指标
            Indicator indicatorFirst = IndicatorHelper.builderIndicator(bean.getRuleFirst(), timeSeries);

            //获取第二个指标的
            String NameIndicator2 = bean.getRuleSecond().getValue();
            String params2 = bean.getRuleSecond().getParams();

            if (params2 == null) {
                //值
                if (num_down.equals(compare)) {
                    //小于
                    UnderIndicatorRule underIndicatorRule = new UnderIndicatorRule(indicatorFirst, new BigDecimal(NameIndicator2));
                    entry = andOr(entry, underIndicatorRule, ruleOnlyOne.get(), condition);
                }
                if (num_up.equals(compare)) {
                    //小于
                    OverIndicatorRule overIndicatorRule = new OverIndicatorRule(indicatorFirst, new BigDecimal(NameIndicator2));
                    entry = andOr(entry, overIndicatorRule, ruleOnlyOne.get(), condition);

                }

            } else {
                Indicator indicatorSecond = IndicatorHelper.builderIndicator(bean.getRuleSecond(), timeSeries);
                if (compare.equals(cross_up)) {
                    //交叉向上的趋势
                    CrossedUpIndicatorRule crossedUpIndicatorRule = new CrossedUpIndicatorRule(indicatorFirst, indicatorSecond);
                    entry = andOr(entry, crossedUpIndicatorRule, ruleOnlyOne.get(), condition);

                }

                if (compare.equals(cross_down)) {
                    //交叉向下的趋势
                    CrossedDownIndicatorRule crossedDownIndicatorRule = new CrossedDownIndicatorRule(indicatorFirst, indicatorSecond);
                    entry = andOr(entry, crossedDownIndicatorRule, ruleOnlyOne.get(), condition);
                }
            }
        }
        ruleOnlyOne.set(false);
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
