package com.qklx.qt.core.request;

/**
 * @Author ISME
 * @Date 2018/1/14
 * @Time 19:08
 */

public class IntrustOrdersDetailRequest {

    public static interface OrderType {
        /**
         * 限价买入
         */
        static final String BUY_LIMIT = "buy-limit";
        /**
         * 限价卖出
         */
        static final String SELL_LIMIT = "sell-limit";
        /**
         * 市价买入
         */
        static final String BUY_MARKET = "buy-market";
        /**
         * 市价卖出
         */
        static final String SELL_MARKET = "sell-market";
    }

    public static interface OrderStates {
        /**
         * pre-submitted 准备提交
         */
        static final String PRE_SUBMITTED = "pre-submitted";
        /**
         * submitted 已提交
         */
        static final String SUBMITTED = "submitted";
        /**
         * partial-filled 部分成交
         */
        static final String PARTIAL_FILLED = "partial-filled";
        /**
         * partial-canceled 部分成交撤销
         */
        static final String PARTIAL_CANCELED = "partial-canceled";

        /**
         * filled 完全成交
         */
        static final String FILLED = "filled";
        /**
         * canceled 已撤销
         */
        static final String CANCELED = "canceled";
    }

    public String symbol;       //true	string	交易对		btcusdt, bccbtc, rcneth ...
    public String types;       //false	string	查询的订单类型组合，使用','分割		buy-market：市价买, sell-market：市价卖, buy-limit：限价买, sell-limit：限价卖
    public String startDate;   //false	string	查询开始日期, 日期格式yyyy-mm-dd
    public String endDate;       //false	string	查询结束日期, 日期格式yyyy-mm-dd
    public String states;       //true	string	查询的订单状态组合，使用','分割		pre-submitted 准备提交, submitted 已提交, partial-filled 部分成交,
    // partial-canceled 部分成交撤销, filled 完全成交, canceled 已撤销
    public String from;           //false	string	查询起始 ID
    public String direct;       //false	string	查询方向		prev 向前，next 向后
    public String size;           //false	string	查询记录大小
}
