package com.readingsystem.enums;

import lombok.Getter;

/**
 * 角色枚举
 */
@Getter
public enum RoleEnum {

    SUPER_ADMIN("超级管理员"),
    TEAM_ADMIN("团队管理员"),
    MEMBER("普通队员");

    private final String desc;

    RoleEnum(String desc) {
        this.desc = desc;
    }
}
