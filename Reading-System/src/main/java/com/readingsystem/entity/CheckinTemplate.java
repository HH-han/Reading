package com.readingsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 打卡模板表
 */
@Data
@TableName("checkin_template")
public class CheckinTemplate implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long circleId;

    private String name;

    /** 允许的打卡类型(逗号分隔) */
    private String checkinTypes;

    /** 是否必填音频 0否 1是 */
    private Integer requireAudio;

    /** 是否必填心得 0否 1是 */
    private Integer requireContent;

    /** 是否必填图片 0否 1是 */
    private Integer requireImage;

    /** 是否必填书籍 0否 1是 */
    private Integer requireBook;

    /** 是否必填章节标题 0否 1是 */
    private Integer requireChapter;

    /**
     * 各打卡类型必填项配置(JSON)，结构如：
     * {"1":{"book":1,"chapter":1,"image":1,"audio":0,"note":0},"3":{"book":1,"chapter":1,"image":0,"audio":0,"note":1}}
     * 打卡页按此配置渲染卡片并校验必填
     */
    private String typeConfig;

    /** 心得最少字数 */
    private Integer minWordCount;

    /** 0停用 1启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
