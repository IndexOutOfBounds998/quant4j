package com.quant.core;

import com.alibaba.fastjson.JSON;
import com.quant.common.config.VpnProxyConfig;
import com.quant.core.api.ApiClient;
import com.quant.core.request.DepthRequest;
import com.quant.core.response.Depth;
import com.quant.core.response.DepthResponse;
import com.quant.core.response.Kline;
import com.quant.core.response.KlineResponse;

import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.List;

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
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;

/**
 * Created by yang on 2019/5/20.
 */
public class Main {
    public static void main(String[] args) {
        testRsi();

    }

    public static final int PERIODS_AVERAGE = 14;

    public static TimeSeries buildSeries(TimeSeries series, List<Kline> lines) {
        // build a bar
        for (Kline kline : lines) {
            ZonedDateTime time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(kline.getId() * 1000), ZoneId.systemDefault());
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

    public static TimeSeries loadTimeSeries(List<Kline> lines) {

        TimeSeries series = new BaseTimeSeries();
        Collections.reverse(lines);
        // build the list of populated bars
        return buildSeries(series, lines);

    }


    public static void testRsi() {
        List<Kline> kline = getKline("15min","2000");
        TimeSeries series = loadTimeSeries(kline);

        // Running the strategy
        Strategy strategy = RSI2Strategy.buildStrategy(series, 14);
//        Strategy strategy2 = RSI2Strategy.buildStrategy(series, 5);
        TimeSeriesManager seriesManager = new TimeSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);
//        TradingRecord tradingRecord2 = seriesManager.run(strategy2);
        System.out.println("Number of trades for the strategy: "
                + tradingRecord.getTradeCount());

        // Analysis
        System.out.println("Total profit for the strategy: "
                + new TotalProfitCriterion().calculate(series, tradingRecord));


        //==================================
//        System.out.println("Number of trades for the strategy: "
//                + tradingRecord2.getTradeCount());

        // Analysis
//        System.out.println("Total profit for the strategy: "
//                + new TotalProfitCriterion().calculate(series, tradingRecord2));
//        //==============================
        AnalysisCriterion criterion = new TotalProfitCriterion();
        criterion.calculate(series, tradingRecord); // Returns the result for strategy1
//        criterion.calculate(series, tradingRecord2);

//        Strategy bestStrategy = criterion.chooseBest(seriesManager, Arrays.asList(strategy, strategy2));

//        System.out.println(bestStrategy);
        /*
          Building chart datasets
         */
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(buildChartTimeSeries(series, new ClosePriceIndicator(series), "Bitstamp Bitcoin (BTC)"));

        /*
          Creating the chart
         */
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "eth", // title
                "Date", // x-axis label
                "Price", // y-axis label
                dataset, // data
                true, // create legend?
                true, // generate tooltips?
                false // generate URLs?
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MM-dd HH:mm"));

        /*
          Running the strategy and adding the buy and sell signals to plot
         */
        addBuySellSignals(series, strategy, plot);

        /*
          Displaying the chart
         */
        displayChart(chart);

//        FileInputStream inputStream = new FileInputStream(new File())
//        final int TOTAL_PERIODS = kline.size();
//
//        double[] closePrice = new double[TOTAL_PERIODS];
//        double[] out = new double[TOTAL_PERIODS];
//        MInteger begin = new MInteger();
//        MInteger length = new MInteger();
//
//        for (int i = 0; i < closePrice.length; i++) {
//            closePrice[i] = kline.get(i).getClose();
//        }
//
//        Core core = new Core();
//        RetCode retCode = core.rsi(0, closePrice.length - 1, closePrice, PERIODS_AVERAGE, begin, length, out);
//
//
//        if (retCode == RetCode.Success) {
//            System.out.println("Output Start Period: " + begin.value);
//            System.out.println("Output End Period: " + (begin.value + length.value - 1));
//            for (double d : out) {
//
//
//                System.out.println("RSI:" + d);
//
//
//            }
//        } else {
//            System.out.println("Error");
//        }

    }

    public void getDeep() {
        long currentTimeMillis = System.currentTimeMillis();
        long min20 = currentTimeMillis + 20 * 60 * 1000;
        HashMap<String, Depth> depthHashMap = new HashMap<>();
        VpnProxyConfig vpnProxyConfig = new VpnProxyConfig();
        vpnProxyConfig.setEnable(true);
        vpnProxyConfig.setIp("127.0.0.1");
        vpnProxyConfig.setPort(1088);
        ApiClient apiClient = new ApiClient(vpnProxyConfig);
        DepthRequest depthRequest = new DepthRequest();
        depthRequest.setSymbol("ethusdt");
        depthRequest.setType("step1");
        while (System.currentTimeMillis() < min20) {
            DepthResponse depth = apiClient.depth(depthRequest);
            if (depth.getStatus().equals("ok")) {
                depthHashMap.put(depth.getTs(), depth.getTick());
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        File file = new File("d:\\dept_ethusdt.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 根据文件创建文件的输出流
        try (OutputStream os = new FileOutputStream(file)) {
            String str = JSON.toJSONString(depthHashMap);
            // 把内容转换成字节数组
            byte[] data = str.getBytes();
            // 向文件写入内容
            os.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Builds a JFreeChart time series from a Ta4j time series and an indicator.
     *
     * @param barseries the ta4j time series
     * @param indicator the indicator
     * @param name      the name of the chart time series
     * @return the JFreeChart time series
     */
    private static org.jfree.data.time.TimeSeries buildChartTimeSeries(TimeSeries barseries, Indicator<Num> indicator, String name) {
        org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(name);
        for (int i = 0; i < barseries.getBarCount(); i++) {
            Bar bar = barseries.getBar(i);
            chartTimeSeries.add(new Minute(Date.from(bar.getEndTime().toInstant())), indicator.getValue(i).doubleValue());
        }
        return chartTimeSeries;
    }

    /**
     * Runs a strategy over a time series and adds the value markers
     * corresponding to buy/sell signals to the plot.
     *
     * @param series   a time series
     * @param strategy a trading strategy
     * @param plot     the plot
     */
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

    public static List<Kline> getKline(String tyoe,String size) {
        VpnProxyConfig vpnProxyConfig = new VpnProxyConfig();
        vpnProxyConfig.setEnable(true);
        vpnProxyConfig.setIp("127.0.0.1");
        vpnProxyConfig.setPort(1088);
        ApiClient apiClient = new ApiClient(vpnProxyConfig);
        KlineResponse ethusdt = apiClient.kline("ethusdt", tyoe, size);
        List<Kline> data = (ArrayList<Kline>) ethusdt.data;
        return data;
    }


    /**
     * 2-Period RSI Strategy
     * </p>
     *
     * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2">
     * http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2</a>
     */
    public static class RSI2Strategy {

        /**
         * @param series a time series
         * @return a 2-period RSI strategy
         */
        public static Strategy buildStrategy(TimeSeries series, int barCount) {
            if (series == null) {
                throw new IllegalArgumentException("Series cannot be null");
            }

            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            // We use a 2-period RSI indicator to identify buying
            // or selling opportunities within the bigger trend.
            RSIIndicator rsi = new RSIIndicator(closePrice, barCount);

            // Entry rule
            // The long-term trend is up when a security is above its 200-period SMA.
            Rule entryRule = // Trend
                    new CrossedDownIndicatorRule(rsi, 30);// Signal 1; // Signal 2

            // Exit rule
            // The long-term trend is down when a security is below its 200-period SMA.
            Rule exitRule = new CrossedUpIndicatorRule(rsi, 70);

            // TODO: Finalize the strategy

            return new BaseStrategy(entryRule, exitRule);
        }


    }
}
