package com.readingsystem.controller;

import com.readingsystem.common.PageResult;
import com.readingsystem.common.Result;
import com.readingsystem.entity.Notification;
import com.readingsystem.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 消息通知
 */
@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyService notifyService;

    @GetMapping("/page")
    public Result<PageResult<Notification>> page(@RequestParam(defaultValue = "1") long current,
                                                 @RequestParam(defaultValue = "10") long size) {
        return Result.ok(notifyService.page(current, size));
    }

    @PutMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        notifyService.read(id);
        return Result.ok();
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.ok(notifyService.unreadCount());
    }
}
