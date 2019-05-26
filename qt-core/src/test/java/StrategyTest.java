import com.quant.core.Main;
import com.quant.common.response.Kline;
import com.quant.core.indicator.IndicatorCat;
import com.quant.core.indicator.RsiIndicatorCat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        AnalysisCriterion criterion = new TotalProfitCriterion();
        criterion.calculate(series, tradingRecord); // Returns the result for strategy1

    }


    private static void addBuySellSignals(TimeSeries series, Strategy strategy, XYPlot plot) {
        // Running the strategy
        TimeSeriesManager seriesManager = new TimeSeriesManager(series);
        List<Trade> trades = seriesManager.run(strategy).getTrades();
        // Adding markers to plot
        for (Trade trade : trades) {
            // Buy signal
            double buySignalBarTime = new Minute(Date.from(series.getBar(trade.getEntry().getIndex()).getEndTime().toInstant())).getFirstMillisecond();
            Marker buyMarker = new ValueMarker(buySignalBarTime);
            buyMarker.setPaint(Color.GREEN);
            buyMarker.setLabel("B");
            plot.addDomainMarker(buyMarker);
            // Sell signal
            double sellSignalBarTime = new Minute(Date.from(series.getBar(trade.getExit().getIndex()).getEndTime().toInstant())).getFirstMillisecond();
            Marker sellMarker = new ValueMarker(sellSignalBarTime);
            sellMarker.setPaint(Color.RED);
            sellMarker.setLabel("S");
            plot.addDomainMarker(sellMarker);
        }
    }

    /**
     * Displays a chart in a frame.
     *
     * @param chart the chart to be displayed
     */
    private static void displayChart(JFreeChart chart) {
        // Chart panel
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new Dimension(1024, 400));
        // Application frame
        ApplicationFrame frame = new ApplicationFrame("Ta4j example - Buy and sell signals to chart");
        frame.setContentPane(panel);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }

    //构建一个简单的策略
    private static BaseStrategy buildSimpleStrategy(TimeSeries series) {

        //计算rsi
        IndicatorCat rsiStrategyCal = new RsiIndicatorCat(series, 14);
        org.ta4j.core.Indicator indicator = rsiStrategyCal.strategCalculation();
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

        MACDIndicator macdIndicator = new MACDIndicator(closePrice, 12, 26);
//        PreviousValueIndicator
//        StochasticOscillatorKIndicator  stochasticOscillatorKIndicator=new StochasticOscillatorKIndicator(series,14,3,3);
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
