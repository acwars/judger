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
        // FutureTask可以执行异步计算，可以查看异步程序是否执行完毕，并且可以开始和取消程序，并取得程序最终的执行结果。
        FutureTask<Long> futureTask = new FutureTask<>(new TestcaseUsedMemoryTask(process));
        new Thread(futureTask).start();
        // 来统计一个方法执行了多长时间
        Instant startTime = Instant.now();
        // 阻塞
        String output = StreamUtil.getOutPut(process.getInputStream());
        Instant endTime = Instant.now();

        // 来统计一个方法执行了多长时间
        testcaseResult.setTime(Duration.between(startTime, endTime).toMillis());
        testcaseResult.setUserOutput(output);

        // 等待进程执行结束 0代表正常退出
        int exitValue = process.waitFor();
        // 获取代码使用内存
        Long usedMemory = futureTask.get();
        testcaseResult.setMemory(usedMemory);
        if (exitValue != 0 && testcaseResult.getStatus() == null) {
            testcaseResult.setStatus(JudgeStatusEnum.RUNTIME_ERROR.getStatus());
        }
        return testcaseResult;
    }

}


