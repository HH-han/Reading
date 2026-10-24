package com.readingsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 打卡规则表(按职位配置)
 */
@Data
@TableName("checkin_rule")
public class CheckinRule implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long circleId;

    /** 职位名称 */
    private String position;

    /** 每周合格打卡天数 */
    private Integer requiredDaysPerWeek;

    /** 每月合格打卡天数 */
    private Integer requiredDaysPerMonth;

    /** 每年合格打卡天数 */
    private Integer requiredDaysPerYear;

    /** 状态 0停用 1启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
