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
 * 排行榜快照表
 */
@Data
@TableName("ranking_snapshot")
public class RankingSnapshot implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 排行类型 1周 2月 3年 */
    private Integer rankType;

    /** 范围类型 1个人 2小组 3圈子 */
    private Integer scopeType;

    /** 范围ID(个人用户ID/小组ID/圈子ID) */
    private Long scopeId;

    private Long circleId;

    private Long groupId;

    /** 周期标识 如 2026-W42 / 2026-10 / 2026 */
    private String periodKey;

    /** 得分(打卡天数等) */
    private Integer score;

    /** 排名 */
    private Integer rankNo;

    /** 统计基准日 */
    private LocalDate statDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
