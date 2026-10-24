package com.readingsystem.service;

import com.readingsystem.common.PageResult;
import com.readingsystem.entity.Book;
import com.readingsystem.vo.BookDetailVO;

import java.util.List;

public interface BookService {

    PageResult<Book> page(String category, String keyword, long current, long size);

    List<String> categories();

    BookDetailVO detail(Long id);

    void addShelf(Long bookId, Integer shelfType);

    void removeShelf(Long bookId);

    List<Book> shelfList(Integer shelfType);
}
