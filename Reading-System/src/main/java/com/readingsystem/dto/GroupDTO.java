package com.readingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 读书圈小组
 */
@Data
public class GroupDTO {

    private Long id;

    @NotNull(message = "读书圈不能为空")
    private Long circleId;

    @NotBlank(message = "小组名称不能为空")
    private String name;

    private String description;

    private Integer status = 1;
}
