package com.readingsystem.controller;

import com.readingsystem.common.PageResult;
import com.readingsystem.common.Result;
import com.readingsystem.dto.CheckinAddDTO;
import com.readingsystem.dto.CheckinUpdateDTO;
import com.readingsystem.dto.CommentAddDTO;
import com.readingsystem.entity.CheckinRecord;
import com.readingsystem.entity.CheckinTemplate;
import com.readingsystem.service.CheckinService;
import com.readingsystem.vo.CheckinFeedVO;
import com.readingsystem.vo.CommentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 打卡
 */
@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @PostMapping
    public Result<Long> add(@Valid @RequestBody CheckinAddDTO dto) {
        return Result.ok(checkinService.add(dto));
    }

    @GetMapping("/template")
    public Result<CheckinTemplate> template() {
        return Result.ok(checkinService.currentTemplate());
    }

    @GetMapping("/feed")
    public Result<PageResult<CheckinFeedVO>> feed(@RequestParam(required = false) String tab,
                                                  @RequestParam(defaultValue = "1") long current,
                                                  @RequestParam(defaultValue = "10") long size) {
        return Result.ok(checkinService.feed(tab, current, size));
    }

    @GetMapping("/my")
    public Result<PageResult<CheckinRecord>> my(@RequestParam(defaultValue = "1") long current,
                                                @RequestParam(defaultValue = "10") long size) {
        return Result.ok(checkinService.my(current, size));
    }

    @GetMapping("/{id}")
    public Result<CheckinRecord> detail(@PathVariable Long id) {
        return Result.ok(checkinService.detail(id));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CheckinUpdateDTO dto) {
        checkinService.updateMy(id, dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkinService.delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id) {
        checkinService.like(id);
        return Result.ok();
    }

    @DeleteMapping("/{id}/like")
    public Result<Void> unlike(@PathVariable Long id) {
        checkinService.unlike(id);
        return Result.ok();
    }

    @GetMapping("/{id}/comments")
    public Result<List<CommentVO>> comments(@PathVariable Long id) {
        return Result.ok(checkinService.comments(id));
    }

    @PostMapping("/{id}/comment")
    public Result<Long> addComment(@PathVariable Long id, @Valid @RequestBody CommentAddDTO dto) {
        return Result.ok(checkinService.addComment(id, dto));
    }
}
