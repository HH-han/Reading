package com.readingsystem.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 书籍章节表
 */
@Data
@TableName("book_chapter")
public class BookChapter implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long bookId;

    /** 章节序号 */
    private Integer chapterNo;

    private String title;

    private String content;

    private String audioUrl;

    /** 音频时长(秒) */
    private Integer duration;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
