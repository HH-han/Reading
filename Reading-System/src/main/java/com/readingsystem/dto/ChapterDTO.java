package com.readingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 书籍章节数据维护
 */
@Data
public class ChapterDTO {

    private Long id;

    @NotNull(message = "所属书籍不能为空")
    private Long bookId;

    /** 章节序号(为空自动追加) */
    private Integer chapterNo;

    @NotBlank(message = "章节标题不能为空")
    private String title;

    private String content;

    private String audioUrl;

    /** 音频时长(秒) */
    private Integer duration;
}
