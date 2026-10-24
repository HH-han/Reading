package com.readingsystem.vo;

import lombok.Data;

/**
 * 周/月/年打卡天数
 */
@Data
public class CheckinDaysVO {

    private Integer weekDays;

    private Integer monthDays;

    private Integer yearDays;

    /** 该职位要求的每周合格天数 */
    private Integer requiredDaysPerWeek;

    /** 该职位要求的每月合格天数 */
    private Integer requiredDaysPerMonth;

    /** 该职位要求的每年合格天数 */
    private Integer requiredDaysPerYear;

    /** 本周是否合格 */
    private Boolean weekQualified;
}
