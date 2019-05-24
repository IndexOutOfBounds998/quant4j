package com.quant.admin.controller;

import com.quant.common.config.VpnProxyConfig;
import com.quant.common.utils.IndicatorHelper;
import com.quant.core.api.ApiClient;
import com.quant.common.response.Kline;
import com.quant.common.response.KlineResponse;
import com.quant.common.to.SimpleIndicatorVo;
import com.quant.core.strategy.impl.RsiStrategyImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yang on 2019/5/24.
 */
@RestController
@RequestMapping("/indicator")
public class IndicatorController extends BaseController {

    @Autowired
    VpnProxyConfig config;

    /**
     * @author yang
     * @desc 回测
     * @date 2019/5/24
     */
    @PostMapping("/backTest")
    public void backTest(@RequestBody SimpleIndicatorVo vo) {
        ApiClient apiClient = new ApiClient(config);
        KlineResponse response = apiClient.kline(vo.getSymbol(), vo.getKline(), vo.getSize());
        List<Kline> data = (ArrayList<Kline>) response.data;

        TimeSeries series = IndicatorHelper.buildSeries(data);

        Indicator indicator = null;
        if (vo.getIndicatorBuy().getIndicator().contains("RSI")) {
            RsiStrategyImpl rsiStrategy = new RsiStrategyImpl(series, Integer.parseInt(vo.getIndicatorBuy().getCount()));
            indicator = rsiStrategy.strategCalculation();
        }
        Rule entry = new CrossedDownIndicatorRule(indicator, Double.parseDouble(vo.getIndicatorBuy().getValue()));
//        entry.and(new CrossedDownIndicatorRule(williamsRIndicator, indicator));
        //rsi 指标高于70 执行卖出
        Rule exit = new OverIndicatorRule(indicator, Double.parseDouble(vo.getIndicatorSell().getValue()));

        Strategy strategy = new BaseStrategy(entry, exit);

        TimeSeriesManager seriesManager = new TimeSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        // Analysis
        System.out.println("策略总收益: "
                + new TotalProfitCriterion().calculate(series, tradingRecord));

        //==================================
        System.out.println("策略总交易数: "
                + tradingRecord.getTradeCount());


    }


}
