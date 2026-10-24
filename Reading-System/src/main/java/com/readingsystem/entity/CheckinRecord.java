package com.readingsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡记录表
 */
@Data
@TableName("checkin_record")
public class CheckinRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 所属圈子ID(冗余) */
    private Long circleId;

    /** 所属小组ID(冗余) */
    private Long groupId;

    private Long bookId;

    private Long chapterId;

    /** 章节标题(冗余) */
    private String chapterTitle;

    /** 打卡类型 1图片 2朗读音频 3心得笔记 4学习录像 */
    private Integer checkinType;

    /** 打卡内容(心得文字) */
    private String content;

    /** 图片/视频URL(多图逗号分隔) */
    private String mediaUrl;

    private String audioUrl;

    /** 音频时长(秒) */
    private Integer audioDuration;

    /** 打卡日期(统计去重) */
    private LocalDate checkinDate;

    /** 状态 0待审核 1已通过 2已驳回 */
    private Integer status;

    /** 审核人ID */
    private Long auditUserId;

    private String auditRemark;

    private Integer likeCount;

    private Integer commentCount;

    private Integer viewCount;

    /** 是否公开到动态 0否 1是 */
    private Integer isPublic;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
