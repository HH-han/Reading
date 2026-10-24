package com.readingsystem.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息(不含敏感字段)
 */
@Data
public class UserInfoVO {

    private Long id;

    private String openId;

    private String username;

    private String nickname;

    private String avatar;

    private String phone;

    private Integer gender;

    private String role;

    private String position;

    private Long circleId;

    private Long groupId;

    private String backgroundImg;

    private Integer points;

    private Integer checkinDays;

    private Integer followerCount;

    private Integer followingCount;

    private Integer likeCount;

    private String address;

    private Integer status;

    private LocalDateTime lastLoginTime;
}
