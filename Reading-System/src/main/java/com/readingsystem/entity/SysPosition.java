package com.readingsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职位字典表
 */
@Data
@TableName("sys_position")
public class SysPosition implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 职位名称(与用户、打卡规则中的职位匹配) */
    private String name;

    private String description;

    private Integer sort;

    /** 状态 0停用 1启用 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
