package com.readingsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 打卡记录数据维护
 */
@Data
public class CheckinEditDTO {

    @NotNull(message = "记录ID不能为空")
    private Long id;

    @NotNull(message = "打卡成员不能为空")
    private Long userId;

    private Long bookId;

    private Long chapterId;

    private String chapterTitle;

    /** 打卡类型 1图片 2朗读音频 3心得笔记 4学习录像 */
    private Integer checkinType;

    private String content;

    private String mediaUrl;

    private String audioUrl;

    private Integer audioDuration;

    private LocalDate checkinDate;

    /** 状态 0待审核 1已通过 2已驳回 */
    private Integer status;

    /** 是否公开到动态 0否 1是 */
    private Integer isPublic;

    private String auditRemark;
}
