package com.readingsystem.vo;

import lombok.Data;

/**
 * 今日打卡看板
 */
@Data
public class DashboardTodayVO {

    /** 今日已打卡人数 */
    private Long checkinUsers;

    /** 圈子总人数 */
    private Long totalMembers;

    /** 完成率(0-100) */
    private Double completeRate;

    /** 今日活跃小组数 */
    private Long activeGroups;

    /** 听录音人数 */
    private Long audioUsers;

    /** 诵读文章人数 */
    private Long readUsers;

    /** 写心得人数 */
    private Long noteUsers;
}
