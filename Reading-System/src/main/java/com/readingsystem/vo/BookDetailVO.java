package com.readingsystem.vo;

import com.readingsystem.entity.Book;
import com.readingsystem.entity.BookChapter;
import lombok.Data;

import java.util.List;

/**
 * 书籍详情(含章节)
 */
@Data
public class BookDetailVO {

    private Book book;

    private List<BookChapter> chapters;
}
