package com.readingsystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏条目
 */
@Data
public class FavoriteVO {

    private Long id;

    /** 目标类型 1书籍 2打卡记录 */
    private Integer targetType;

    private Long targetId;

    /** 书名或打卡摘要 */
    private String title;

    /** 封面或首图 */
    private String cover;

    private LocalDateTime createTime;
}
