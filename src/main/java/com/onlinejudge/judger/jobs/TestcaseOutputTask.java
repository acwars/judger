package com.onlinejudge.judger.jobs;

import com.onlinejudge.judger.common.JudgeStatusEnum;
import com.onlinejudge.judger.entity.TestcaseResult;
import com.onlinejudge.judger.utils.StreamUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * 程序输出
 */
public class TestcaseOutputTask implements Callable<TestcaseResult> {

    private Process process;

    private TestcaseResult testcaseResult;

    public TestcaseOutputTask(Process process, TestcaseResult testcaseResult) {
        this.process = process;
        this.testcaseResult = testcaseResult;
    }

    @Override
    public TestcaseResult call() throws Exception {
        FutureTask<Long> futureTask = new FutureTask<>(new TestcaseUsedMemoryTask(process));
        new Thread(futureTask).start();
        Instant startTime = Instant.now();
        //阻塞
        String output = StreamUtil.getOutPut(process.getInputStream());
        Instant endTime = Instant.now();

        testcaseResult.setTime(Duration.between(startTime, endTime).toMillis());
        testcaseResult.setUserOutput(output);

        //等待进程执行结束 0代表正常退出
        int exitValue = process.waitFor();
        Long usedMemory = futureTask.get();
        testcaseResult.setMemory(usedMemory);
        if (exitValue != 0 && testcaseResult.getStatus() == null) {
            testcaseResult.setStatus(JudgeStatusEnum.RUNTIME_ERROR.getStatus());
        }
        return testcaseResult;
    }

}


