package com.readingsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小组表
 */
@Data
@TableName("t_group")
public class TGroup implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long circleId;

    private String name;

    private String description;

    /** 组长ID */
    private Long leaderId;

    private Integer memberCount;

    private String inviteCode;

    /** 状态 0停用 1正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
