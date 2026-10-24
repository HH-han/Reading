package com.readingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 书籍数据维护
 */
@Data
public class BookDTO {

    private Long id;

    @NotBlank(message = "书籍名称不能为空")
    private String title;

    private String author;

    private String publisher;

    private String cover;

    /** 分类 */
    private String category;

    /** 类型 1电子书 2实体书 */
    private Integer bookType;

    private String description;

    private String recommendReason;

    /** 是否开放书库 0否 1是 */
    private Integer isPublic;

    /** 状态 0下架 1上架 */
    private Integer status;

    private Integer sort;
}
