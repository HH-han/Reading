package com.readingsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 管理员代队员打卡
 */
@Data
public class AdminCheckinDTO {

    @NotNull(message = "队员不能为空")
    private Long userId;

    @NotNull(message = "书籍不能为空")
    private Long bookId;

    private Long chapterId;

    @NotNull(message = "打卡类型不能为空")
    private Integer checkinType;

    private String content;

    private String mediaUrl;

    private String audioUrl;

    private Integer audioDuration;

    /** 补卡日期(默认今天) */
    private LocalDate checkinDate;

    private String remark;
}
