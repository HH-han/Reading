package com.readingsystem.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 管理员打卡记录（含成员与书籍名称）
 */
@Data
public class AdminCheckinVO {

    private Long id;

    private Long userId;

    private String nickname;

    private Long circleId;

    private Long groupId;

    private Long bookId;

    private String bookTitle;

    private Long chapterId;

    private String chapterTitle;

    private Integer checkinType;

    private String content;

    private String mediaUrl;

    private String audioUrl;

    private Integer audioDuration;

    private LocalDate checkinDate;

    private Integer status;

    private Integer isPublic;

    private String auditRemark;

    private LocalDateTime createTime;
}
