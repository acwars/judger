package com.onlinejudge.judger.runner;

import com.onlinejudge.judger.consumer.JudgeConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class JudgeRunner implements ApplicationRunner {

    @Autowired
    private JudgeConsumer judgeConsumer;

    @Override
    public void run(ApplicationArguments args){
        judgeConsumer.judge();
    }
}
