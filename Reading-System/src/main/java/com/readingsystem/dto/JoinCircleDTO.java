package com.readingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 加入圈子/小组
 */
@Data
public class JoinCircleDTO {

    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;
}
