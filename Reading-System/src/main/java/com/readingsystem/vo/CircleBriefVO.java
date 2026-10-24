package com.readingsystem.vo;

import lombok.Data;

/**
 * 可加入的读书圈简要信息
 */
@Data
public class CircleBriefVO {

    private Long id;

    private String name;

    private String cover;

    private String description;

    private Integer memberCount;

    /** 当前用户是否已在该圈 */
    private Boolean joined;

    /** 是否为当前正在使用的读书圈 */
    private Boolean current;
}
