package com.readingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 打卡模板
 */
@Data
public class TemplateDTO {

    private Long id;

    @NotNull(message = "圈子不能为空")
    private Long circleId;

    @NotBlank(message = "模板名称不能为空")
    private String name;

    /** 允许的打卡类型(逗号分隔) */
    private String checkinTypes = "1,2,3";

    private Integer requireAudio = 0;

    private Integer requireContent = 0;

    private Integer requireImage = 0;

    /** 是否必填书籍 0否 1是 */
    private Integer requireBook = 0;

    /** 是否必填章节标题 0否 1是 */
    private Integer requireChapter = 0;

    /** 各打卡类型必填项配置(JSON) */
    private String typeConfig;

    private Integer minWordCount = 0;

    private Integer status = 1;
}
