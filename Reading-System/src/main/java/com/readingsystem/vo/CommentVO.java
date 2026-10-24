package com.readingsystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论
 */
@Data
public class CommentVO {

    private Long id;

    private Long checkinId;

    private Long userId;

    private String nickname;

    private String avatar;

    private Long parentId;

    private String content;

    private Integer likeCount;

    private LocalDateTime createTime;
}
