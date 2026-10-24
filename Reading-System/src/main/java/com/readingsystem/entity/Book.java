package com.readingsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 书籍表
 */
@Data
@TableName("book")
public class Book implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String author;

    private String publisher;

    private String cover;

    /** 分类: 经营之道/人生哲学/领导力/自我精进等 */
    private String category;

    /** 类型 1电子书 2实体书 */
    private Integer bookType;

    private String description;

    private String recommendReason;

    private Integer chapterCount;

    /** 打卡人数 */
    private Integer checkinCount;

    /** 是否开放书库 0否 1是 */
    private Integer isPublic;

    /** 状态 0下架 1上架 */
    private Integer status;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
