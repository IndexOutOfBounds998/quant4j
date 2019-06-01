package com.quant.admin.rest.impl;

import com.quant.admin.rest.RobotClientService;
import com.quant.common.domain.vo.IndicatorStrategyVo;
import com.quant.core.api.ApiResult;
import com.quant.common.domain.vo.RobotStrategyVo;
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

    @Override
    public ApiResult operatingIndicatorRobot(String url, IndicatorStrategyVo strategyVo) {
        return restTemplate.postForEntity(url, strategyVo, ApiResult.class).getBody();
    }


}
