package com.qklx.qt.admin.controller;

import com.alibaba.fastjson.JSON;
import com.qklx.qt.common.config.RedisUtil;
import com.qklx.qt.common.constans.RobotRedisKeyConfig;
import com.qklx.qt.core.to.RobotRunMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/test")
public class TestController extends AbstractController {

    @Autowired
    RedisUtil redisUtil;

    @GetMapping(value = "/test")
    public void info() {
        RobotRunMessage robotRunMessage = new RobotRunMessage();
        robotRunMessage.setMsg(" 更新机器人cccc状态成功");
        robotRunMessage.setRobotId(11);
        robotRunMessage.setUserId(1);
        redisUtil.convertAndSend(RobotRedisKeyConfig.getRobot_msg_queue(), JSON.toJSONString(robotRunMessage));
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return null;
    }
}
