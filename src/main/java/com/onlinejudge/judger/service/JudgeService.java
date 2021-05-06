package com.onlinejudge.judger.service;

import com.onlinejudge.judger.entity.ProblemResult;

public interface JudgeService {


    String compile(ProblemResult problemResult);

    void execute(ProblemResult problemResult,String userDirPath);

}
