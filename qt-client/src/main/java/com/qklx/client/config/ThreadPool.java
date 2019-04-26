package com.qklx.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ThreadPool {

    @Bean
    public ExecutorService initPool() {
        return Executors.newFixedThreadPool(100);
    }

}
