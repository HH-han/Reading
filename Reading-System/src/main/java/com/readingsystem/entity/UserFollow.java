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
 * 用户关注表
 */
@Data
@TableName("user_follow")
public class UserFollow implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关注者ID */
    private Long followerId;

    /** 被关注者ID */
    private Long followingId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
