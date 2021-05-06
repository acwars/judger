package com.onlinejudge.judger.service;

import com.onlinejudge.judger.response.ServerResponse;

public interface CompetitionProblemService {

    Integer getScoreByCompIdProblemId(Integer compId,Integer problemId);

    ServerResponse addAcCountByCompIdProblemId(Integer compId,Integer problemId);

    ServerResponse addSubmitCountByCompIdProblemId(Integer compId,Integer problemId);

}
