package com.quant.common.enums;

/**
 * 机器人启动状态
 */
public enum RobotState {

    start(1),
    stop(0);


    int str;

    RobotState(int str) {
        this.str = str;
    }

    public int getStr() {
        return str;
    }
}
