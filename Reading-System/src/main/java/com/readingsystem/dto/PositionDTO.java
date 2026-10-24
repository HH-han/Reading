package com.readingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 职位维护
 */
@Data
public class PositionDTO {

    private Long id;

    @NotBlank(message = "职位名称不能为空")
    private String name;

    private String description;

    private Integer sort;

    /** 状态 0停用 1启用 */
    private Integer status;
}
