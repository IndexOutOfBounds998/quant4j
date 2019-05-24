package com.quant.common.to;

import lombok.Data;

@Data
public class RobotRunMessage {

    int userId;
    int robotId;
    String msg;
    String date;

}
