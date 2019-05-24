package com.quant.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@MapperScan("com.quant.admin.dao")
@EnableEurekaClient
@ComponentScan({"com.quant.common", "com.quant.admin"})
@EnableScheduling
public class QtAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(QtAdminApplication.class, args);
    }


}
