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
 * 消息通知表
 */
@Data
@TableName("notification")
public class Notification implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户ID */
    private Long userId;

    private String title;

    private String content;

    /** 类型 1系统 2打卡提醒 3作业通知 4审核结果 */
    private Integer notifyType;

    /** 关联业务ID */
    private Long bizId;

    /** 0未读 1已读 */
    private Integer readStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
