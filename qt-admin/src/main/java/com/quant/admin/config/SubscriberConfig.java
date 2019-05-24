package com.quant.admin.config;

import com.quant.admin.component.OrderIdReceiver;
import com.quant.admin.component.ProfitReceiver;
import com.quant.admin.component.RobotMsgReceiver;
import com.quant.admin.service.IMailService;
import com.quant.common.config.RedisUtil;
import com.quant.common.config.VpnProxyConfig;
import com.quant.common.constans.RobotRedisKeyConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import javax.sound.midi.Receiver;

@Configuration
@AutoConfigureAfter({Receiver.class})
public class SubscriberConfig {

    @Autowired
    VpnProxyConfig vpnProxyConfig;
    @Autowired
    IMailService iMailService;
    @Autowired
    RedisUtil redisUtil;

    private SimpMessagingTemplate simpMessageSendingOperations;//消息发送模板

    @Autowired
    public void setSimpMessageSendingOperations(SimpMessagingTemplate simpMessageSendingOperations) {
        this.simpMessageSendingOperations = simpMessageSendingOperations;
    }

    @Bean
    public MessageListenerAdapter orderIdReceiverAdapter() {
        return new MessageListenerAdapter(new OrderIdReceiver(vpnProxyConfig, iMailService, redisUtil), "receiveMessage"); //当没有继承MessageListener时需要写方法名字
    }

    @Bean
    public MessageListenerAdapter robotMsgReceiverAdapter() {
        return new MessageListenerAdapter(new RobotMsgReceiver(simpMessageSendingOperations), "receiveMessage"); //当没有继承MessageListener时需要写方法名字
    }

    @Bean
    public MessageListenerAdapter profitMsgReceiverAdapter() {
        return new MessageListenerAdapter(new ProfitReceiver(), "receiveMessage"); //当没有继承MessageListener时需要写方法名字
    }

    /**
     * 创建消息监听容器
     *
     * @param redisConnectionFactory
     * @param
     * @return
     */
    @Bean
    public RedisMessageListenerContainer getRedisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory,
                                                                          MessageListenerAdapter orderIdReceiverAdapter,
                                                                          MessageListenerAdapter robotMsgReceiverAdapter,
                                                                          MessageListenerAdapter profitMsgReceiverAdapter) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory);
        redisMessageListenerContainer.addMessageListener(orderIdReceiverAdapter, new PatternTopic(RobotRedisKeyConfig.getQueue()));
        redisMessageListenerContainer.addMessageListener(robotMsgReceiverAdapter, new PatternTopic(RobotRedisKeyConfig.getRobot_msg_queue()));
        redisMessageListenerContainer.addMessageListener(profitMsgReceiverAdapter, new PatternTopic(RobotRedisKeyConfig.getOrder_profit()));

        return redisMessageListenerContainer;
    }


}
