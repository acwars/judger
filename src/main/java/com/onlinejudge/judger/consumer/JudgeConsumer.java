package com.onlinejudge.judger.consumer;

import com.onlinejudge.judger.common.JudgeStatusEnum;
import com.onlinejudge.judger.entity.ProblemResult;
import com.onlinejudge.judger.service.JudgeService;
import com.onlinejudge.judger.service.ProblemService;
import com.onlinejudge.judger.utils.JsonUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@Component
public class JudgeConsumer {
    private static Logger logger = LoggerFactory.getLogger(JudgeConsumer.class);
    @Autowired
    private JudgeService judgeService;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private Environment environment;

    private static DefaultMQPushConsumer pushConsumer;

    @PostConstruct
    private void initMQConsumer() throws MQClientException {
        pushConsumer = new DefaultMQPushConsumer(environment.getProperty("rocketmq.consumer.group"));
        pushConsumer.setNamesrvAddr(environment.getProperty("rocketmq.nameserver"));
        pushConsumer.setConsumeThreadMax(Integer.parseInt(environment.getProperty("rocketmq.consumer.max-thread")));
        pushConsumer.setConsumeThreadMin(Integer.parseInt(environment.getProperty("rocketmq.consumer.min-thread")));
        pushConsumer.setMessageModel(MessageModel.CLUSTERING);
        pushConsumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        pushConsumer.subscribe(environment.getProperty("rocketmq.topic"), "*");
    }

    @PreDestroy
    private void shutdownMQConsumer() {
        if (pushConsumer != null) {
            pushConsumer.shutdown();
            logger.info("{},??????????????????", Thread.currentThread().getName());
        }
    }


    public void judge() {
        pushConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                for (MessageExt messageExt : list) {
                    try {
                        String body = new String(messageExt.getBody(), RemotingHelper.DEFAULT_CHARSET);
                        logger.info("{},??????body???{}:", Thread.currentThread().getName(), body);
                        ProblemResult problemResult = JsonUtil.string2Obj(body, ProblemResult.class);

                        //????????????????????????????????????
                        ProblemResult problemResultFromDB = problemService.getProblemResultByRunNum(problemResult.getRunNum());
                        if (problemResultFromDB != null) {
                            if (problemResultFromDB.getStatus().equals(JudgeStatusEnum.QUEUING.getStatus())
                                    || problemResultFromDB.getStatus().equals(JudgeStatusEnum.COMPILING.getStatus())
                                    || problemResultFromDB.getStatus().equals(JudgeStatusEnum.JUDGING.getStatus())) {
                                //update compiling
                                problemResult = problemResultFromDB;
                                problemResult.setStatus(JudgeStatusEnum.COMPILING.getStatus());
                                problemService.updateProblemResultById(problemResult);
                            } else {
                                continue;
                            }
                        } else {
                            problemResult.setStatus(JudgeStatusEnum.COMPILING.getStatus());
                            problemService.insertProblemResult(problemResult);
                        }
                        //????????????
                        String userDirPath = judgeService.compile(problemResult);

                        if (userDirPath != null) {
                            //??????
                            judgeService.execute(problemResult, userDirPath);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error("RocketMQ????????????,{}", e.getMessage());
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        try {
            pushConsumer.start();
            logger.info("{},??????????????????", Thread.currentThread().getName());
        } catch (MQClientException e) {
            logger.error("?????????????????????,{}", e.getErrorMessage());
        }
    }

}
