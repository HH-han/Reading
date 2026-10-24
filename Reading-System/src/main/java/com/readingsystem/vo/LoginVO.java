package com.readingsystem.vo;

import lombok.Data;

/**
 * 登录返回
 */
@Data
public class LoginVO {

    private String token;

    private UserInfoVO user;
}
