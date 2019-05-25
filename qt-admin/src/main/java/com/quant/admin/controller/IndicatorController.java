package com.quant.admin.controller;

import com.quant.common.config.VpnProxyConfig;
import com.quant.common.enums.Status;
import com.quant.common.utils.DateUtils;
import com.quant.common.utils.IndicatorHelper;
import com.quant.common.vo.IndicatorCalParam;
import com.quant.core.api.ApiClient;
import com.quant.common.response.Kline;
import com.quant.common.response.KlineResponse;
import com.quant.common.to.SimpleIndicatorVo;
import com.quant.core.api.ApiResult;
import com.quant.core.factory.StaticIndicatorFactory;
import com.quant.core.indicator.RsiIndicatorCat;
import org.bouncycastle.asn1.pkcs.IssuerAndSerialNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;

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

    /**
     * @author yang
     * @desc 回测
     * @date 2019/5/24
     */
    @PostMapping("/backTest")
    public ApiResult backTest(@RequestBody SimpleIndicatorVo vo) {
        ApiClient apiClient = new ApiClient(config);
        KlineResponse response = apiClient.kline(vo.getSymbol(), vo.getKline(), vo.getSize());
        List<Kline> data = (ArrayList<Kline>) response.data;

        TimeSeries series = IndicatorHelper.buildSeries(data);


        StaticIndicatorFactory factory = new StaticIndicatorFactory(series);


        int buyDay = Integer.parseInt(vo.getIndicatorBuy().getCount());
        IndicatorCalParam buy = new IndicatorCalParam();
        buy.setDay(buyDay);
        buy.setIndicatorName(vo.getIndicatorBuy().getIndicator());
        Indicator buyIndicator = factory.getIndicator(buy);


        int sellDay = Integer.parseInt(vo.getIndicatorSell().getCount());
        IndicatorCalParam sell = new IndicatorCalParam();
        sell.setDay(sellDay);
        sell.setIndicatorName(vo.getIndicatorSell().getIndicator());
        Indicator sellIndicator = factory.getIndicator(sell);

        Rule entry = new CrossedDownIndicatorRule(buyIndicator, Double.parseDouble(vo.getIndicatorBuy().getValue()));
//        entry.and(new CrossedDownIndicatorRule(williamsRIndicator, indicator));
        //rsi 指标高于70 执行卖出
        Rule exit = new OverIndicatorRule(sellIndicator, Double.parseDouble(vo.getIndicatorSell().getValue()));

        Strategy strategy = new BaseStrategy(entry, exit);

        TimeSeriesManager seriesManager = new TimeSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        // Analysis
        System.out.println("策略总收益: "
                + new TotalProfitCriterion().calculate(series, tradingRecord));

        //==================================
        System.out.println("策略总交易数: "
                + tradingRecord.getTradeCount());
        HashMap<String, Object> param = new HashMap<>(2);
        List<HashMap> backDataList = new ArrayList<>(data.size());
        List<HashMap> buyAndSell = new ArrayList<>(tradingRecord.getTradeCount() * 2);
        int i = 0;
        for (Kline k : data) {
            HashMap<Object, Object> date = new HashMap<>();
            HashMap<Object, Object> date2 = new HashMap<>();
            String s = DateUtils.formateDate(DateUtils.parseTimeMillisToDate(k.getId() * 1000), null);
            date.put("日期", s);
            date.put("价格", k.getClose());
            for (Trade t : tradingRecord.getTrades()) {
                if (t.getEntry().getIndex() == i) {
                    date2.put("name", "买入");
                    date2.put("xAxis", s);
                    buyAndSell.add(date2);
                    break;
                }
                if (t.getExit().getIndex() == i) {
                    date2.put("name", "卖出");
                    date2.put("xAxis", s);
                    buyAndSell.add(date2);
                    break;
                }
            }
            i++;
            backDataList.add(date);
        }
        param.put("data", backDataList);
        param.put("buyOrSell", buyAndSell);
        return new ApiResult(Status.SUCCESS, param);

    }


}
