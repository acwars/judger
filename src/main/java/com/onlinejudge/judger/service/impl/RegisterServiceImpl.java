package com.onlinejudge.judger.service.impl;

import com.onlinejudge.judger.dao.RegisterMapper;
import com.onlinejudge.judger.response.ServerResponse;
import com.onlinejudge.judger.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegisterServiceImpl implements RegisterService {

    @Autowired
    private RegisterMapper registerMapper;


    @Override
    public ServerResponse addSolutionCountByProblemIdCompIdUserId(Integer problemId,Integer compId, Integer userId) {
        int effect = registerMapper.addSolutionCountByProblemIdCompIdUserId(problemId,compId, userId);
        return effect > 0 ? ServerResponse.success() : ServerResponse.fail();
    }



    @Override
    public ServerResponse addSubmitCountByCompIdUserId(Integer compId, Integer userId) {
        int effect = registerMapper.addSubmitCountByCompIdUserId(compId, userId);
        return effect > 0 ? ServerResponse.success() : ServerResponse.fail();
    }

    @Override
    public ServerResponse addAcCountByCompIdUserId(Integer compId, Integer userId) {
        int effect = registerMapper.addAcCountByCompIdUserId(compId, userId);
        return effect > 0 ? ServerResponse.success() : ServerResponse.fail();
    }

    @Override
    public ServerResponse updateScore(Integer score, Integer compId, Integer userId) {
        int effect = registerMapper.updateScoreByCompIdUserId(score,compId,userId);
        return effect > 0 ? ServerResponse.success() : ServerResponse.fail();
    }


}
