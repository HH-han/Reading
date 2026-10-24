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
 * 打卡评论表
 */
@Data
@TableName("checkin_comment")
public class CheckinComment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 打卡记录ID */
    private Long checkinId;

    /** 评论用户ID */
    private Long userId;

    /** 父评论ID(回复) */
    private Long parentId;

    private String content;

    private Integer likeCount;

    /** 0隐藏 1正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
