package com.quant.admin.controller;

import com.quant.common.config.VpnProxyConfig;
import com.quant.common.enums.Status;
import com.quant.common.to.BuyAndSellIndicatorTo;
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
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
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

        Rule entry = null;


        int i = 0;
        for (BuyAndSellIndicatorTo.IndicatorBuyBean buyBean : to.getIndicatorBuy()) {
            //指标名称
            String NameIndicator = buyBean.getRuleFirst().getValue();
            String params = buyBean.getRuleFirst().getParams();


            String[] str = params.split(",");
            boolean price = NameIndicator.equals("price");
            boolean amount = NameIndicator.equals("amount");

            //获取价格的值
            String value = buyBean.getRuleSecond().getValue();
            //获取之间的关系
            String condition = buyBean.getCondition();
            //获取大小关系
            String compare = buyBean.getCompare().getValue();

            if (price) {
                //是价格指标
                ClosePriceIndicator priceIndicator = new ClosePriceIndicator(series);
                if (num_down.equals(compare)) {
                    //小于
                    UnderIndicatorRule rule = new UnderIndicatorRule(priceIndicator, new BigDecimal(value));
                    entry = andOr(entry, rule, i, condition);
                    i++;
                }

                if (num_up.equals(compare)) {
                    //大于
                    OverIndicatorRule rule = new OverIndicatorRule(priceIndicator, new BigDecimal(value));
                    entry = andOr(entry, rule, i, condition);
                    i++;

                }
            } else if (amount) {
                //是成交量指标
                VolumeIndicator volumeIndicator = new VolumeIndicator(series);
                if (num_down.equals(compare)) {
                    //小于
                    UnderIndicatorRule rule = new UnderIndicatorRule(volumeIndicator, new BigDecimal(value));
                    entry = andOr(entry, rule, i, condition);
                    i++;
                }

                if (num_up.equals(compare)) {
                    //大于
                    OverIndicatorRule rule = new OverIndicatorRule(volumeIndicator, new BigDecimal(value));
                    entry = andOr(entry, rule, i, condition);
                    i++;

                }
            } else {
                //是指标
                IndicatorCalParam param = new IndicatorCalParam();
                param.setIndicatorName(NameIndicator);
                param.setParams(str);
                Indicator indicator = factory.getIndicator(param);

                //获取第二个指标
                String NameIndicator2 = buyBean.getRuleSecond().getValue();
                String params2 = buyBean.getRuleSecond().getParams();

                if (buyBean.getRuleSecond().getParams() == null) {
                    //值

                    if (num_down.equals(compare)) {
                        //小于

                        UnderIndicatorRule underIndicatorRule = new UnderIndicatorRule(indicator, new BigDecimal(NameIndicator2));

                        if (i == 0) {
                            entry = underIndicatorRule;
                        } else {

                            if (condition.equals(and)) {
                                entry = entry.and(underIndicatorRule);
                            }
                            if (condition.equals(or)) {
                                entry = entry.or(underIndicatorRule);
                            }

                        }


                    }
                    if (num_up.equals(compare)) {
                        //小于

                        OverIndicatorRule overIndicatorRule = new OverIndicatorRule(indicator, new BigDecimal(NameIndicator2));

                        if (i == 0) {
                            entry = overIndicatorRule;
                        } else {

                            if (condition.equals(and)) {
                                entry = entry.and(overIndicatorRule);
                            }
                            if (condition.equals(or)) {
                                entry = entry.or(overIndicatorRule);
                            }

                        }


                    }

                } else {


                    IndicatorCalParam param2 = new IndicatorCalParam();
                    param2.setIndicatorName(NameIndicator2);
                    param2.setParams(params2.split(","));
                    Indicator indicator2 = factory.getIndicator(param2);


                    if (compare.equals(cross_up)) {
                        //交叉向上的趋势
                        CrossedUpIndicatorRule crossedUpIndicatorRule = new CrossedUpIndicatorRule(indicator, indicator2);
                        if (i == 0) {
                            entry = crossedUpIndicatorRule;
                            i++;
                            continue;
                        }
                        if (condition.equals(and)) {
                            entry = entry.and(crossedUpIndicatorRule);
                        }
                        if (condition.equals(or)) {
                            entry = entry.or(crossedUpIndicatorRule);
                        }

                    }

                    if (compare.equals(cross_down)) {
                        //交叉向下的趋势
                        CrossedDownIndicatorRule crossedDownIndicatorRule = new CrossedDownIndicatorRule(indicator, indicator2);
                        if (i == 0) {
                            entry = crossedDownIndicatorRule;
                            i++;
                            continue;
                        }
                        if (condition.equals(and)) {
                            entry = entry.and(crossedDownIndicatorRule);
                        }
                        if (condition.equals(or)) {
                            entry = entry.or(crossedDownIndicatorRule);
                        }

                    }
                }


            }

        }


        //构建卖出

        int j = 0;
        Rule exit = null;
        for (BuyAndSellIndicatorTo.IndicatorSellBean sellBean : to.getIndicatorSell()) {
            //指标名称
            String NameIndicator = sellBean.getRuleFirst().getValue();
            String params = sellBean.getRuleFirst().getParams();


            String[] str = params.split(",");
            boolean price = NameIndicator.equals("price");
            boolean amount = NameIndicator.equals("amount");

            //获取价格的值
            String value = sellBean.getRuleSecond().getValue();
            //获取之间的关系
            String condition = sellBean.getCondition();
            //获取大小关系
            String compare = sellBean.getCompare().getValue();

            if (price) {
                //是价格指标
                ClosePriceIndicator priceIndicator = new ClosePriceIndicator(series);
                if (num_down.equals(compare)) {
                    //小于
                    UnderIndicatorRule rule = new UnderIndicatorRule(priceIndicator, new BigDecimal(value));
                    exit = andOr(exit, rule, j, condition);
                    j++;
                }

                if (num_up.equals(compare)) {
                    //大于
                    OverIndicatorRule rule = new OverIndicatorRule(priceIndicator, new BigDecimal(value));
                    exit = andOr(exit, rule, j, condition);
                    j++;

                }
            } else if (amount) {
                //是成交量指标
                VolumeIndicator volumeIndicator = new VolumeIndicator(series);
                if (num_down.equals(compare)) {
                    //小于
                    UnderIndicatorRule rule = new UnderIndicatorRule(volumeIndicator, new BigDecimal(value));
                    exit = andOr(exit, rule, j, condition);
                    j++;
                }

                if (num_up.equals(compare)) {
                    //大于
                    OverIndicatorRule rule = new OverIndicatorRule(volumeIndicator, new BigDecimal(value));
                    exit = andOr(exit, rule, j, condition);
                    j++;

                }
            } else {
                //是指标
                IndicatorCalParam param = new IndicatorCalParam();
                param.setIndicatorName(NameIndicator);
                param.setParams(str);
                Indicator indicator = factory.getIndicator(param);
                String NameIndicator2 = sellBean.getRuleSecond().getValue();
                //获取第二个指标
                //判断第二个指标是值还是指标
                if (sellBean.getRuleSecond().getParams() == null) {
                    //值

                    if (num_down.equals(compare)) {
                        //小于

                        UnderIndicatorRule underIndicatorRule = new UnderIndicatorRule(indicator, new BigDecimal(NameIndicator2));

                        if (j == 0) {
                            exit = underIndicatorRule;
                        } else {

                            if (condition.equals(and)) {
                                exit = exit.and(underIndicatorRule);
                            }
                            if (condition.equals(or)) {
                                exit = exit.or(underIndicatorRule);
                            }

                        }


                    }
                    if (num_up.equals(compare)) {
                        //大于

                        OverIndicatorRule overIndicatorRule = new OverIndicatorRule(indicator, new BigDecimal(NameIndicator2));

                        if (j == 0) {
                            exit = overIndicatorRule;
                        } else {

                            if (condition.equals(and)) {
                                exit = exit.and(overIndicatorRule);
                            }
                            if (condition.equals(or)) {
                                exit = exit.or(overIndicatorRule);
                            }

                        }


                    }

                } else {

                    String params2 = sellBean.getRuleSecond().getParams();
                    IndicatorCalParam param2 = new IndicatorCalParam();
                    param2.setIndicatorName(NameIndicator2);
                    param2.setParams(params2.split(","));
                    Indicator indicator2 = factory.getIndicator(param2);


                    if (compare.equals(cross_up)) {
                        //交叉向上的趋势
                        CrossedUpIndicatorRule crossedUpIndicatorRule = new CrossedUpIndicatorRule(indicator, indicator2);
                        if (j == 0) {
                            entry = crossedUpIndicatorRule;
                            j++;
                            continue;
                        }
                        if (condition.equals(and)) {
                            exit = exit.and(crossedUpIndicatorRule);
                        }
                        if (condition.equals(or)) {
                            exit = exit.or(crossedUpIndicatorRule);
                        }

                    }

                    if (compare.equals(cross_down)) {
                        //交叉向下的趋势
                        CrossedDownIndicatorRule crossedDownIndicatorRule = new CrossedDownIndicatorRule(indicator, indicator2);
                        if (j == 0) {
                            exit = crossedDownIndicatorRule;
                            j++;
                            continue;
                        }
                        if (condition.equals(and)) {
                            exit = exit.and(crossedDownIndicatorRule);
                        }
                        if (condition.equals(or)) {
                            exit = exit.or(crossedDownIndicatorRule);
                        }

                    }
                }


            }

        }


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

        //==================================
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


    private Rule andOr(Rule entry, Rule rule, int i, String condition) {
        if (i == 0) {
            entry = rule;
            return entry;
        }
        if (condition.equals(and)) {
            entry = entry.and(rule);
        }
        if (condition.equals(or)) {
            entry = entry.or(rule);
        }
        return entry;
    }

}
