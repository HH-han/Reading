package com.readingsystem.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户表
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 微信OpenID */
    private String openId;

    /** 微信UnionID */
    private String unionId;

    /** 登录账号 */
    private String username;

    /** 密码(BCrypt) */
    private String password;

    private String nickname;

    private String avatar;

    private String phone;

    /** 性别 0未知 1男 2女 */
    private Integer gender;

    /** 角色 SUPER_ADMIN/TEAM_ADMIN/MEMBER */
    private String role;

    /** 职位(匹配打卡规则) */
    private String position;

    private Long circleId;

    private Long groupId;

    /** 个人主页背景图 */
    private String backgroundImg;

    private Integer points;

    /** 累计打卡天数 */
    private Integer checkinDays;

    private Integer followerCount;

    private Integer followingCount;

    private Integer likeCount;

    private String address;

    /** 状态 0禁用 1正常 */
    private Integer status;

    private LocalDateTime lastLoginTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
