package com.onlinejudge.judger.service.impl;

import com.onlinejudge.judger.dao.CompetitionProblemMapper;
import com.onlinejudge.judger.response.ServerResponse;
import com.onlinejudge.judger.service.CompetitionProblemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompetitionProblemServiceImpl implements CompetitionProblemService {
    @Autowired
    private CompetitionProblemMapper competitionProblemMapper;

    @Override
    public Integer getScoreByCompIdProblemId(Integer compId, Integer problemId) {
        if(compId == null || problemId == null){
            return null;
        }
        return competitionProblemMapper.getScoreByCompIdProblemId(compId, problemId);
    }

    @Override
    public ServerResponse addAcCountByCompIdProblemId(Integer compId, Integer problemId) {
        if(compId == null || problemId == null){
            return ServerResponse.fail();
        }
        int effect = competitionProblemMapper.addAcCountByCompIdProblemId(compId, problemId);
        return effect > 0 ? ServerResponse.success() : ServerResponse.fail();
    }

    @Override
    public ServerResponse addSubmitCountByCompIdProblemId(Integer compId, Integer problemId) {
        if(compId == null || problemId == null){
            return ServerResponse.fail();
        }
        int effect = competitionProblemMapper.addSubmitCountByCompIdProblemId(compId, problemId);
        return effect > 0 ? ServerResponse.success() : ServerResponse.fail();
    }
}
