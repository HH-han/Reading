package com.readingsystem.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户成长积分记录表
 */
@Data
@TableName("user_growth_log")
public class UserGrowthLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 变动类型 1打卡 2连续打卡奖励 3获赞 4作业完成 5管理员调整 */
    private Integer changeType;

    /** 积分变动值(正负) */
    private Integer points;

    private String remark;

    /** 关联业务ID */
    private Long bizId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
