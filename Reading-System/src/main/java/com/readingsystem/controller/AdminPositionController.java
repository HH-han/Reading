package com.readingsystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.readingsystem.annotation.OperLog;
import com.readingsystem.common.Result;
import com.readingsystem.dto.PositionDTO;
import com.readingsystem.service.AdminService;
import com.readingsystem.vo.PositionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职位管理
 */
@RestController
@RequestMapping("/admin/position")
@RequiredArgsConstructor
public class AdminPositionController {

    private final AdminService adminService;

    @GetMapping("/list")
    @SaCheckRole(value = {"SUPER_ADMIN", "TEAM_ADMIN"}, mode = SaMode.OR)
    public Result<List<PositionVO>> list(@RequestParam(required = false) String keyword) {
        return Result.ok(adminService.positionList(keyword));
    }

    @PostMapping
    @SaCheckRole("SUPER_ADMIN")
    @OperLog(module = "职位管理", operation = "新增职位")
    public Result<Long> save(@Valid @RequestBody PositionDTO dto) {
        return Result.ok(adminService.savePosition(dto));
    }

    @PutMapping
    @SaCheckRole("SUPER_ADMIN")
    @OperLog(module = "职位管理", operation = "修改职位")
    public Result<Void> update(@Valid @RequestBody PositionDTO dto) {
        adminService.updatePosition(dto);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckRole("SUPER_ADMIN")
    @OperLog(module = "职位管理", operation = "删除职位")
    public Result<Void> delete(@PathVariable Long id) {
        adminService.deletePosition(id);
        return Result.ok();
    }
}
