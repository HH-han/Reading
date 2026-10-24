package com.readingsystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.readingsystem.annotation.OperLog;
import com.readingsystem.common.PageResult;
import com.readingsystem.common.Result;
import com.readingsystem.dto.AdminCheckinDTO;
import com.readingsystem.dto.AdminUserDTO;
import com.readingsystem.dto.BookDTO;
import com.readingsystem.dto.ChapterDTO;
import com.readingsystem.dto.CheckinEditDTO;
import com.readingsystem.entity.Book;
import com.readingsystem.entity.BookChapter;
import com.readingsystem.service.AdminService;
import com.readingsystem.service.CheckinService;
import com.readingsystem.vo.AdminCheckinVO;
import com.readingsystem.vo.UserInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 超级管理员数据管理（增删改查）
 */
@RestController
@RequestMapping("/admin/data")
@RequiredArgsConstructor
@SaCheckRole("SUPER_ADMIN")
public class AdminDataController {

    private final AdminService adminService;
    private final CheckinService checkinService;

    // ================= 用户 =================

    @GetMapping("/user/page")
    public Result<PageResult<UserInfoVO>> userPage(@RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Long circleId,
                                                   @RequestParam(required = false) String role,
                                                   @RequestParam(defaultValue = "1") long current,
                                                   @RequestParam(defaultValue = "10") long size) {
        return Result.ok(adminService.dataUserPage(keyword, circleId, role, current, size));
    }

    @PostMapping("/user")
    @OperLog(module = "数据管理", operation = "新增用户")
    public Result<Long> saveUser(@Valid @RequestBody AdminUserDTO dto) {
        return Result.ok(adminService.saveUser(dto));
    }

    @PutMapping("/user")
    @OperLog(module = "数据管理", operation = "修改用户")
    public Result<Void> updateUser(@Valid @RequestBody AdminUserDTO dto) {
        adminService.updateUser(dto);
        return Result.ok();
    }

    @DeleteMapping("/user/{id}")
    @OperLog(module = "数据管理", operation = "删除用户")
    public Result<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return Result.ok();
    }

    // ================= 书籍 =================

    @GetMapping("/book/page")
    public Result<PageResult<Book>> bookPage(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String category,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(defaultValue = "1") long current,
                                             @RequestParam(defaultValue = "10") long size) {
        return Result.ok(adminService.dataBookPage(keyword, category, status, current, size));
    }

    @PostMapping("/book")
    @OperLog(module = "数据管理", operation = "新增书籍")
    public Result<Long> saveBook(@Valid @RequestBody BookDTO dto) {
        return Result.ok(adminService.saveBook(dto));
    }

    @PutMapping("/book")
    @OperLog(module = "数据管理", operation = "修改书籍")
    public Result<Void> updateBook(@Valid @RequestBody BookDTO dto) {
        adminService.updateBook(dto);
        return Result.ok();
    }

    @DeleteMapping("/book/{id}")
    @OperLog(module = "数据管理", operation = "删除书籍")
    public Result<Void> deleteBook(@PathVariable Long id) {
        adminService.deleteBook(id);
        return Result.ok();
    }

    // ================= 章节 =================

    @GetMapping("/book/{bookId}/chapter/list")
    public Result<List<BookChapter>> chapterList(@PathVariable Long bookId) {
        return Result.ok(adminService.chapterList(bookId));
    }

    @PostMapping("/chapter")
    @OperLog(module = "数据管理", operation = "新增章节")
    public Result<Long> saveChapter(@Valid @RequestBody ChapterDTO dto) {
        return Result.ok(adminService.saveChapter(dto));
    }

    @PutMapping("/chapter")
    @OperLog(module = "数据管理", operation = "修改章节")
    public Result<Void> updateChapter(@Valid @RequestBody ChapterDTO dto) {
        adminService.updateChapter(dto);
        return Result.ok();
    }

    @DeleteMapping("/chapter/{id}")
    @OperLog(module = "数据管理", operation = "删除章节")
    public Result<Void> deleteChapter(@PathVariable Long id) {
        adminService.deleteChapter(id);
        return Result.ok();
    }

    // ================= 打卡记录 =================

    @GetMapping("/checkin/page")
    public Result<PageResult<AdminCheckinVO>> checkinPage(@RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) Long userId,
                                                          @RequestParam(required = false) Long circleId,
                                                          @RequestParam(required = false) String startDate,
                                                          @RequestParam(required = false) String endDate,
                                                          @RequestParam(required = false) Integer status,
                                                          @RequestParam(defaultValue = "1") long current,
                                                          @RequestParam(defaultValue = "10") long size) {
        return Result.ok(adminService.dataCheckinPage(keyword, userId, circleId, startDate, endDate, status, current, size));
    }

    @PostMapping("/checkin")
    @OperLog(module = "数据管理", operation = "新增打卡记录")
    public Result<Long> saveCheckin(@Valid @RequestBody AdminCheckinDTO dto) {
        return Result.ok(checkinService.adminAdd(dto));
    }

    @PutMapping("/checkin")
    @OperLog(module = "数据管理", operation = "修改打卡记录")
    public Result<Void> updateCheckin(@Valid @RequestBody CheckinEditDTO dto) {
        adminService.updateCheckin(dto);
        return Result.ok();
    }

    @DeleteMapping("/checkin/{id}")
    @OperLog(module = "数据管理", operation = "删除打卡记录")
    public Result<Void> deleteCheckin(@PathVariable Long id) {
        adminService.deleteCheckin(id);
        return Result.ok();
    }
}
