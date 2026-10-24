package com.readingsystem.controller;

import com.readingsystem.common.PageResult;
import com.readingsystem.common.Result;
import com.readingsystem.dto.UserUpdateDTO;
import com.readingsystem.entity.UserGrowthLog;
import com.readingsystem.service.UserService;
import com.readingsystem.vo.CheckinDaysVO;
import com.readingsystem.vo.FavoriteVO;
import com.readingsystem.vo.UserInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public Result<UserInfoVO> info() {
        return Result.ok(userService.info());
    }

    @PutMapping("/info")
    public Result<Void> updateInfo(@Valid @RequestBody UserUpdateDTO dto) {
        userService.updateInfo(dto);
        return Result.ok();
    }

    @GetMapping("/growth")
    public Result<PageResult<UserGrowthLog>> growth(@RequestParam(defaultValue = "1") long current,
                                                    @RequestParam(defaultValue = "10") long size) {
        return Result.ok(userService.growthPage(current, size));
    }

    @GetMapping("/favorites")
    public Result<PageResult<FavoriteVO>> favorites(@RequestParam(defaultValue = "1") long current,
                                                    @RequestParam(defaultValue = "10") long size) {
        return Result.ok(userService.favoritesPage(current, size));
    }

    @GetMapping("/checkin-days")
    public Result<CheckinDaysVO> checkinDays() {
        return Result.ok(userService.checkinDays());
    }
}
