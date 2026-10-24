package com.readingsystem.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户每日打卡统计表(看板加速)
 */
@Data
@TableName("user_checkin_daily_stat")
public class UserCheckinDailyStat implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long circleId;

    private Long groupId;

    /** 统计日期 */
    private LocalDate statDate;

    /** 当日是否打卡 0否 1是 */
    private Integer hasCheckin;

    /** 听录音数(当日) */
    private Integer audioCount;

    /** 诵读文章数(当日) */
    private Integer readCount;

    /** 写心得数(当日) */
    private Integer noteCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
