package com.readingsystem.controller;

import com.readingsystem.common.PageResult;
import com.readingsystem.common.Result;
import com.readingsystem.dto.ShelfAddDTO;
import com.readingsystem.entity.Book;
import com.readingsystem.service.BookService;
import com.readingsystem.vo.BookDetailVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 书库/书架
 */
@RestController
@RequestMapping("/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/page")
    public Result<PageResult<Book>> page(@RequestParam(required = false) String category,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(defaultValue = "1") long current,
                                         @RequestParam(defaultValue = "10") long size) {
        return Result.ok(bookService.page(category, keyword, current, size));
    }

    @GetMapping("/categories")
    public Result<List<String>> categories() {
        return Result.ok(bookService.categories());
    }

    @GetMapping("/{id}")
    public Result<BookDetailVO> detail(@PathVariable Long id) {
        return Result.ok(bookService.detail(id));
    }

    @PostMapping("/shelf")
    public Result<Void> addShelf(@Valid @RequestBody ShelfAddDTO dto) {
        bookService.addShelf(dto.getBookId(), dto.getShelfType());
        return Result.ok();
    }

    @DeleteMapping("/shelf/{bookId}")
    public Result<Void> removeShelf(@PathVariable Long bookId) {
        bookService.removeShelf(bookId);
        return Result.ok();
    }

    @GetMapping("/shelf")
    public Result<List<Book>> shelf(@RequestParam(required = false) Integer shelfType) {
        return Result.ok(bookService.shelfList(shelfType));
    }
}
