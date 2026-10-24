package com.readingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增评论
 */
@Data
public class CommentAddDTO {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论最长500字")
    private String content;

    private Long parentId;
}
