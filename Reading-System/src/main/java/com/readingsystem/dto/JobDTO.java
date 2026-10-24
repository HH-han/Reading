package com.readingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学习作业
 */
@Data
public class JobDTO {

    private Long id;

    private Long circleId;

    /** 指定小组ID(为空全圈) */
    private Long groupId;

    @NotBlank(message = "作业标题不能为空")
    private String title;

    @NotNull(message = "指定书籍不能为空")
    private Long bookId;

    private Integer chapterStart;

    private Integer chapterEnd;

    private String content;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "截止日期不能为空")
    private LocalDate deadline;

    /** 状态 0草稿 1已发布 2已结束 */
    private Integer status = 1;
}
