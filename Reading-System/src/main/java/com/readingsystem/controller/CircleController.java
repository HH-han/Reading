package com.readingsystem.controller;

import com.readingsystem.common.PageResult;
import com.readingsystem.common.Result;
import com.readingsystem.dto.JoinCircleDTO;
import com.readingsystem.entity.StudyJob;
import com.readingsystem.entity.TGroup;
import com.readingsystem.service.CircleService;
import com.readingsystem.vo.CircleBriefVO;
import com.readingsystem.vo.CircleCurrentVO;
import com.readingsystem.vo.UserInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 圈子/小组
 */
@RestController
@RequestMapping("/circle")
@RequiredArgsConstructor
public class CircleController {

    private final CircleService circleService;

    @GetMapping("/current")
    public Result<CircleCurrentVO> current() {
        return Result.ok(circleService.current());
    }

    @GetMapping("/groups")
    public Result<List<TGroup>> groups() {
        return Result.ok(circleService.groups());
    }

    @GetMapping("/members")
    public Result<PageResult<UserInfoVO>> members(@RequestParam(required = false) Long groupId,
                                                  @RequestParam(defaultValue = "1") long current,
                                                  @RequestParam(defaultValue = "10") long size) {
        return Result.ok(circleService.members(groupId, current, size));
    }

    @GetMapping("/job/page")
    public Result<PageResult<StudyJob>> jobPage(@RequestParam(defaultValue = "1") long current,
                                                @RequestParam(defaultValue = "10") long size) {
        return Result.ok(circleService.jobPage(current, size));
    }

    @GetMapping("/available")
    public Result<List<CircleBriefVO>> available() {
        return Result.ok(circleService.available());
    }

    @GetMapping("/mine")
    public Result<List<CircleBriefVO>> mine() {
        return Result.ok(circleService.mine());
    }

    @PostMapping("/join")
    public Result<Void> join(@Valid @RequestBody JoinCircleDTO dto) {
        circleService.join(dto.getInviteCode());
        return Result.ok();
    }

    @PostMapping("/join/{circleId}")
    public Result<Void> joinById(@PathVariable Long circleId) {
        circleService.joinById(circleId);
        return Result.ok();
    }

    @PostMapping("/switch/{circleId}")
    public Result<Void> switchTo(@PathVariable Long circleId) {
        circleService.switchTo(circleId);
        return Result.ok();
    }

    @PostMapping("/exit")
    public Result<Void> exit() {
        circleService.exit();
        return Result.ok();
    }

    @PostMapping("/exit/{circleId}")
    public Result<Void> exit(@PathVariable Long circleId) {
        circleService.exit(circleId);
        return Result.ok();
    }
}
