package com.quant.client;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableEurekaClient
@EnableScheduling
@ComponentScan({"com.qklx.qt.common", "com.qklx.client"})
public class AppClient {
    public static void main(String[] args) {

        SpringApplication.run(AppClient.class, args);
    }

}
