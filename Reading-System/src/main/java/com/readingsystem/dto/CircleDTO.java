package com.readingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 新建/编辑读书圈
 */
@Data
public class CircleDTO {

    private Long id;

    @NotBlank(message = "读书圈名称不能为空")
    private String name;

    private String description;

    private String cover;
}
