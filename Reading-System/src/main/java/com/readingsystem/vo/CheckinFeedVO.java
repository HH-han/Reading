package com.readingsystem.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡动态卡片
 */
@Data
public class CheckinFeedVO {

    private Long id;

    private Long userId;

    private String nickname;

    private String avatar;

    private Integer checkinDays;

    private Long bookId;

    private String bookTitle;

    private String chapterTitle;

    private Integer checkinType;

    private String content;

    private String mediaUrl;

    private String audioUrl;

    private Integer audioDuration;

    private Integer likeCount;

    private Integer commentCount;

    private Integer viewCount;

    private LocalDate checkinDate;

    private LocalDateTime createTime;
}
