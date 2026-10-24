package com.readingsystem.controller;

import com.readingsystem.common.Result;
import com.readingsystem.service.DashboardService;
import com.readingsystem.vo.DashboardTodayVO;
import com.readingsystem.vo.MyStatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 统计看板
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/today")
    public Result<DashboardTodayVO> today() {
        return Result.ok(dashboardService.today());
    }

    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend(@RequestParam(defaultValue = "30") Integer days) {
        return Result.ok(dashboardService.trend(days));
    }

    @GetMapping("/top-groups")
    public Result<List<Map<String, Object>>> topGroups() {
        return Result.ok(dashboardService.topGroups());
    }

    @GetMapping("/my")
    public Result<MyStatVO> my() {
        return Result.ok(dashboardService.my());
    }
}
