package com.qklx.qt.common.constans;


public class RobotRedisKeyConfig {

    /**
     * 机器人是否启动的标志
     */
    private final static String robotIsStartStateKey = "robot_start_id_";

    /**
     * 机器人是否还在运行的标志
     *
     * @return
     */
    private final static String robotIsRunStateKey = "robot_run_id_";

    /**
     * 订单成功后的id存储在queue这个队列中
     */
    private final static String queue = "order_id";

    /**
     * 订单成功后的id存储在queue这个队列中
     */
    private final static String robot_msg_queue = "robot_msg";
    /**
     * 每个交易对对应的 和 quote 标志
     */
    private final static String symbol = "symbol_base_quote_price_amount_";

    public final static String ok = "ok";


    public static String getRobot_msg_queue() {
        return robot_msg_queue;
    }

    public static String getQueue() {
        return queue;
    }

    public static String getSymbol() {
        return symbol;
    }

    public static String getRobotIsStartStateKey() {
        return robotIsStartStateKey;
    }

    public static String getRobotIsRunStateKey() {
        return robotIsRunStateKey;
    }
}
