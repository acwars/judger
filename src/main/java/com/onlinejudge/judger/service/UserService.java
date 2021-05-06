package com.onlinejudge.judger.service;

import com.onlinejudge.judger.common.JudgeStatusEnum;
import com.onlinejudge.judger.response.ServerResponse;

public interface UserService {

    ServerResponse addCount(Integer userId, JudgeStatusEnum statusConst);

    ServerResponse addSolutionCountAndGoldCountAndRating(Integer userId,Integer problemId,Integer goldCount,Integer ratingCount);

}
