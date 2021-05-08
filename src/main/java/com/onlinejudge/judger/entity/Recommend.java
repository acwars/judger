package com.onlinejudge.judger.entity;

import java.io.Serializable;
import java.util.Date;

public class Recommend implements Serializable {

    private static final long serialVersionUID = 2395647203811175860L;

    private Integer id;

    private Integer userId;

    private Integer problemId;

    private Integer problemRating;

    private Date createTime;

    public Recommend(Integer id, Integer userId, Integer problemId, Integer problemRating, Date createTime) {
        this.id = id;
        this.userId = userId;
        this.problemId = problemId;
        this.problemRating = problemRating;
        this.createTime = createTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProblemId() {
        return problemId;
    }

    public void setProblemId(Integer problemId) {
        this.problemId = problemId;
    }

    public Integer getProblemRating() {
        return problemRating;
    }

    public void setProblemRating(Integer problemRating) {
        this.problemRating = problemRating;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Recommend{" +
                "id=" + id +
                ", userId=" + userId +
                ", problemId=" + problemId +
                ", problemRating=" + problemRating +
                ", createTime=" + createTime +
                '}';
    }
}
