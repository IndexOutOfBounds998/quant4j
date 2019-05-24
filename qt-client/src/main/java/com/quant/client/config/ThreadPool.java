package com.quant.client.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ThreadPool {

    @Bean
    public ExecutorService initPool() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("-----机器人后台程序-----pool--%d").build();
        return new ThreadPoolExecutor(10,
                20,
                1000L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1000),
                namedThreadFactory,
                new ThreadPoolExecutor.AbortPolicy());
    }

}
