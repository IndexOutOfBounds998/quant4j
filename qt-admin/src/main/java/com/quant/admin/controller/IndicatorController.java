package com.quant.admin.controller;

import com.quant.common.config.VpnProxyConfig;
import com.quant.common.enums.Status;
import com.quant.common.domain.to.BuyAndSellIndicatorTo;
import com.quant.common.domain.to.IndicatorBean;
import com.quant.common.utils.DateUtils;
import com.quant.core.api.ApiClient;
import com.quant.common.domain.response.Kline;
import com.quant.common.domain.response.KlineResponse;
import com.quant.core.api.ApiResult;
import com.quant.core.factory.StaticIndicatorFactory;
import com.quant.core.indicator.IndicatorHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yang on 2019/5/24.
 */
@RestController
@RequestMapping("/indicator")
public class IndicatorController extends BaseController {

    @Autowired
    VpnProxyConfig config;
    private static final String and = "and"; //and
    private static final String or = "or"; //or

    private static final String num_down = "num_down"; //小于
    private static final String num_up = "num_up"; //大于

    private static final String cross_up = "cross_up"; //交叉向上
    private static final String cross_down = "cross_down"; //交叉乡下


    private static final ThreadLocal<Boolean> buyOnlyOneRule = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> sellOnlyOneRule = new ThreadLocal<>();


    /**
     * @author yang
     * @desc 回测
     * @date 2019/5/24
     */
    @PostMapping("/backTest")
    public ApiResult backTest(@RequestBody BuyAndSellIndicatorTo to) {
        ApiClient apiClient = new ApiClient(config);
        KlineResponse response = apiClient.kline(to.getSymbol(), to.getKline(), to.getSize());
        List<Kline> data = (ArrayList<Kline>) response.data;
        TimeSeries series = IndicatorHelper.buildSeries(data);
        StaticIndicatorFactory factory = new StaticIndicatorFactory(series);
        //是否只有一条规则
        buyOnlyOneRule.set(true);
        sellOnlyOneRule.set(true);

        Rule entry = null;
        for (BuyAndSellIndicatorTo.IndicatorBuyBean buyBean : to.getIndicatorBuy()) {
            //指标名称
            entry = simpleBuilder(buyBean, factory, series, entry, buyOnlyOneRule);
        }
        //构建卖出
        Rule exit = null;
        for (BuyAndSellIndicatorTo.IndicatorSellBean sellBean : to.getIndicatorSell()) {
            exit = simpleBuilder(sellBean, factory, series, exit, sellOnlyOneRule);
        }
        if (entry == null || exit == null) {
            return new ApiResult(Status.ERROR, "策略构建失败！");
        }
        //构建止盈止损
        if (to.getStopGain() != null && to.getStopGain() != 0) {
            //给卖出条件增加止盈
            StopGainRule stopGainRule = new StopGainRule(new ClosePriceIndicator(series), to.getStopGain());
            exit = exit.and(stopGainRule);

        }
        if (to.getStopLoss() != null && to.getStopLoss() != 0) {
            //给卖出条件增加止损
            StopLossRule stopLossRule = new StopLossRule(new ClosePriceIndicator(series), to.getStopLoss());
            exit = exit.and(stopLossRule);

        }
        //构建策略
        Strategy strategy = new BaseStrategy(entry, exit);
        // 回测
        TimeSeriesManager seriesManager = new TimeSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);
        // Analysis
        System.out.println("策略总收益: "
                + new TotalProfitCriterion().calculate(series, tradingRecord));

        System.out.println("策略总交易数: "
                + tradingRecord.getTradeCount());
        HashMap<String, Object> param = new HashMap<>(2);
        List<HashMap> backDataList = new ArrayList<>(data.size());
        List<HashMap> buyAndSell = new ArrayList<>(tradingRecord.getTradeCount() * 2);
        int l = 0;
        for (Kline k : data) {
            HashMap<Object, Object> date = new HashMap<>();
            HashMap<Object, Object> date2 = new HashMap<>();
            String s = DateUtils.formateDate(DateUtils.parseTimeMillisToDate(k.getId() * 1000), null);
            date.put("日期", s);
            date.put("价格", k.getClose());
            for (Trade t : tradingRecord.getTrades()) {
                if (t.getEntry().getIndex() == l) {
                    date2.put("name", "买入");
                    date2.put("xAxis", s);
                    buyAndSell.add(date2);
                    break;
                }
                if (t.getExit().getIndex() == l) {
                    date2.put("name", "卖出");
                    date2.put("xAxis", s);
                    buyAndSell.add(date2);
                    break;
                }
            }
            l++;
            backDataList.add(date);
        }
        param.put("data", backDataList);
        param.put("buyOrSell", buyAndSell);
        return new ApiResult(Status.SUCCESS, param);

    }


    private static Rule simpleBuilder(IndicatorBean bean, StaticIndicatorFactory factory, TimeSeries series, Rule rule, ThreadLocal<Boolean> onlyOneRule) {

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


    private static Rule builderRule(boolean price, boolean amount,
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


    private static Rule andOr(Rule entry, Rule rule, boolean firstRuleOnlyOne, String condition) {
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
