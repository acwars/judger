package com.onlinejudge.judger.service.impl;

import com.onlinejudge.judger.common.ExecConst;
import com.onlinejudge.judger.common.JudgeStatusEnum;
import com.onlinejudge.judger.common.LanguageEnum;
import com.onlinejudge.judger.entity.Problem;
import com.onlinejudge.judger.entity.ProblemResult;
import com.onlinejudge.judger.entity.TestcaseResult;
import com.onlinejudge.judger.jobs.TestcaseInputTask;
import com.onlinejudge.judger.service.*;
import com.onlinejudge.judger.utils.FileUtil;
import com.onlinejudge.judger.utils.StreamUtil;
import com.onlinejudge.judger.utils.StringUtil;
import com.onlinejudge.judger.utils.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class JudgeServiceImpl implements JudgeService {
    private static Logger logger = LoggerFactory.getLogger(JudgeServiceImpl.class);

    @Value("${file.server.testcase.dir}")
    private String fileServerTestcaseDir;
    // 执行进程
    private static Runtime runtime = Runtime.getRuntime();

    @Autowired
    private ProblemService problemService;

    @Autowired
    private UserService userService;

    @Autowired
    private RegisterService registerService;

    @Autowired
    private CompetitionProblemService competitionProblemService;


    //当一个用户提交同一个题目很多次答案时候，由于并发问题，可能在执行编译的时候，文件夹被另一个线程删除了，发出NotFoundException
    //执行程序同样存在这个问题
    //解决办法一：uuid文件，只删除文件，不删除文件夹
    //解决办法二：限制用户提交同一个题目５秒一次
    //解决办法三：文件夹＋时间戳，推荐 (已选择)

    @Override
    public String compile(ProblemResult problemResult) {
        // 题目测试点路径
        String problemDirPath = fileServerTestcaseDir + "/" + problemResult.getProblemId();
        // 用户提交代码路径
        String userDirPath = problemDirPath + "/" + UUIDUtil.createByTime();
        // 获取提交源代码语言
        LanguageEnum languageEnum = LanguageEnum.getEnumByType(problemResult.getType());
        // 获取相关源代码后缀名
        String ext = languageEnum.getExt();
        // 保存源代码
        FileUtil.saveFile(problemResult.getSourceCode(), userDirPath + "/Main." + ext);

        // 如果是比赛题目
        if (problemResult.getCompId() != null) {
            //add  submitCount
            registerService.addSubmitCountByCompIdUserId(problemResult.getCompId(), problemResult.getUserId());
            competitionProblemService.addSubmitCountByCompIdProblemId(problemResult.getCompId(), problemResult.getProblemId());
        }

        // 编译错误信息
        String compileErrorOutput = null;

        // 如果是需要编译的语言
        if (languageEnum.isRequiredCompile()) {
            try {
                // 单独打开一条线程执行指定的命令
                Process process = runtime.exec(ExecConst.compileExec(problemResult.getType(), userDirPath));
                // process.getErrorSteam()获取子进程的错误流,
                compileErrorOutput = StreamUtil.getOutPut(process.getErrorStream());

            } catch (IOException e) {
                e.printStackTrace();
                String message = "".equals(e.getMessage()) ? "IOException" : e.getMessage();
                logger.error(message);
                compileErrorOutput = message;
            }
        }

        // 如果编译成功
        if (compileErrorOutput == null || "".equals(compileErrorOutput)) {
            return userDirPath;
        } else {
            //update compile error
            compileErrorOutput = StringUtil.getLimitLengthByString(compileErrorOutput, 1000);
            // 状态设置为编译错误
            problemResult.setStatus(JudgeStatusEnum.COMPILE_ERROR.getStatus());
            // 获取编译错误信息
            problemResult.setErrorMsg(compileErrorOutput);
            // 更新测评结果
            problemService.updateProblemResultById(problemResult);

            //add count
            // 编译错误次数累计
            problemService.addProblemCountById(problemResult.getProblemId(), JudgeStatusEnum.COMPILE_ERROR);
            userService.addCount(problemResult.getUserId(), JudgeStatusEnum.COMPILE_ERROR);
            FileUtil.deleteFile(userDirPath);
            return null;
        }

    }

    @Override
    public void execute(ProblemResult problemResult, String userDirPath) {
        //update 判题中
        problemService.updateProblemResultStatusById(problemResult.getId(), JudgeStatusEnum.JUDGING.getStatus());

        // 题目测试点目录
        String problemDirPath = fileServerTestcaseDir + "/" + problemResult.getProblemId();
        // 输入文件目录
        String inputFileDirPath = problemDirPath + "/input";
        // 输出文件目录
        String outputFileDirPath = problemDirPath + "/output";
        // 获取题目信息
        Problem problem = problemService.getProblemById(problemResult.getProblemId());
        //AC题目增加的点数
        Integer ratingCount = problem.getRating() * 10;
        Integer goldCount = problem.getRating();

        try {
            // 执行输入和输出
            // 创建一个文件对象
            File inputFileDir = new File(inputFileDirPath);
            // 获取该目录下所有文件和目录的绝对路径
            File[] inputFiles = inputFileDir.listFiles();
            // 等待其他的线程都执行完任务，必要时可以对各个任务的执行结果进行汇总，然后主线程才继续往下执行。
            CountDownLatch countDownLatch = new CountDownLatch(inputFiles.length);
            // 创建固定大小的线程池。每次提交一个任务就创建一个线程，直到线程达到线程池的最大大小。
            // 线程池的大小一旦达到最大值就会保持不变，如果某个线程因为执行异常而结束，那么线程池会补充一个新线程。
            ExecutorService executorService = Executors.newFixedThreadPool(inputFiles.length);
            //
            for (File inputFile : inputFiles) {
                // 获取运行脚本实例进程
                ProcessBuilder builder = ExecConst.executeExec(problemResult.getType(), userDirPath);
                // 启动子进程
                Process process = builder.start();
                //
                TestcaseInputTask testcaseInputTask = new TestcaseInputTask(problem, inputFile,
                        outputFileDirPath, process, problemResult, countDownLatch);
                // 执行任务
                executorService.execute(testcaseInputTask);
            }

            // 最后关闭线程池，但执行以前提交的任务，不接受新任务
            executorService.shutdown();
            // 阻塞当前线程，直到所有测试点测试结束
            countDownLatch.await();
            //汇总统计
            long maxTime = -1;
            long maxMemory = -1;
            Integer status = null;
            Integer acCount = 0;
            //
            List<TestcaseResult> testcaseResultList = new ArrayList<>();
            //
            Set<Map.Entry<Integer, TestcaseResult>> entrySet = problemResult.getResultMap().entrySet();
            //
            for (Map.Entry<Integer, TestcaseResult> entry : entrySet) {
                //
                Integer testcaseNum = entry.getKey();
                //
                TestcaseResult testcaseResult = entry.getValue();
                //
                if (testcaseResult.getMemory() != null && testcaseResult.getMemory() > maxMemory) {
                    maxMemory = testcaseResult.getMemory();
                }
                if (testcaseResult.getTime() != null && testcaseResult.getTime() > maxTime) {
                    maxTime = testcaseResult.getTime();
                }
                if (JudgeStatusEnum.RUNTIME_ERROR.getStatus().equals(testcaseResult.getStatus())) {
                    status = JudgeStatusEnum.RUNTIME_ERROR.getStatus();
                }
                if (status == null && JudgeStatusEnum.PRESENTATION_ERROR.getStatus().equals(testcaseResult.getStatus())) {
                    status = JudgeStatusEnum.PRESENTATION_ERROR.getStatus();
                }
                if (status == null && JudgeStatusEnum.WRONG_ANSWER.getStatus().equals(testcaseResult.getStatus())) {
                    status = JudgeStatusEnum.WRONG_ANSWER.getStatus();
                }
                if (status == null && JudgeStatusEnum.TIME_LIMIT_EXCEEDED.getStatus().equals(testcaseResult.getStatus())) {
                    status = JudgeStatusEnum.TIME_LIMIT_EXCEEDED.getStatus();
                }
                if (status == null && JudgeStatusEnum.MEMORY_LIMIT_EXCEEDED.getStatus().equals(testcaseResult.getStatus())) {
                    status = JudgeStatusEnum.MEMORY_LIMIT_EXCEEDED.getStatus();
                }
                if (JudgeStatusEnum.ACCEPTED.getStatus().equals(testcaseResult.getStatus())) {
                    acCount++;
                }
                //
                testcaseResultList.add(testcaseResult);
            }

            // ac condition
            if (acCount == testcaseResultList.size()) {
                status = JudgeStatusEnum.ACCEPTED.getStatus();
                //user solutionCount
                userService.addSolutionCountAndGoldCountAndRating(problemResult.getUserId(),
                        problemResult.getProblemId(), goldCount, ratingCount);
                // competitionProblem register
                if (problemResult.getCompId() != null) {
                    competitionProblemService.addAcCountByCompIdProblemId(problemResult.getCompId(), problemResult.getProblemId());
                    //register add count
                    registerService.addAcCountByCompIdUserId(problemResult.getCompId(), problemResult.getUserId());
                    registerService.addSolutionCountByProblemIdCompIdUserId(problemResult.getProblemId(), problemResult.getCompId(), problemResult.getUserId());
                }
            }


            //record competition score
            if (problemResult.getCompId() != null) {
                Integer score = competitionProblemService.getScoreByCompIdProblemId(problemResult.getCompId(), problemResult.getProblemId());
                if (score != null) {
                    score = (int) ((acCount * 1.0 / testcaseResultList.size()) * score);
                    problemService.addCompScoreById(score, problemResult.getId());

                    //更新总分
                    Integer totalScore = problemService.getTotalScoreById(problemResult.getUserId(), problemResult.getCompId());
                    registerService.updateScore(totalScore, problemResult.getCompId(), problemResult.getUserId());
                }
            }

            //insertBatch testcase
            problemService.insertBatchTestcaseResult(testcaseResultList);
            //update problemResult
            problemResult.setMemory(maxMemory);
            problemResult.setTime(maxTime);
            problemResult.setStatus(status);
            problemService.updateProblemResultById(problemResult);

            //add count
            problemService.addProblemCountById(problemResult.getProblemId(), JudgeStatusEnum.getStatusConst(status));
            userService.addCount(problemResult.getUserId(), JudgeStatusEnum.getStatusConst(status));


        } catch (Exception e) {
            //执行脚本错误或没有测试用例或闭锁中断Exception (update database
            String message = StringUtil.getLimitLengthByString(e.getMessage(), 1000);
            problemResult.setErrorMsg(message);
            problemService.updateProblemResultStatusById(problemResult.getId(), JudgeStatusEnum.RUNTIME_ERROR.getStatus());

            logger.error("执行脚本错误或闭锁中断Exception", e);
        } finally {
            FileUtil.deleteFile(userDirPath);
        }

    }


}
