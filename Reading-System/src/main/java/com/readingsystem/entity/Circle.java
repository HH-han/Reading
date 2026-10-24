package com.readingsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 圈子/部门表
 */
@Data
@TableName("circle")
public class Circle implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String cover;

    private String description;

    /** 圈主(管理员)ID */
    private Long ownerId;

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
