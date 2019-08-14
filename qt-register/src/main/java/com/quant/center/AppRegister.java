package com.quant.center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class AppRegister {


    /**
     * 注册中心 可以移至 admin 为了方便管理 还是单独使用、
     * 注册中心只是为了注册节点 获取节点的ip 无其他意义
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(AppRegister.class, args);
    }

}
