import com.quant.common.utils.DateUtils;
import com.quant.core.Main;
import com.quant.common.domain.response.Kline;
import com.quant.core.indicatorAdapter.IndicatorAdapter;
import com.quant.core.indicatorAdapter.RsiIndicatorAdapter;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Minute;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.PrecisionNum;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.awt.*;
import java.security.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import static com.opslab.Opslab.DATE_FORMAT;

/**
 * Created by yang on 2019/5/23.
 */
public class StrategyTest {

    static SMAIndicator sma9;
    static SMAIndicator sma26;

    public static void main(String[] args) {
        //get kine
        List<Kline> kline = Main.getKline("1min", "2000");
        //builder time series
        TimeSeries series = Main.loadTimeSeries(kline);
        //构建策略并执行
        Strategy strategy = buildStrategy(series);

        //回测历史数据
        ZonedDateTime beginTime = series.getBar(series.getEndIndex()).getEndTime();
        while (true) {

            // New bar
            try {
                Thread.sleep(1000); // I know...
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Kline line = getLine();
            System.out.println("sm9 val= " + sma9.getValue(series.getEndIndex()));
            System.out.println("sm26 val= " + sma26.getValue(series.getEndIndex()));
            ZonedDateTime nowTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli((line.getId()) * 1000), ZoneId.systemDefault());
            Bar newBar = new BaseBar(nowTime, line.getOpen(), line.getHigh(), line.getLow(), line.getClose(), line.getVol(), series.function());
            if (nowTime.isAfter(beginTime)) {
                series.addBar(newBar);
                beginTime = nowTime;
            } else {
                series.addBar(newBar, true);
            }
            System.out.println("------------------------------------------------------\n"
                    + "Bar " + series.getEndIndex() + " added, close price = " + newBar.getClosePrice().doubleValue());
            int endIndex = series.getEndIndex();
            if (strategy.shouldEnter(endIndex)) {
                // Our strategy should enter
                System.out.println("Strategy should ENTER on " + endIndex);
            }
            if (strategy.shouldExit(endIndex)) {
                // Our strategy should exit
                System.out.println("Strategy should EXIT on " + endIndex);
            }
        }
    }


    public static Strategy buildStrategy(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        // The bias is bullish when the shorter-moving average moves above the longer moving average.
        // The bias is bearish when the shorter-moving average moves below the longer moving average.
        sma9 = new SMAIndicator(closePrice, 9);
        sma26 = new SMAIndicator(closePrice, 26);


        // Entry rule
        Rule entryRule = new CrossedUpIndicatorRule(sma9, sma26);// Trend


        // Exit rule
        Rule exitRule = new CrossedDownIndicatorRule(sma9, sma26);
        return new BaseStrategy(entryRule, exitRule);


    }

    public static Kline getLine() {
        List<Kline> kline = Main.getKline("1min", "10");
        Kline line = kline.get(0);
        return line;
    }
}
