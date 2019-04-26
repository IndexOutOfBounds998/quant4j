package com.qklx.qt.admin.rest.impl;

import com.qklx.qt.admin.rest.RobotClientService;
import com.qklx.qt.core.api.ApiResult;
import com.qklx.qt.core.vo.RobotStrategyVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 调用机器人节点服务器 传递数据给机器人
 */
@Service
public class RobotClientServiceImpl implements RobotClientService {

    @Autowired
    RestTemplate restTemplate;

    /**
     * 发送信息到client端
     *
     * @param url
     * @param vo
     * @return
     */
    @Override
    public ApiResult operatingRobot(String url, RobotStrategyVo vo) {
        return restTemplate.postForEntity(url, vo, ApiResult.class).getBody();
    }


}
