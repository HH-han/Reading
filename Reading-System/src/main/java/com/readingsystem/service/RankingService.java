package com.readingsystem.service;

import com.readingsystem.vo.RankingVO;

import java.util.List;

public interface RankingService {

    /**
     * 排行榜
     *
     * @param rankType  1周 2月 3年
     * @param scopeType 1个人 2小组
     */
    List<RankingVO> rank(Integer rankType, Integer scopeType);
}
