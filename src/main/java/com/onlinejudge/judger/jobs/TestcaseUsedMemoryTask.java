package com.onlinejudge.judger.jobs;

import com.onlinejudge.judger.utils.StreamUtil;

import java.util.concurrent.Callable;

public class TestcaseUsedMemoryTask implements Callable<Long> {
    private Process process;

    public TestcaseUsedMemoryTask(Process process) {
        this.process = process;
    }


    @Override
    public Long call() throws Exception {
        //tr其实是translate的缩写，主要用于删除文件中的控制字符，或进行字符转换
        //-d表示删除，[0-9]表示所有数字，-c表示对条件取反
        String cmd = "cat   /proc/" + process.pid() + "/status | grep VmRSS | tr -cd '[0-9]' ";
        Long max = 0L;
        while (process.isAlive()) {
            try {
                Process countUsedMemoryProcess = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", cmd});
                String memory = StreamUtil.getOutPut(countUsedMemoryProcess.getInputStream());
                long aLong = 0;
                try{
                    aLong = Long.parseLong(memory);
                }catch (Exception e){
                    continue;
                }
                if (aLong > max) {
                    max = aLong;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return max;
    }
}
