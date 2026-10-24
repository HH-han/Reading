package com.readingsystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 加入书架
 */
@Data
public class ShelfAddDTO {

    @NotNull(message = "书籍ID不能为空")
    private Long bookId;

    /** 书架类型 1我的藏书 2小组合集 3精选集 */
    private Integer shelfType;
}
