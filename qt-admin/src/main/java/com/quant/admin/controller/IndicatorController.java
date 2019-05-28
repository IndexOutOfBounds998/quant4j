package com.quant.admin.controller;

import com.quant.common.config.VpnProxyConfig;
import com.quant.common.domain.response.Kline;
import com.quant.common.domain.response.KlineResponse;
import com.quant.common.domain.to.BuyAndSellIndicatorTo;
import com.quant.common.enums.Status;
import com.quant.common.utils.DateUtils;
import com.quant.core.api.ApiClient;
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
import org.ta4j.core.trading.rules.StopGainRule;
import org.ta4j.core.trading.rules.StopLossRule;

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
            entry = IndicatorHelper.simpleBuilder(buyBean, factory, series, entry, buyOnlyOneRule);
        }
        //构建卖出
        Rule exit = null;
        for (BuyAndSellIndicatorTo.IndicatorSellBean sellBean : to.getIndicatorSell()) {
            exit = IndicatorHelper.simpleBuilder(sellBean, factory, series, exit, sellOnlyOneRule);
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


}
