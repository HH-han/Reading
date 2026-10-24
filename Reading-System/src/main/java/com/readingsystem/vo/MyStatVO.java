package com.readingsystem.vo;

import lombok.Data;

import java.util.List;

/**
 * 我的统计
 */
@Data
public class MyStatVO {

    /** 我累计打卡天数 */
    private Integer checkinDays;

    /** 我在小组排名(周) */
    private Integer groupRank;

    /** 我在圈子排名(周) */
    private Integer circleRank;

    /** 本周前三名 */
    private List<RankingVO> topThree;
}
