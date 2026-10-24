package com.readingsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增打卡
 */
@Data
public class CheckinAddDTO {

    /** 书籍ID(是否必填由打卡模板决定) */
    private Long bookId;

    private Long chapterId;

    /** 章节标题(未按目录选择章节时直接填写) */
    private String chapterTitle;

    @NotNull(message = "打卡类型不能为空")
    private Integer checkinType;

    /** 心得文字 */
    private String content;

    /** 图片/视频URL(多图逗号分隔) */
    private String mediaUrl;

    /** 音频URL */
    private String audioUrl;

    /** 音频时长(秒) */
    private Integer audioDuration;

    /** 是否公开到动态 默认公开 */
    private Integer isPublic = 1;
}
