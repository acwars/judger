package com.onlinejudge.judger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class BeanConfig {


    @Bean(name = "judgeExecutor")
    public Executor getJudgeExecutor() {
        ExecutorService executorService =
                new ThreadPoolExecutor(32, 40, 10,
                        TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(400));
        return executorService;
    }


}
