package com.quant.common.to;

import lombok.Data;

/**
 * @author yang
 * @desc 机器人实时日志
 * @date 2019/5/26
 */
@Data
public class RobotRunMessage {

    int userId;
    int robotId;
    String msg;
    String date;

}
