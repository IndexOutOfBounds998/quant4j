import com.quant.core.Main;
import com.quant.common.response.Kline;
import com.quant.core.strategy.StrategyCalculation;
import com.quant.core.strategy.impl.RsiStrategyImpl;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.CCIIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.util.List;

/**
 * Created by yang on 2019/5/23.
 */
public class StrategyTest {

    public static void main(String[] args) {
        //get kine
        List<Kline> kline = Main.getKline("30min", "2000");
        //builder time series
        TimeSeries series = Main.loadTimeSeries(kline);
        //构建策略并执行
        Strategy strategy = buildStrategy(series);
        //回测历史数据

        TimeSeriesManager seriesManager = new TimeSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);

        // Analysis
        System.out.println("策略总收益: "
                + new TotalProfitCriterion().calculate(series, tradingRecord));

        //==================================
        System.out.println("策略总交易数: "
                + tradingRecord.getTradeCount());

        System.out.println("end================");
//        boolean shouldEnter = strategy.shouldEnter(series.getBarCount() - 1);
//        boolean shouldExit = strategy.shouldExit(series.getBarCount() - 1);
//        System.out.println("shouldEnter:" + shouldEnter);
//        System.out.println("shouldExit:" + shouldExit);

//        //run strategy for this
//        TimeSeriesManager seriesManager = new TimeSeriesManager(series);
//        TradingRecord tradingRecord = seriesManager.run(strategy);
//
//        // Analysis
//        System.out.println("Total profit for the strategy: "
//                + new TotalProfitCriterion().calculate(series, tradingRecord));
//
//        //==================================
//        System.out.println("Number of trades for the strategy: "
//                + tradingRecord.getTradeCount());
//
//        Num calculate = new BuyAndHoldCriterion().calculate(series, tradingRecord);
//        System.out.println("buy and hold " + calculate);
    }

    //构建一个简单的策略
    private static BaseStrategy buildSimpleStrategy(TimeSeries series) {

        //计算rsi
        StrategyCalculation rsiStrategyCal = new RsiStrategyImpl(series, 14);
        Indicator indicator = rsiStrategyCal.strategCalculation();
//        WilliamsRIndicator williamsRIndicator = new WilliamsRIndicator(series, 14);
        //rsi 指标值 低于30 执行买入
        Rule entry = new CrossedDownIndicatorRule(indicator, 30);
//        entry.and(new CrossedDownIndicatorRule(williamsRIndicator, indicator));
        //rsi 指标高于70 执行卖出
        Rule exit = new OverIndicatorRule(indicator, 70);

        //构建策略
        BaseStrategy strategy = new BaseStrategy(entry, exit);

        return strategy;
    }

    public static Strategy buildStrategy(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
        SMAIndicator longSma = new SMAIndicator(closePrice, 200);
        // We use a 2-period RSI indicator to identify buying
        // or selling opportunities within the bigger trend.
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);

        CCIIndicator cciIndicator = new CCIIndicator(series, 20);
        // Entry rule
        // The long-term trend is up when a security is above its 200-period SMA.
        Rule entryRule = new OverIndicatorRule(shortSma, longSma) // Trend
                .and(new CrossedDownIndicatorRule(rsi, 30)) // Signal 1
                .and(new OverIndicatorRule(shortSma, closePrice))
                .or(new CrossedDownIndicatorRule(cciIndicator, -100)); // Signal 2
        // Exit rule
        // The long-term trend is down when a security is below its 200-period SMA.
        Rule exitRule = new UnderIndicatorRule(shortSma, longSma) // Trend
                .and(new CrossedUpIndicatorRule(rsi, 70)) // Signal 1
                .and(new UnderIndicatorRule(shortSma, closePrice))
                .or(new UnderIndicatorRule(cciIndicator, 100)); // Signal 2
        return new BaseStrategy(entryRule, exitRule);


    }
}
