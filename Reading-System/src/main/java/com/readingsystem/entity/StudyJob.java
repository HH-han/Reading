package com.readingsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习作业表
 */
@Data
@TableName("study_job")
public class StudyJob implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long circleId;

    /** 指定小组ID(为空则全圈) */
    private Long groupId;

    private String title;

    private Long bookId;

    /** 起始章节序号 */
    private Integer chapterStart;

    /** 结束章节序号 */
    private Integer chapterEnd;

    /** 作业要求说明 */
    private String content;

    private LocalDate startDate;

    private LocalDate deadline;

    /** 发布人ID */
    private Long publisherId;

    /** 状态 0草稿 1已发布 2已结束 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
