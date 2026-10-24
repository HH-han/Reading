package com.readingsystem.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.readingsystem.annotation.OperLog;
import com.readingsystem.common.PageResult;
import com.readingsystem.common.Result;
import com.readingsystem.dto.AdminCheckinDTO;
import com.readingsystem.dto.CircleDTO;
import com.readingsystem.dto.GroupDTO;
import com.readingsystem.dto.JobDTO;
import com.readingsystem.dto.RuleDTO;
import com.readingsystem.dto.TemplateDTO;
import com.readingsystem.entity.CheckinRule;
import com.readingsystem.entity.CheckinTemplate;
import com.readingsystem.entity.Circle;
import com.readingsystem.entity.StudyJob;
import com.readingsystem.entity.TGroup;
import com.readingsystem.service.AdminService;
import com.readingsystem.service.CheckinService;
import com.readingsystem.vo.CircleBriefVO;
import com.readingsystem.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 管理员后台
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@SaCheckRole(value = {"SUPER_ADMIN", "TEAM_ADMIN"}, mode = SaMode.OR)
public class AdminController {

    private final AdminService adminService;
    private final CheckinService checkinService;

    // ================= 作业 =================

    @GetMapping("/job/page")
    public Result<PageResult<StudyJob>> jobPage(@RequestParam(required = false) Long circleId,
                                                @RequestParam(defaultValue = "1") long current,
                                                @RequestParam(defaultValue = "10") long size) {
        return Result.ok(adminService.jobPage(circleId, current, size));
    }

    @PostMapping("/job")
    @OperLog(module = "作业管理", operation = "布置学习作业")
    public Result<Long> saveJob(@Valid @RequestBody JobDTO dto) {
        return Result.ok(adminService.saveJob(dto));
    }

    @PutMapping("/job")
    @OperLog(module = "作业管理", operation = "调整学习作业")
    public Result<Void> updateJob(@Valid @RequestBody JobDTO dto) {
        adminService.updateJob(dto);
        return Result.ok();
    }

    @DeleteMapping("/job/{id}")
    @OperLog(module = "作业管理", operation = "删除学习作业")
    public Result<Void> deleteJob(@PathVariable Long id) {
        adminService.deleteJob(id);
        return Result.ok();
    }

    // ================= 读书圈 =================

    @GetMapping("/circle/list")
    public Result<List<Circle>> circleList() {
        return Result.ok(adminService.circleList());
    }

    @PostMapping("/circle")
    @OperLog(module = "读书圈管理", operation = "创建读书圈")
    public Result<Long> saveCircle(@Valid @RequestBody CircleDTO dto) {
        return Result.ok(adminService.saveCircle(dto));
    }

    @PutMapping("/circle")
    @OperLog(module = "读书圈管理", operation = "修改读书圈")
    public Result<Void> updateCircle(@Valid @RequestBody CircleDTO dto) {
        adminService.updateCircle(dto);
        return Result.ok();
    }

    @DeleteMapping("/circle/{id}")
    @OperLog(module = "读书圈管理", operation = "删除读书圈")
    public Result<Void> deleteCircle(@PathVariable Long id) {
        adminService.deleteCircle(id);
        return Result.ok();
    }

    @GetMapping("/circle/{id}/member/page")
    public Result<PageResult<UserInfoVO>> circleMembers(@PathVariable Long id,
                                                        @RequestParam(required = false) String keyword,
                                                        @RequestParam(defaultValue = "1") long current,
                                                        @RequestParam(defaultValue = "20") long size) {
        return Result.ok(adminService.circleMembers(id, keyword, current, size));
    }

    @PostMapping("/circle/{id}/member/{userId}")
    @OperLog(module = "读书圈管理", operation = "邀请成员加入读书圈")
    public Result<Void> inviteMember(@PathVariable Long id, @PathVariable Long userId) {
        adminService.inviteMember(id, userId);
        return Result.ok();
    }

    @DeleteMapping("/circle/{id}/member/{userId}")
    @OperLog(module = "读书圈管理", operation = "移除读书圈成员")
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long userId) {
        adminService.removeMember(id, userId);
        return Result.ok();
    }

    // ================= 读书圈小组 =================

    @GetMapping("/circle/{circleId}/group/list")
    public Result<List<TGroup>> groupList(@PathVariable Long circleId) {
        return Result.ok(adminService.groupList(circleId));
    }

    @PostMapping("/group")
    @OperLog(module = "小组管理", operation = "创建小组")
    public Result<Long> saveGroup(@Valid @RequestBody GroupDTO dto) {
        return Result.ok(adminService.saveGroup(dto));
    }

    @PutMapping("/group")
    @OperLog(module = "小组管理", operation = "修改小组")
    public Result<Void> updateGroup(@Valid @RequestBody GroupDTO dto) {
        adminService.updateGroup(dto);
        return Result.ok();
    }

    @DeleteMapping("/group/{id}")
    @OperLog(module = "小组管理", operation = "删除小组")
    public Result<Void> deleteGroup(@PathVariable Long id) {
        adminService.deleteGroup(id);
        return Result.ok();
    }

    @GetMapping("/group/{groupId}/member/page")
    public Result<PageResult<UserInfoVO>> groupMembers(@PathVariable Long groupId,
                                                       @RequestParam(defaultValue = "1") long current,
                                                       @RequestParam(defaultValue = "20") long size) {
        return Result.ok(adminService.groupMembers(groupId, current, size));
    }

    @PutMapping("/circle/{circleId}/member/{userId}/group")
    @OperLog(module = "小组管理", operation = "调整成员小组")
    public Result<Void> assignMemberGroup(@PathVariable Long circleId,
                                          @PathVariable Long userId,
                                          @RequestParam(required = false) Long groupId) {
        adminService.assignMemberGroup(circleId, userId, groupId);
        return Result.ok();
    }

    @GetMapping("/user/page")
    public Result<PageResult<UserInfoVO>> userPage(@RequestParam(required = false) String keyword,
                                                   @RequestParam(defaultValue = "1") long current,
                                                   @RequestParam(defaultValue = "20") long size) {
        return Result.ok(adminService.userPage(keyword, current, size));
    }

    // ================= 打卡规则 =================

    @GetMapping("/rule/list")
    public Result<List<CheckinRule>> ruleList(@RequestParam(required = false) Long circleId) {
        return Result.ok(adminService.ruleList(circleId));
    }

    @PostMapping("/rule")
    @OperLog(module = "打卡规则", operation = "新增规则")
    public Result<Long> saveRule(@Valid @RequestBody RuleDTO dto) {
        return Result.ok(adminService.saveRule(dto));
    }

    @PutMapping("/rule")
    @OperLog(module = "打卡规则", operation = "修改规则")
    public Result<Void> updateRule(@Valid @RequestBody RuleDTO dto) {
        adminService.updateRule(dto);
        return Result.ok();
    }

    @DeleteMapping("/rule/{id}")
    @OperLog(module = "打卡规则", operation = "删除规则")
    public Result<Void> deleteRule(@PathVariable Long id) {
        adminService.deleteRule(id);
        return Result.ok();
    }

    // ================= 打卡模板 =================

    @GetMapping("/template/list")
    public Result<List<CheckinTemplate>> templateList(@RequestParam(required = false) Long circleId) {
        return Result.ok(adminService.templateList(circleId));
    }

    @PostMapping("/template")
    @OperLog(module = "打卡模板", operation = "新增模板")
    public Result<Long> saveTemplate(@Valid @RequestBody TemplateDTO dto) {
        return Result.ok(adminService.saveTemplate(dto));
    }

    @PutMapping("/template")
    @OperLog(module = "打卡模板", operation = "修改模板")
    public Result<Void> updateTemplate(@Valid @RequestBody TemplateDTO dto) {
        adminService.updateTemplate(dto);
        return Result.ok();
    }

    @DeleteMapping("/template/{id}")
    @OperLog(module = "打卡模板", operation = "删除模板")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        adminService.deleteTemplate(id);
        return Result.ok();
    }

    // ================= 打卡管理 =================

    @PostMapping("/checkin")
    @OperLog(module = "打卡管理", operation = "管理员代队员打卡")
    public Result<Long> adminCheckin(@Valid @RequestBody AdminCheckinDTO dto) {
        return Result.ok(checkinService.adminAdd(dto));
    }

    @GetMapping("/export")
    @OperLog(module = "打卡管理", operation = "导出打卡数据")
    public void export(@RequestParam(required = false) Long circleId,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       HttpServletResponse response) {
        adminService.exportCheckin(circleId, startDate, endDate, response);
    }

    @PostMapping("/import")
    @SaCheckRole("SUPER_ADMIN")
    @OperLog(module = "初始数据", operation = "导入初始用户")
    public Result<Integer> importUsers(@RequestParam("file") MultipartFile file) {
        return Result.ok(adminService.importUsers(file));
    }

    // ================= 账号管理 =================

    @PutMapping("/user/{id}/status")
    @OperLog(module = "账号管理", operation = "禁用/启用用户")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminService.updateUserStatus(id, status);
        return Result.ok();
    }

    @GetMapping("/user/{id}/circle/list")
    @SaCheckRole("SUPER_ADMIN")
    public Result<List<CircleBriefVO>> userCircles(@PathVariable Long id) {
        return Result.ok(adminService.userCircles(id));
    }

    @PutMapping("/user/{id}/position")
    @SaCheckRole("SUPER_ADMIN")
    @OperLog(module = "账号管理", operation = "分配用户职位")
    public Result<Void> updateUserPosition(@PathVariable Long id,
                                           @RequestParam(required = false) String position) {
        adminService.updateUserPosition(id, position);
        return Result.ok();
    }

    @DeleteMapping("/account")
    @OperLog(module = "账号管理", operation = "注销自己账号")
    public Result<Void> cancelAccount() {
        adminService.cancelAccount();
        return Result.ok();
    }
}
