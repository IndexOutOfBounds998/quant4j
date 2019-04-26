/*
package com.qklx.qt.core.strategy.impl;

import MarketConfig;
import StrategyConfig;
import Kline;
import StrategyException;
import TradingStrategy;
import TradingApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

*/
/**
 * ar 指标 AR = [N天所有（High-Open）的和/ N天所有（Open—Low）的和] * 100
 *//*

public class ArStrategyImpl implements TradingStrategy {

    private static Logger logger = LoggerFactory.getLogger(ArStrategyImpl.class);


    TradingApi tradingApi;
    MarketConfig market;
    StrategyConfig strategyConfig;

    @Override
    public void init(TradingApi tradingApi, MarketConfig market, StrategyConfig config) {
        this.tradingApi = tradingApi;
        this.market = market;
        this.strategyConfig = config;
    }

    */
/**
     * ar 指标 AR = [N天所有（High-Open）的和/ N天所有（Open—Low）的和] * 100
     *
     * @throws StrategyException
     *//*

    @Override
    public void execute() throws StrategyException {
        boolean buySgin = false;
        boolean sellSgin = false;
        double ar = getAr();
        while (true) {
            //获取k线 计算 ar
            System.out.println("Calculation res " + ar);
            if (ar < strategyConfig.getDataOverSell() && buySgin == false) {
                //"AR超过了超卖线，产生买入信号"
                logger.info("AR超过了超卖线，产生买入信号============");
                logger.info("买入============");
                buySgin = true;
                sellSgin = false;
            } else if (ar > strategyConfig.getDataOverBuy() && sellSgin == false) {
                //("AR超过了超买线，产生卖出信号")
                logger.info("AR超过了超买线，产生卖出信号============");
                logger.info("卖出============");
                sellSgin = true;
                buySgin = false;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }


    private double getAr() {

        List<Kline> klines = tradingApi.getKline(market);

        double highOpenDiff = 0;
        double openLowDiff = 0;
        for (int i = 0; i < klines.size(); i++) {
            highOpenDiff += (klines.get(i).getHigh()) - (klines.get(i).getOpen());
            openLowDiff += (klines.get(i).getOpen()) - (klines.get(i).getLow());
        }
        return (highOpenDiff / openLowDiff) * 100;

    }


}
*/
