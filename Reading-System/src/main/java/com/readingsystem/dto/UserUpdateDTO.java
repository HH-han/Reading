package com.readingsystem.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 个人信息修改
 */
@Data
public class UserUpdateDTO {

    @Size(max = 50, message = "昵称最长50字")
    private String nickname;

    private String avatar;

    private String phone;

    private Integer gender;

    private String address;

    private String backgroundImg;
}
