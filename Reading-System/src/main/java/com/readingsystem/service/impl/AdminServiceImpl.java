package com.readingsystem.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.readingsystem.common.PageResult;
import com.readingsystem.constant.SystemConstant;
import com.readingsystem.dto.AdminUserDTO;
import com.readingsystem.dto.BookDTO;
import com.readingsystem.dto.ChapterDTO;
import com.readingsystem.dto.CheckinEditDTO;
import com.readingsystem.dto.CircleDTO;
import com.readingsystem.dto.GroupDTO;
import com.readingsystem.dto.JobDTO;
import com.readingsystem.dto.PositionDTO;
import com.readingsystem.dto.RuleDTO;
import com.readingsystem.dto.TemplateDTO;
import com.readingsystem.dto.UserImportDTO;
import com.readingsystem.entity.*;
import com.readingsystem.enums.CheckinTypeEnum;
import com.readingsystem.enums.RoleEnum;
import com.readingsystem.exception.BusinessException;
import com.readingsystem.mapper.*;
import com.readingsystem.service.AdminService;
import com.readingsystem.vo.AdminCheckinVO;
import com.readingsystem.vo.CheckinExportVO;
import com.readingsystem.vo.CircleBriefVO;
import com.readingsystem.vo.PositionVO;
import com.readingsystem.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 管理员服务
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final StudyJobMapper studyJobMapper;
    private final CheckinRuleMapper checkinRuleMapper;
    private final CheckinTemplateMapper checkinTemplateMapper;
    private final NotificationMapper notificationMapper;
    private final SysUserMapper sysUserMapper;
    private final CheckinRecordMapper checkinRecordMapper;
    private final BookMapper bookMapper;
    private final BookChapterMapper bookChapterMapper;
    private final CircleMapper circleMapper;
    private final TGroupMapper tGroupMapper;
    private final UserCircleMapper userCircleMapper;
    private final SysPositionMapper sysPositionMapper;

    // ================= 作业 =================

    @Override
    public PageResult<StudyJob> jobPage(Long circleId, long current, long size) {
        Page<StudyJob> page = studyJobMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<StudyJob>()
                        .eq(circleId != null, StudyJob::getCircleId, circleId)
                        .orderByDesc(StudyJob::getCreateTime));
        return PageResult.of(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveJob(JobDTO dto) {
        StudyJob job = BeanUtil.copyProperties(dto, StudyJob.class);
        job.setPublisherId(StpUtil.getLoginIdAsLong());
        if (job.getStatus() == null) {
            job.setStatus(1);
        }
        studyJobMapper.insert(job);
        if (job.getStatus() == 1) {
            notifyCircle(job);
        }
        return job.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateJob(JobDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("作业ID不能为空");
        }
        StudyJob exist = studyJobMapper.selectById(dto.getId());
        if (exist == null) {
            throw new BusinessException("作业不存在");
        }
        StudyJob job = BeanUtil.copyProperties(dto, StudyJob.class);
        studyJobMapper.updateById(job);
        if (exist.getStatus() != 1 && job.getStatus() != null && job.getStatus() == 1) {
            notifyCircle(studyJobMapper.selectById(dto.getId()));
        }
    }

    @Override
    public void deleteJob(Long id) {
        studyJobMapper.deleteById(id);
    }

    /** 发布作业时给圈内成员写通知 */
    private void notifyCircle(StudyJob job) {
        List<SysUser> members = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getCircleId, job.getCircleId())
                .eq(job.getGroupId() != null, SysUser::getGroupId, job.getGroupId())
                .eq(SysUser::getStatus, 1));
        for (SysUser member : members) {
            Notification n = new Notification();
            n.setUserId(member.getId());
            n.setTitle("新作业通知");
            n.setContent("管理员布置了新作业: " + job.getTitle());
            n.setNotifyType(SystemConstant.NOTIFY_JOB);
            n.setBizId(job.getId());
            n.setReadStatus(0);
            notificationMapper.insert(n);
        }
    }

    // ================= 读书圈 =================

    @Override
    public List<Circle> circleList() {
        return circleMapper.selectList(new LambdaQueryWrapper<Circle>()
                .orderByDesc(Circle::getId));
    }

    @Override
    public Long saveCircle(CircleDTO dto) {
        Long count = circleMapper.selectCount(new LambdaQueryWrapper<Circle>()
                .eq(Circle::getName, dto.getName()));
        if (count > 0) {
            throw new BusinessException("读书圈名称已存在");
        }
        Circle circle = new Circle();
        circle.setName(dto.getName().trim());
        circle.setDescription(StrUtil.blankToDefault(dto.getDescription(), ""));
        circle.setCover(StrUtil.blankToDefault(dto.getCover(), ""));
        circle.setOwnerId(StpUtil.getLoginIdAsLong());
        circle.setMemberCount(0);
        circle.setInviteCode(generateInviteCode());
        circle.setStatus(1);
        circleMapper.insert(circle);
        return circle.getId();
    }

    @Override
    public void updateCircle(CircleDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("读书圈ID不能为空");
        }
        Circle exist = circleMapper.selectById(dto.getId());
        if (exist == null) {
            throw new BusinessException("读书圈不存在");
        }
        Long count = circleMapper.selectCount(new LambdaQueryWrapper<Circle>()
                .eq(Circle::getName, dto.getName())
                .ne(Circle::getId, dto.getId()));
        if (count > 0) {
            throw new BusinessException("读书圈名称已存在");
        }
        Circle update = new Circle();
        update.setId(dto.getId());
        update.setName(dto.getName().trim());
        update.setDescription(StrUtil.blankToDefault(dto.getDescription(), ""));
        if (StrUtil.isNotBlank(dto.getCover())) {
            update.setCover(dto.getCover());
        }
        circleMapper.updateById(update);
    }

    @Override
    public void deleteCircle(Long id) {
        Circle circle = circleMapper.selectById(id);
        if (circle == null) {
            throw new BusinessException("读书圈不存在");
        }
        if (circle.getMemberCount() != null && circle.getMemberCount() > 0) {
            throw new BusinessException("圈内仍有成员，无法删除");
        }
        circleMapper.deleteById(id);
    }

    @Override
    public PageResult<UserInfoVO> circleMembers(Long circleId, String keyword, long current, long size) {
        Page<SysUser> page = new Page<>(current, size);
        return PageResult.of(
                userCircleMapper.pageMembers(page, circleId, null, keyword),
                u -> BeanUtil.copyProperties(u, UserInfoVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inviteMember(Long circleId, Long userId) {
        Circle circle = circleMapper.selectById(circleId);
        if (circle == null || circle.getStatus() == null || circle.getStatus() != 1) {
            throw new BusinessException("读书圈不存在或已停用");
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserCircle membership = userCircleMapper.selectOne(new LambdaQueryWrapper<UserCircle>()
                .eq(UserCircle::getUserId, userId)
                .eq(UserCircle::getCircleId, circleId));
        if (membership != null) {
            throw new BusinessException("该用户已在该读书圈中");
        }
        UserCircle created = new UserCircle();
        created.setUserId(userId);
        created.setCircleId(circleId);
        userCircleMapper.insert(created);
        circleMapper.update(null, new LambdaUpdateWrapper<Circle>()
                .eq(Circle::getId, circleId)
                .setSql("member_count = member_count + 1"));
        // 用户还没有当前圈子时，邀请后直接作为当前圈子
        if (user.getCircleId() == null) {
            sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                    .eq(SysUser::getId, userId)
                    .set(SysUser::getCircleId, circleId)
                    .set(SysUser::getGroupId, null));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long circleId, Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        UserCircle membership = userCircleMapper.selectOne(new LambdaQueryWrapper<UserCircle>()
                .eq(UserCircle::getUserId, userId)
                .eq(UserCircle::getCircleId, circleId));
        if (user == null || membership == null) {
            throw new BusinessException("该用户不在当前读书圈");
        }
        if (membership.getGroupId() != null) {
            tGroupMapper.update(null, new LambdaUpdateWrapper<TGroup>()
                    .eq(TGroup::getId, membership.getGroupId())
                    .setSql("member_count = GREATEST(member_count - 1, 0)"));
        }
        circleMapper.update(null, new LambdaUpdateWrapper<Circle>()
                .eq(Circle::getId, circleId)
                .setSql("member_count = GREATEST(member_count - 1, 0)"));
        userCircleMapper.deleteById(membership.getId());
        if (circleId.equals(user.getCircleId())) {
            UserCircle next = userCircleMapper.selectOne(new LambdaQueryWrapper<UserCircle>()
                    .eq(UserCircle::getUserId, userId)
                    .orderByDesc(UserCircle::getId)
                    .last("LIMIT 1"));
            sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                    .eq(SysUser::getId, userId)
                    .set(SysUser::getCircleId, next == null ? null : next.getCircleId())
                    .set(SysUser::getGroupId, next == null ? null : next.getGroupId()));
        }
    }

    // ================= 读书圈小组 =================

    @Override
    public List<TGroup> groupList(Long circleId) {
        if (circleId == null) {
            throw new BusinessException("读书圈不能为空");
        }
        return tGroupMapper.selectList(new LambdaQueryWrapper<TGroup>()
                .eq(TGroup::getCircleId, circleId)
                .orderByAsc(TGroup::getId));
    }

    @Override
    public Long saveGroup(GroupDTO dto) {
        Circle circle = circleMapper.selectById(dto.getCircleId());
        if (circle == null) {
            throw new BusinessException("读书圈不存在");
        }
        String name = dto.getName().trim();
        Long count = tGroupMapper.selectCount(new LambdaQueryWrapper<TGroup>()
                .eq(TGroup::getCircleId, dto.getCircleId())
                .eq(TGroup::getName, name));
        if (count > 0) {
            throw new BusinessException("小组名称已存在");
        }
        TGroup group = new TGroup();
        group.setCircleId(dto.getCircleId());
        group.setName(name);
        group.setDescription(StrUtil.blankToDefault(dto.getDescription(), ""));
        group.setMemberCount(0);
        group.setInviteCode(generateGroupInviteCode());
        group.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        tGroupMapper.insert(group);
        return group.getId();
    }

    @Override
    public void updateGroup(GroupDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("小组ID不能为空");
        }
        TGroup exist = tGroupMapper.selectById(dto.getId());
        if (exist == null) {
            throw new BusinessException("小组不存在");
        }
        String name = dto.getName().trim();
        Long count = tGroupMapper.selectCount(new LambdaQueryWrapper<TGroup>()
                .eq(TGroup::getCircleId, exist.getCircleId())
                .eq(TGroup::getName, name)
                .ne(TGroup::getId, dto.getId()));
        if (count > 0) {
            throw new BusinessException("小组名称已存在");
        }
        TGroup update = new TGroup();
        update.setId(dto.getId());
        update.setName(name);
        update.setDescription(StrUtil.blankToDefault(dto.getDescription(), ""));
        if (dto.getStatus() != null) {
            update.setStatus(dto.getStatus());
        }
        tGroupMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(Long id) {
        TGroup group = tGroupMapper.selectById(id);
        if (group == null) {
            throw new BusinessException("小组不存在");
        }
        List<UserCircle> members = userCircleMapper.selectList(new LambdaQueryWrapper<UserCircle>()
                .eq(UserCircle::getGroupId, id));
        for (UserCircle membership : members) {
            membership.setGroupId(null);
            userCircleMapper.updateById(membership);
            sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                    .eq(SysUser::getId, membership.getUserId())
                    .eq(SysUser::getCircleId, group.getCircleId())
                    .set(SysUser::getGroupId, null));
        }
        tGroupMapper.deleteById(id);
    }

    @Override
    public PageResult<UserInfoVO> groupMembers(Long groupId, long current, long size) {
        TGroup group = tGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("小组不存在");
        }
        IPage<SysUser> page = userCircleMapper.pageMembers(new Page<>(current, size),
                group.getCircleId(), groupId, null);
        return PageResult.of(page, u -> BeanUtil.copyProperties(u, UserInfoVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMemberGroup(Long circleId, Long userId, Long groupId) {
        UserCircle membership = userCircleMapper.selectOne(new LambdaQueryWrapper<UserCircle>()
                .eq(UserCircle::getUserId, userId)
                .eq(UserCircle::getCircleId, circleId));
        if (membership == null) {
            throw new BusinessException("该用户不在当前读书圈");
        }
        if (groupId != null) {
            TGroup group = tGroupMapper.selectById(groupId);
            if (group == null || !circleId.equals(group.getCircleId())) {
                throw new BusinessException("小组不属于当前读书圈");
            }
            if (groupId.equals(membership.getGroupId())) {
                throw new BusinessException("该用户已在该小组中");
            }
        }
        if (membership.getGroupId() != null) {
            TGroup oldGroup = tGroupMapper.selectById(membership.getGroupId());
            if (oldGroup != null && circleId.equals(oldGroup.getCircleId())) {
                tGroupMapper.update(null, new LambdaUpdateWrapper<TGroup>()
                        .eq(TGroup::getId, membership.getGroupId())
                        .setSql("member_count = GREATEST(member_count - 1, 0)"));
            }
        }
        membership.setGroupId(groupId);
        userCircleMapper.updateById(membership);
        if (groupId != null) {
            tGroupMapper.update(null, new LambdaUpdateWrapper<TGroup>()
                    .eq(TGroup::getId, groupId)
                    .setSql("member_count = member_count + 1"));
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user != null && circleId.equals(user.getCircleId())) {
            sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                    .eq(SysUser::getId, userId)
                    .set(SysUser::getGroupId, groupId));
        }
    }

    /** 生成唯一的小组邀请码 */
    private String generateGroupInviteCode() {
        for (int i = 0; i < 10; i++) {
            String code = "G" + RandomUtil.randomStringUpper(6);
            Long count = tGroupMapper.selectCount(new LambdaQueryWrapper<TGroup>()
                    .eq(TGroup::getInviteCode, code));
            if (count == 0) {
                return code;
            }
        }
        throw new BusinessException("小组邀请码生成失败，请重试");
    }

    @Override
    public PageResult<UserInfoVO> userPage(String keyword, long current, long size) {
        Page<SysUser> page = sysUserMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<SysUser>()
                        .and(StrUtil.isNotBlank(keyword), w -> w.like(SysUser::getNickname, keyword)
                                .or()
                                .like(SysUser::getPhone, keyword))
                        .orderByAsc(SysUser::getId));
        return PageResult.of(page, u -> BeanUtil.copyProperties(u, UserInfoVO.class));
    }

    /** 生成唯一的圈子邀请码 */
    private String generateInviteCode() {
        for (int i = 0; i < 10; i++) {
            String code = "C" + RandomUtil.randomStringUpper(6);
            Long count = circleMapper.selectCount(new LambdaQueryWrapper<Circle>()
                    .eq(Circle::getInviteCode, code));
            if (count == 0) {
                return code;
            }
        }
        throw new BusinessException("邀请码生成失败，请重试");
    }

    // ================= 打卡规则 =================

    @Override
    public List<CheckinRule> ruleList(Long circleId) {
        return checkinRuleMapper.selectList(new LambdaQueryWrapper<CheckinRule>()
                .eq(circleId != null, CheckinRule::getCircleId, circleId)
                .orderByAsc(CheckinRule::getId));
    }

    @Override
    public Long saveRule(RuleDTO dto) {
        Long count = checkinRuleMapper.selectCount(new LambdaQueryWrapper<CheckinRule>()
                .eq(CheckinRule::getCircleId, dto.getCircleId())
                .eq(CheckinRule::getPosition, dto.getPosition()));
        if (count > 0) {
            throw new BusinessException("该职位规则已存在");
        }
        CheckinRule rule = BeanUtil.copyProperties(dto, CheckinRule.class);
        checkinRuleMapper.insert(rule);
        return rule.getId();
    }

    @Override
    public void updateRule(RuleDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("规则ID不能为空");
        }
        checkinRuleMapper.updateById(BeanUtil.copyProperties(dto, CheckinRule.class));
    }

    @Override
    public void deleteRule(Long id) {
        checkinRuleMapper.deleteById(id);
    }

    // ================= 打卡模板 =================

    @Override
    public List<CheckinTemplate> templateList(Long circleId) {
        return checkinTemplateMapper.selectList(new LambdaQueryWrapper<CheckinTemplate>()
                .eq(circleId != null, CheckinTemplate::getCircleId, circleId)
                .orderByAsc(CheckinTemplate::getId));
    }

    @Override
    public Long saveTemplate(TemplateDTO dto) {
        CheckinTemplate template = BeanUtil.copyProperties(dto, CheckinTemplate.class);
        checkinTemplateMapper.insert(template);
        disableOtherTemplates(template);
        return template.getId();
    }

    @Override
    public void updateTemplate(TemplateDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("模板ID不能为空");
        }
        CheckinTemplate template = BeanUtil.copyProperties(dto, CheckinTemplate.class);
        checkinTemplateMapper.updateById(template);
        disableOtherTemplates(template);
    }

    /** 同一读书圈只保留一个启用的打卡模板，打卡页按该模板渲染 */
    private void disableOtherTemplates(CheckinTemplate template) {
        if (template.getCircleId() == null || template.getStatus() == null || template.getStatus() != 1) {
            return;
        }
        checkinTemplateMapper.update(null, new LambdaUpdateWrapper<CheckinTemplate>()
                .eq(CheckinTemplate::getCircleId, template.getCircleId())
                .eq(CheckinTemplate::getStatus, 1)
                .ne(CheckinTemplate::getId, template.getId())
                .set(CheckinTemplate::getStatus, 0));
    }

    @Override
    public void deleteTemplate(Long id) {
        checkinTemplateMapper.deleteById(id);
    }

    // ================= 导出/导入 =================

    @Override
    public void exportCheckin(Long circleId, String startDate, String endDate, HttpServletResponse response) {
        QueryWrapper<CheckinRecord> qw = new QueryWrapper<>();
        qw.select("*")
                .eq(circleId != null, "circle_id", circleId)
                .ge(StrUtil.isNotBlank(startDate), "checkin_date", startDate)
                .le(StrUtil.isNotBlank(endDate), "checkin_date", endDate)
                .eq("status", 1)
                .orderByDesc("checkin_date")
                .last("LIMIT 10000");
        List<CheckinRecord> records = checkinRecordMapper.selectList(qw);

        List<Long> userIds = records.stream().map(CheckinRecord::getUserId).distinct().toList();
        List<Long> bookIds = records.stream().map(CheckinRecord::getBookId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> userNames = userIds.isEmpty() ? Map.of()
                : sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getNickname));
        Map<Long, String> bookTitles = bookIds.isEmpty() ? Map.of()
                : bookMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, Book::getTitle));

        List<CheckinExportVO> rows = records.stream().map(r -> {
            CheckinExportVO vo = new CheckinExportVO();
            vo.setId(r.getId());
            vo.setNickname(userNames.getOrDefault(r.getUserId(), ""));
            vo.setBookTitle(bookTitles.getOrDefault(r.getBookId(), ""));
            vo.setChapterTitle(r.getChapterTitle());
            CheckinTypeEnum type = CheckinTypeEnum.of(r.getCheckinType());
            vo.setCheckinType(type == null ? "未知" : type.getDesc());
            vo.setContent(r.getContent());
            vo.setCheckinDate(r.getCheckinDate() == null ? "" : r.getCheckinDate().toString());
            vo.setCreateTime(r.getCreateTime() == null ? "" : DateUtil.format(r.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            return vo;
        }).toList();

        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("打卡记录" + DateUtil.today(), StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), CheckinExportVO.class)
                    .sheet("打卡记录")
                    .doWrite(rows);
        } catch (IOException e) {
            throw new BusinessException("导出失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer importUsers(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        List<UserImportDTO> rows;
        try {
            rows = EasyExcel.read(file.getInputStream()).head(UserImportDTO.class).sheet().doReadSync();
        } catch (IOException e) {
            throw new BusinessException("读取Excel失败");
        }
        if (rows == null || rows.isEmpty()) {
            throw new BusinessException("Excel中没有数据");
        }
        String defaultPwd = BCrypt.hashpw(SystemConstant.DEFAULT_PASSWORD);
        int success = 0;
        for (UserImportDTO row : rows) {
            if (StrUtil.isBlank(row.getUsername())) {
                continue;
            }
            Long exists = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, row.getUsername()));
            if (exists > 0) {
                continue;
            }
            SysUser user = new SysUser();
            user.setUsername(row.getUsername().trim());
            user.setPassword(defaultPwd);
            user.setNickname(StrUtil.blankToDefault(row.getNickname(), row.getUsername().trim()));
            user.setPhone(row.getPhone());
            user.setPosition(StrUtil.blankToDefault(row.getPosition(), "普通员工"));
            user.setRole(RoleEnum.MEMBER.name());
            user.setGender(0);
            user.setPoints(0);
            user.setCheckinDays(0);
            user.setStatus(1);
            sysUserMapper.insert(user);
            success++;
        }
        return success;
    }

    // ================= 账号管理 =================

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setStatus(status);
        sysUserMapper.updateById(update);
        if (status != null && status == 0) {
            StpUtil.kickout(userId);
        }
    }

    @Override
    public void updateUserPosition(Long userId, String position) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .set(SysUser::getPosition, StrUtil.trimToNull(position)));
    }

    @Override
    public List<CircleBriefVO> userCircles(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        List<UserCircle> memberships = userCircleMapper.selectList(new LambdaQueryWrapper<UserCircle>()
                .eq(UserCircle::getUserId, userId)
                .orderByDesc(UserCircle::getCreateTime)
                .orderByDesc(UserCircle::getId));
        if (memberships.isEmpty()) {
            return List.of();
        }
        List<Long> circleIds = memberships.stream().map(UserCircle::getCircleId).toList();
        Map<Long, Circle> circleMap = circleMapper.selectBatchIds(circleIds).stream()
                .collect(Collectors.toMap(Circle::getId, c -> c));
        return memberships.stream()
                .map(membership -> {
                    Circle circle = circleMap.get(membership.getCircleId());
                    if (circle == null) {
                        return null;
                    }
                    CircleBriefVO vo = new CircleBriefVO();
                    vo.setId(circle.getId());
                    vo.setName(circle.getName());
                    vo.setCover(circle.getCover());
                    vo.setDescription(circle.getDescription());
                    vo.setMemberCount(circle.getMemberCount());
                    vo.setJoined(true);
                    vo.setCurrent(membership.getCircleId().equals(user.getCircleId()));
                    return vo;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void cancelAccount() {
        Long userId = StpUtil.getLoginIdAsLong();
        sysUserMapper.deleteById(userId);
        StpUtil.logout(userId);
    }

    // ================= 数据管理：书籍 =================

    @Override
    public PageResult<Book> dataBookPage(String keyword, String category, Integer status, long current, long size) {
        Page<Book> page = bookMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Book>()
                        .eq(StrUtil.isNotBlank(category), Book::getCategory, category)
                        .eq(status != null, Book::getStatus, status)
                        .and(StrUtil.isNotBlank(keyword), w -> w.like(Book::getTitle, keyword)
                                .or()
                                .like(Book::getAuthor, keyword))
                        .orderByAsc(Book::getSort)
                        .orderByDesc(Book::getId));
        return PageResult.of(page);
    }

    @Override
    public Long saveBook(BookDTO dto) {
        Long count = bookMapper.selectCount(new LambdaQueryWrapper<Book>()
                .eq(Book::getTitle, dto.getTitle().trim()));
        if (count > 0) {
            throw new BusinessException("书籍名称已存在");
        }
        Book book = BeanUtil.copyProperties(dto, Book.class);
        book.setTitle(dto.getTitle().trim());
        book.setBookType(book.getBookType() == null ? 1 : book.getBookType());
        book.setIsPublic(book.getIsPublic() == null ? 1 : book.getIsPublic());
        book.setStatus(book.getStatus() == null ? 1 : book.getStatus());
        book.setSort(book.getSort() == null ? 0 : book.getSort());
        book.setChapterCount(0);
        book.setCheckinCount(0);
        bookMapper.insert(book);
        return book.getId();
    }

    @Override
    public void updateBook(BookDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("书籍ID不能为空");
        }
        Book exist = bookMapper.selectById(dto.getId());
        if (exist == null) {
            throw new BusinessException("书籍不存在");
        }
        Long count = bookMapper.selectCount(new LambdaQueryWrapper<Book>()
                .eq(Book::getTitle, dto.getTitle().trim())
                .ne(Book::getId, dto.getId()));
        if (count > 0) {
            throw new BusinessException("书籍名称已存在");
        }
        Book book = BeanUtil.copyProperties(dto, Book.class);
        book.setTitle(dto.getTitle().trim());
        bookMapper.updateById(book);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBook(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException("书籍不存在");
        }
        bookChapterMapper.delete(new LambdaQueryWrapper<BookChapter>().eq(BookChapter::getBookId, id));
        bookMapper.deleteById(id);
    }

    // ================= 数据管理：章节 =================

    @Override
    public List<BookChapter> chapterList(Long bookId) {
        if (bookId == null) {
            throw new BusinessException("书籍不能为空");
        }
        return bookChapterMapper.selectList(new LambdaQueryWrapper<BookChapter>()
                .eq(BookChapter::getBookId, bookId)
                .orderByAsc(BookChapter::getChapterNo)
                .orderByAsc(BookChapter::getId));
    }

    @Override
    public Long saveChapter(ChapterDTO dto) {
        if (bookMapper.selectById(dto.getBookId()) == null) {
            throw new BusinessException("书籍不存在");
        }
        BookChapter chapter = BeanUtil.copyProperties(dto, BookChapter.class);
        chapter.setTitle(dto.getTitle().trim());
        if (chapter.getChapterNo() == null) {
            Long count = bookChapterMapper.selectCount(new LambdaQueryWrapper<BookChapter>()
                    .eq(BookChapter::getBookId, dto.getBookId()));
            chapter.setChapterNo(count.intValue() + 1);
        }
        bookChapterMapper.insert(chapter);
        refreshChapterCount(dto.getBookId());
        return chapter.getId();
    }

    @Override
    public void updateChapter(ChapterDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("章节ID不能为空");
        }
        BookChapter exist = bookChapterMapper.selectById(dto.getId());
        if (exist == null) {
            throw new BusinessException("章节不存在");
        }
        BookChapter chapter = BeanUtil.copyProperties(dto, BookChapter.class);
        chapter.setTitle(dto.getTitle().trim());
        bookChapterMapper.updateById(chapter);
    }

    @Override
    public void deleteChapter(Long id) {
        BookChapter chapter = bookChapterMapper.selectById(id);
        if (chapter == null) {
            throw new BusinessException("章节不存在");
        }
        bookChapterMapper.deleteById(id);
        refreshChapterCount(chapter.getBookId());
    }

    private void refreshChapterCount(Long bookId) {
        Long count = bookChapterMapper.selectCount(new LambdaQueryWrapper<BookChapter>()
                .eq(BookChapter::getBookId, bookId));
        bookMapper.update(null, new LambdaUpdateWrapper<Book>()
                .eq(Book::getId, bookId)
                .set(Book::getChapterCount, count.intValue()));
    }

    // ================= 数据管理：用户 =================

    @Override
    public PageResult<UserInfoVO> dataUserPage(String keyword, Long circleId, String role, long current, long size) {
        Page<SysUser> page = sysUserMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<SysUser>()
                        .eq(circleId != null, SysUser::getCircleId, circleId)
                        .eq(StrUtil.isNotBlank(role), SysUser::getRole, role)
                        .and(StrUtil.isNotBlank(keyword), w -> w.like(SysUser::getUsername, keyword)
                                .or()
                                .like(SysUser::getNickname, keyword)
                                .or()
                                .like(SysUser::getPhone, keyword))
                        .orderByDesc(SysUser::getId));
        return PageResult.of(page, u -> BeanUtil.copyProperties(u, UserInfoVO.class));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveUser(AdminUserDTO dto) {
        if (StrUtil.isBlank(dto.getUsername())) {
            throw new BusinessException("登录账号不能为空");
        }
        String username = dto.getUsername().trim();
        Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (count > 0) {
            throw new BusinessException("登录账号已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(BCrypt.hashpw(StrUtil.blankToDefault(dto.getPassword(), SystemConstant.DEFAULT_PASSWORD)));
        user.setNickname(StrUtil.blankToDefault(StrUtil.trim(dto.getNickname()), username));
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender() == null ? 0 : dto.getGender());
        user.setRole(StrUtil.blankToDefault(dto.getRole(), RoleEnum.MEMBER.name()));
        user.setPosition(StrUtil.blankToDefault(StrUtil.trim(dto.getPosition()), "普通员工"));
        user.setCircleId(dto.getCircleId());
        user.setGroupId(dto.getGroupId());
        user.setPoints(0);
        user.setCheckinDays(0);
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        sysUserMapper.insert(user);
        if (user.getCircleId() != null) {
            addMembership(user.getId(), user.getCircleId(), user.getGroupId());
        }
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(AdminUserDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        SysUser exist = sysUserMapper.selectById(dto.getId());
        if (exist == null) {
            throw new BusinessException("用户不存在");
        }
        SysUser update = new SysUser();
        update.setId(dto.getId());
        if (StrUtil.isNotBlank(dto.getUsername()) && !dto.getUsername().trim().equals(exist.getUsername())) {
            Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, dto.getUsername().trim())
                    .ne(SysUser::getId, dto.getId()));
            if (count > 0) {
                throw new BusinessException("登录账号已存在");
            }
            update.setUsername(dto.getUsername().trim());
        }
        update.setNickname(StrUtil.trim(dto.getNickname()));
        update.setPhone(dto.getPhone());
        update.setGender(dto.getGender());
        update.setRole(dto.getRole());
        update.setPosition(StrUtil.trim(dto.getPosition()));
        update.setStatus(dto.getStatus());
        sysUserMapper.updateById(update);

        boolean circleChanged = !Objects.equals(exist.getCircleId(), dto.getCircleId());
        boolean groupChanged = !Objects.equals(exist.getGroupId(), dto.getGroupId());
        if (circleChanged) {
            if (exist.getCircleId() != null) {
                removeMembership(exist.getCircleId(), dto.getId());
            }
            sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                    .eq(SysUser::getId, dto.getId())
                    .set(SysUser::getCircleId, dto.getCircleId())
                    .set(SysUser::getGroupId, dto.getGroupId()));
            if (dto.getCircleId() != null) {
                addMembership(dto.getId(), dto.getCircleId(), dto.getGroupId());
            }
        } else if (groupChanged && dto.getCircleId() != null) {
            UserCircle membership = userCircleMapper.selectOne(new LambdaQueryWrapper<UserCircle>()
                    .eq(UserCircle::getUserId, dto.getId())
                    .eq(UserCircle::getCircleId, dto.getCircleId()));
            if (membership != null) {
                if (membership.getGroupId() != null) {
                    tGroupMapper.update(null, new LambdaUpdateWrapper<TGroup>()
                            .eq(TGroup::getId, membership.getGroupId())
                            .setSql("member_count = GREATEST(member_count - 1, 0)"));
                }
                membership.setGroupId(dto.getGroupId());
                userCircleMapper.updateById(membership);
            }
            if (dto.getGroupId() != null) {
                tGroupMapper.update(null, new LambdaUpdateWrapper<TGroup>()
                        .eq(TGroup::getId, dto.getGroupId())
                        .setSql("member_count = member_count + 1"));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        if (id == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (id.equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException("不能删除当前登录账号");
        }
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getCircleId() != null) {
            removeMembership(user.getCircleId(), id);
        }
        sysUserMapper.deleteById(id);
        StpUtil.kickout(id);
    }

    private void addMembership(Long userId, Long circleId, Long groupId) {
        Circle circle = circleMapper.selectById(circleId);
        if (circle == null) {
            throw new BusinessException("读书圈不存在");
        }
        if (groupId != null) {
            TGroup group = tGroupMapper.selectById(groupId);
            if (group == null || !circleId.equals(group.getCircleId())) {
                throw new BusinessException("小组不属于当前读书圈");
            }
        }
        UserCircle exists = userCircleMapper.selectOne(new LambdaQueryWrapper<UserCircle>()
                .eq(UserCircle::getUserId, userId)
                .eq(UserCircle::getCircleId, circleId));
        if (exists != null) {
            return;
        }
        UserCircle membership = new UserCircle();
        membership.setUserId(userId);
        membership.setCircleId(circleId);
        membership.setGroupId(groupId);
        userCircleMapper.insert(membership);
        circleMapper.update(null, new LambdaUpdateWrapper<Circle>()
                .eq(Circle::getId, circleId)
                .setSql("member_count = member_count + 1"));
        if (groupId != null) {
            tGroupMapper.update(null, new LambdaUpdateWrapper<TGroup>()
                    .eq(TGroup::getId, groupId)
                    .setSql("member_count = member_count + 1"));
        }
    }

    private void removeMembership(Long circleId, Long userId) {
        UserCircle membership = userCircleMapper.selectOne(new LambdaQueryWrapper<UserCircle>()
                .eq(UserCircle::getUserId, userId)
                .eq(UserCircle::getCircleId, circleId));
        if (membership == null) {
            return;
        }
        if (membership.getGroupId() != null) {
            tGroupMapper.update(null, new LambdaUpdateWrapper<TGroup>()
                    .eq(TGroup::getId, membership.getGroupId())
                    .setSql("member_count = GREATEST(member_count - 1, 0)"));
        }
        circleMapper.update(null, new LambdaUpdateWrapper<Circle>()
                .eq(Circle::getId, circleId)
                .setSql("member_count = GREATEST(member_count - 1, 0)"));
        userCircleMapper.deleteById(membership.getId());
    }

    // ================= 数据管理：打卡记录 =================

    @Override
    public PageResult<AdminCheckinVO> dataCheckinPage(String keyword, Long userId, Long circleId, String startDate,
                                                      String endDate, Integer status, long current, long size) {
        List<Long> matchedUserIds = null;
        if (StrUtil.isNotBlank(keyword)) {
            matchedUserIds = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                            .and(w -> w.like(SysUser::getUsername, keyword)
                                    .or()
                                    .like(SysUser::getNickname, keyword)
                                    .or()
                                    .like(SysUser::getPhone, keyword)))
                    .stream()
                    .map(SysUser::getId)
                    .toList();
            if (matchedUserIds.isEmpty()) {
                return PageResult.empty(current, size);
            }
        }
        IPage<CheckinRecord> page = checkinRecordMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<CheckinRecord>()
                        .in(matchedUserIds != null, CheckinRecord::getUserId, matchedUserIds)
                        .eq(userId != null, CheckinRecord::getUserId, userId)
                        .eq(circleId != null, CheckinRecord::getCircleId, circleId)
                        .ge(StrUtil.isNotBlank(startDate), CheckinRecord::getCheckinDate, startDate)
                        .le(StrUtil.isNotBlank(endDate), CheckinRecord::getCheckinDate, endDate)
                        .eq(status != null, CheckinRecord::getStatus, status)
                        .orderByDesc(CheckinRecord::getCheckinDate)
                        .orderByDesc(CheckinRecord::getId));
        List<CheckinRecord> records = page.getRecords();
        List<Long> userIds = records.stream().map(CheckinRecord::getUserId).filter(Objects::nonNull).distinct().toList();
        List<Long> bookIds = records.stream().map(CheckinRecord::getBookId).filter(Objects::nonNull).distinct().toList();
        Map<Long, String> userNames = userIds.isEmpty() ? Map.of()
                : sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> StrUtil.blankToDefault(u.getNickname(), u.getUsername())));
        Map<Long, String> bookTitles = bookIds.isEmpty() ? Map.of()
                : bookMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, Book::getTitle));
        return PageResult.of(page, r -> {
            AdminCheckinVO vo = BeanUtil.copyProperties(r, AdminCheckinVO.class);
            vo.setNickname(userNames.get(r.getUserId()));
            vo.setBookTitle(bookTitles.get(r.getBookId()));
            return vo;
        });
    }

    @Override
    public void updateCheckin(CheckinEditDTO dto) {
        CheckinRecord exist = checkinRecordMapper.selectById(dto.getId());
        if (exist == null) {
            throw new BusinessException("打卡记录不存在");
        }
        SysUser user = sysUserMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException("打卡成员不存在");
        }
        CheckinRecord update = new CheckinRecord();
        update.setId(dto.getId());
        update.setUserId(dto.getUserId());
        update.setCircleId(user.getCircleId());
        update.setGroupId(user.getGroupId());
        update.setBookId(dto.getBookId());
        update.setChapterId(dto.getChapterId());
        update.setChapterTitle(dto.getChapterTitle());
        if (dto.getChapterId() != null) {
            BookChapter chapter = bookChapterMapper.selectById(dto.getChapterId());
            if (chapter != null) {
                update.setChapterTitle(chapter.getTitle());
            }
        }
        update.setCheckinType(dto.getCheckinType());
        update.setContent(dto.getContent());
        update.setMediaUrl(dto.getMediaUrl());
        update.setAudioUrl(dto.getAudioUrl());
        update.setAudioDuration(dto.getAudioDuration());
        update.setCheckinDate(dto.getCheckinDate() == null ? exist.getCheckinDate() : dto.getCheckinDate());
        update.setStatus(dto.getStatus());
        update.setIsPublic(dto.getIsPublic());
        update.setAuditRemark(dto.getAuditRemark());
        update.setAuditUserId(StpUtil.getLoginIdAsLong());
        checkinRecordMapper.updateById(update);
    }

    @Override
    public void deleteCheckin(Long id) {
        CheckinRecord record = checkinRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("打卡记录不存在");
        }
        checkinRecordMapper.deleteById(id);
    }

    // ================= 职位管理 =================

    @Override
    public List<PositionVO> positionList(String keyword) {
        List<SysPosition> positions = sysPositionMapper.selectList(new LambdaQueryWrapper<SysPosition>()
                .like(StrUtil.isNotBlank(keyword), SysPosition::getName, keyword)
                .orderByAsc(SysPosition::getSort)
                .orderByAsc(SysPosition::getId));
        if (positions.isEmpty()) {
            return List.of();
        }
        Map<String, Long> userCounts = countByPosition(sysUserMapper.selectMaps(new QueryWrapper<SysUser>()
                .select("position", "COUNT(*) AS cnt")
                .isNotNull("position")
                .groupBy("position")));
        Map<String, Long> ruleCounts = countByPosition(checkinRuleMapper.selectMaps(new QueryWrapper<CheckinRule>()
                .select("position", "COUNT(*) AS cnt")
                .isNotNull("position")
                .groupBy("position")));
        return positions.stream().map(p -> {
            PositionVO vo = BeanUtil.copyProperties(p, PositionVO.class);
            vo.setUserCount(userCounts.getOrDefault(p.getName(), 0L));
            vo.setRuleCount(ruleCounts.getOrDefault(p.getName(), 0L));
            return vo;
        }).toList();
    }

    private Map<String, Long> countByPosition(List<Map<String, Object>> rows) {
        Map<String, Long> result = new HashMap<>();
        if (rows == null) {
            return result;
        }
        for (Map<String, Object> row : rows) {
            Object name = row.get("position");
            Object count = row.get("cnt");
            if (name == null || count == null) {
                continue;
            }
            result.put(name.toString(), Long.parseLong(count.toString()));
        }
        return result;
    }

    @Override
    public Long savePosition(PositionDTO dto) {
        String name = dto.getName().trim();
        Long count = sysPositionMapper.selectCount(new LambdaQueryWrapper<SysPosition>()
                .eq(SysPosition::getName, name));
        if (count > 0) {
            throw new BusinessException("职位名称已存在");
        }
        SysPosition position = new SysPosition();
        position.setName(name);
        position.setDescription(StrUtil.blankToDefault(dto.getDescription(), ""));
        position.setSort(dto.getSort() == null ? 0 : dto.getSort());
        position.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        sysPositionMapper.insert(position);
        return position.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePosition(PositionDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("职位ID不能为空");
        }
        SysPosition exist = sysPositionMapper.selectById(dto.getId());
        if (exist == null) {
            throw new BusinessException("职位不存在");
        }
        String name = dto.getName().trim();
        Long count = sysPositionMapper.selectCount(new LambdaQueryWrapper<SysPosition>()
                .eq(SysPosition::getName, name)
                .ne(SysPosition::getId, dto.getId()));
        if (count > 0) {
            throw new BusinessException("职位名称已存在");
        }
        SysPosition update = new SysPosition();
        update.setId(dto.getId());
        update.setName(name);
        update.setDescription(StrUtil.blankToDefault(dto.getDescription(), ""));
        update.setSort(dto.getSort());
        update.setStatus(dto.getStatus());
        sysPositionMapper.updateById(update);
        // 职位改名时同步用户与打卡规则中的职位名称
        if (!name.equals(exist.getName())) {
            sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                    .eq(SysUser::getPosition, exist.getName())
                    .set(SysUser::getPosition, name));
            checkinRuleMapper.update(null, new LambdaUpdateWrapper<CheckinRule>()
                    .eq(CheckinRule::getPosition, exist.getName())
                    .set(CheckinRule::getPosition, name));
        }
    }

    @Override
    public void deletePosition(Long id) {
        SysPosition position = sysPositionMapper.selectById(id);
        if (position == null) {
            throw new BusinessException("职位不存在");
        }
        Long userCount = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPosition, position.getName()));
        Long ruleCount = checkinRuleMapper.selectCount(new LambdaQueryWrapper<CheckinRule>()
                .eq(CheckinRule::getPosition, position.getName()));
        if (userCount > 0 || ruleCount > 0) {
            throw new BusinessException("该职位仍被使用（用户 " + userCount + " 人 / 规则 " + ruleCount
                    + " 条），请先调整后再删除");
        }
        sysPositionMapper.deleteById(id);
    }
}
