package com.readingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录
 */
@Data
public class WxLoginDTO {

    @NotBlank(message = "openId不能为空")
    private String openId;

    private String nickname;

    private String avatar;
}
