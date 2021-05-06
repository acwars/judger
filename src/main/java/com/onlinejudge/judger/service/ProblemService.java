package com.onlinejudge.judger.service;

import com.onlinejudge.judger.common.JudgeStatusEnum;
import com.onlinejudge.judger.entity.Problem;
import com.onlinejudge.judger.entity.ProblemResult;
import com.onlinejudge.judger.entity.TestcaseResult;
import com.onlinejudge.judger.response.ServerResponse;

import java.util.List;

public interface ProblemService {

    Problem getProblemById(Integer problemId);

    ProblemResult getProblemResultById(Integer problemResultId);

    ProblemResult getProblemResultByRunNum(String runNum);

    ServerResponse insertProblemResult(ProblemResult result);

    ServerResponse updateProblemById(Problem problem);

    ServerResponse updateProblemResultStatusById(Integer problemResultId, Integer status);

    ServerResponse updateProblemResultById(ProblemResult problemResult);

    ServerResponse addProblemCountById(Integer problemId, JudgeStatusEnum statusConst);

    ServerResponse insertBatchTestcaseResult(List<TestcaseResult> testcaseResultList);

    int countProblemResult(Integer userId,Integer problemId,Integer status);

    ServerResponse addCompScoreById(Integer compScore,Integer problemResultId);

    Integer getTotalScoreById( Integer userId, Integer compId);
}
