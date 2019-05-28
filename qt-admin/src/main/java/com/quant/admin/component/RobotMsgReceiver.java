package com.quant.admin.component;

import com.alibaba.fastjson.JSON;
import com.quant.common.domain.to.RobotRunMessage;
import com.quant.common.utils.JsonFormate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/***
 * 消息接收者（订阅者）  需要注入到springboot中
 */
@Slf4j
public class RobotMsgReceiver {

    private SimpMessagingTemplate simpMessageSendingOperations;//消息发送模板

    public RobotMsgReceiver(SimpMessagingTemplate simpMessageSendingOperations) {
        this.simpMessageSendingOperations = simpMessageSendingOperations;
    }

    /**
     * 收到通道的消息之后执行的方法
     *
     * @param message
     */
    public void receiveMessage(String message) {
        try {
            //这里是收到通道的消息之后执行的方法？？
            RobotRunMessage msg = JSON.parseObject(JsonFormate.parseJsonToString(message), RobotRunMessage.class);
            simpMessageSendingOperations.convertAndSend("/topic/" + msg.getRobotId(), JsonFormate.parseJsonToString(message));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
