package com.quant.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by yang on 2019/5/16.
 */
@Data
@Component
@ConfigurationProperties(prefix = "vpnproxy")
public class VpnProxyConfig {

    private Boolean enable;

    private String ip;

    private int port;


}
