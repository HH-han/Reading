package com.readingsystem.dto;

import lombok.Data;

/**
 * 用户账号数据维护
 */
@Data
public class AdminUserDTO {

    private Long id;

    /** 登录账号 */
    private String username;

    /** 密码(新增时可选，默认使用系统初始密码) */
    private String password;

    private String nickname;

    private String phone;

    /** 性别 0未知 1男 2女 */
    private Integer gender;

    /** 角色 SUPER_ADMIN/TEAM_ADMIN/MEMBER */
    private String role;

    private String position;

    private Long circleId;

    private Long groupId;

    /** 状态 0禁用 1正常 */
    private Integer status;
}
