package com.readingsystem.vo;

import lombok.Data;

/**
 * 排行榜条目
 */
@Data
public class RankingVO {

    /** 排名 */
    private Integer rankNo;

    /** 用户ID或小组ID */
    private Long scopeId;

    /** 昵称或小组名 */
    private String name;

    private String avatar;

    /** 得分(打卡天数/次数) */
    private Double score;
}
