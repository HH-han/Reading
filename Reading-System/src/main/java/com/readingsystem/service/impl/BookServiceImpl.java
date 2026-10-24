package com.readingsystem.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.readingsystem.common.PageResult;
import com.readingsystem.entity.Book;
import com.readingsystem.entity.BookChapter;
import com.readingsystem.entity.BookShelf;
import com.readingsystem.exception.BusinessException;
import com.readingsystem.mapper.BookChapterMapper;
import com.readingsystem.mapper.BookMapper;
import com.readingsystem.mapper.BookShelfMapper;
import com.readingsystem.service.BookService;
import com.readingsystem.vo.BookDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 书籍服务
 */
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final BookChapterMapper bookChapterMapper;
    private final BookShelfMapper bookShelfMapper;

    @Override
    public PageResult<Book> page(String category, String keyword, long current, long size) {
        Page<Book> page = bookMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Book>()
                        .eq(Book::getStatus, 1)
                        .eq(Book::getIsPublic, 1)
                        .eq(StrUtil.isNotBlank(category), Book::getCategory, category)
                        .and(StrUtil.isNotBlank(keyword), w -> w
                                .like(Book::getTitle, keyword)
                                .or()
                                .like(Book::getAuthor, keyword))
                        .orderByAsc(Book::getSort)
                        .orderByDesc(Book::getCreateTime));
        return PageResult.of(page);
    }

    @Override
    public List<String> categories() {
        return bookMapper.selectList(new LambdaQueryWrapper<Book>()
                        .select(Book::getCategory)
                        .eq(Book::getStatus, 1)
                        .groupBy(Book::getCategory))
                .stream()
                .map(Book::getCategory)
                .filter(StrUtil::isNotBlank)
                .toList();
    }

    @Override
    public BookDetailVO detail(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException("书籍不存在");
        }
        List<BookChapter> chapters = bookChapterMapper.selectList(new LambdaQueryWrapper<BookChapter>()
                .eq(BookChapter::getBookId, id)
                .orderByAsc(BookChapter::getChapterNo));
        BookDetailVO vo = new BookDetailVO();
        vo.setBook(book);
        vo.setChapters(chapters);
        return vo;
    }

    @Override
    public void addShelf(Long bookId, Integer shelfType) {
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException("书籍不存在");
        }
        int type = shelfType == null ? 1 : shelfType;
        Long userId = StpUtil.getLoginIdAsLong();
        Long count = bookShelfMapper.selectCount(new LambdaQueryWrapper<BookShelf>()
                .eq(BookShelf::getUserId, userId)
                .eq(BookShelf::getBookId, bookId)
                .eq(BookShelf::getShelfType, type));
        if (count > 0) {
            return;
        }
        BookShelf shelf = new BookShelf();
        shelf.setUserId(userId);
        shelf.setBookId(bookId);
        shelf.setShelfType(type);
        shelf.setSource(1);
        shelf.setProgress(0);
        bookShelfMapper.insert(shelf);
    }

    @Override
    public void removeShelf(Long bookId) {
        bookShelfMapper.delete(new LambdaQueryWrapper<BookShelf>()
                .eq(BookShelf::getUserId, StpUtil.getLoginIdAsLong())
                .eq(BookShelf::getBookId, bookId));
    }

    @Override
    public List<Book> shelfList(Integer shelfType) {
        List<BookShelf> shelves = bookShelfMapper.selectList(new LambdaQueryWrapper<BookShelf>()
                .eq(BookShelf::getUserId, StpUtil.getLoginIdAsLong())
                .eq(shelfType != null, BookShelf::getShelfType, shelfType)
                .orderByDesc(BookShelf::getCreateTime));
        if (shelves.isEmpty()) {
            return List.of();
        }
        List<Long> bookIds = shelves.stream().map(BookShelf::getBookId).toList();
        return bookMapper.selectBatchIds(bookIds);
    }
}
