package com.readingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 打卡规则
 */
@Data
public class RuleDTO {

    private Long id;

    @NotNull(message = "圈子不能为空")
    private Long circleId;

    @NotBlank(message = "职位不能为空")
    private String position;

    @NotNull(message = "每周合格天数不能为空")
    private Integer requiredDaysPerWeek;

    private Integer requiredDaysPerMonth;

    private Integer requiredDaysPerYear;

    private Integer status = 1;
}
