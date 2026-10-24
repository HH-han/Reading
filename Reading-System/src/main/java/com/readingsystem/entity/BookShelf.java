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
 * 书架关联表
 */
@Data
@TableName("book_shelf")
public class BookShelf implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long bookId;

    /** 书架类型 1我的藏书 2小组合集 3精选集 */
    private Integer shelfType;

    /** 来源 1开放书库 2管理员推荐 */
    private Integer source;

    /** 阅读进度(章节序号) */
    private Integer progress;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
