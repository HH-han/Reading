package com.readingsystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职位（含使用统计）
 */
@Data
public class PositionVO {

    private Long id;

    private String name;

    private String description;

    private Integer sort;

    private Integer status;

    /** 使用该职位的用户数 */
    private Long userCount;

    /** 使用该职位的打卡规则数 */
    private Long ruleCount;

    private LocalDateTime createTime;
}
