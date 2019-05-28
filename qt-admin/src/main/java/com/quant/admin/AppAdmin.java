package com.quant.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableEurekaClient
@EnableScheduling
@ComponentScan({"com.quant.common", "com.quant.admin"})
public class AppAdmin {
    public static void main(String[] args) {
        SpringApplication.run(AppAdmin.class, args);
    }
}
