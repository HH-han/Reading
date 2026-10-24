package com.readingsystem.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.readingsystem.common.PageResult;
import com.readingsystem.entity.Circle;
import com.readingsystem.entity.StudyJob;
import com.readingsystem.entity.SysUser;
import com.readingsystem.entity.TGroup;
import com.readingsystem.entity.UserCircle;
import com.readingsystem.exception.BusinessException;
import com.readingsystem.mapper.CircleMapper;
import com.readingsystem.mapper.StudyJobMapper;
import com.readingsystem.mapper.SysUserMapper;
import com.readingsystem.mapper.TGroupMapper;
import com.readingsystem.mapper.UserCircleMapper;
import com.readingsystem.service.CircleService;
import com.readingsystem.vo.CircleBriefVO;
import com.readingsystem.vo.CircleCurrentVO;
import com.readingsystem.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 圈子/小组服务
 */
@Service
@RequiredArgsConstructor
public class CircleServiceImpl implements CircleService {

    private final CircleMapper circleMapper;
    private final TGroupMapper tGroupMapper;
    private final SysUserMapper sysUserMapper;
    private final StudyJobMapper studyJobMapper;
    private final UserCircleMapper userCircleMapper;

    @Override
    public CircleCurrentVO current() {
        SysUser user = currentUser();
        UserCircle membership = activeMembership(user);
        if (membership == null) {
            throw new BusinessException("您还未加入学习圈");
        }
        Circle circle = circleMapper.selectById(membership.getCircleId());
        if (circle == null) {
            throw new BusinessException("圈子不存在");
        }
        CircleCurrentVO vo = new CircleCurrentVO();
        vo.setCircle(circle);
        vo.setMemberCount(circle.getMemberCount());
        if (membership.getGroupId() != null) {
            vo.setGroup(tGroupMapper.selectById(membership.getGroupId()));
        }
        return vo;
    }

    @Override
    public List<TGroup> groups() {
        UserCircle membership = activeMembership(currentUser());
        if (membership == null) {
            return List.of();
        }
        return tGroupMapper.selectList(new LambdaQueryWrapper<TGroup>()
                .eq(TGroup::getCircleId, membership.getCircleId())
                .eq(TGroup::getStatus, 1)
                .orderByAsc(TGroup::getId));
    }

    @Override
    public PageResult<UserInfoVO> members(Long groupId, long current, long size) {
        UserCircle membership = activeMembership(currentUser());
        if (membership == null) {
            throw new BusinessException("您还未加入学习圈");
        }
        IPage<SysUser> page = userCircleMapper.pageMembers(new Page<>(current, size),
                membership.getCircleId(), groupId, null);
        return PageResult.of(page, u -> BeanUtil.copyProperties(u, UserInfoVO.class));
    }

    @Override
    public PageResult<StudyJob> jobPage(long current, long size) {
        UserCircle membership = activeMembership(currentUser());
        if (membership == null) {
            return PageResult.empty(current, size);
        }
        Page<StudyJob> page = studyJobMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<StudyJob>()
                        .eq(StudyJob::getCircleId, membership.getCircleId())
                        // 仅展示已发布/已结束的作业，草稿对成员不可见
                        .in(StudyJob::getStatus, 1, 2)
                        // 全圈作业或指定到本小组的作业
                        .and(w -> w.isNull(StudyJob::getGroupId)
                                .or()
                                .eq(membership.getGroupId() != null, StudyJob::getGroupId, membership.getGroupId()))
                        .orderByDesc(StudyJob::getCreateTime));
        return PageResult.of(page);
    }

    @Override
    public List<CircleBriefVO> available() {
        SysUser user = currentUser();
        Set<Long> joinedIds = membershipCircleIds(user.getId());
        List<Circle> circles = circleMapper.selectList(new LambdaQueryWrapper<Circle>()
                .eq(Circle::getStatus, 1)
                .orderByDesc(Circle::getId));
        return circles.stream().map(c -> {
            CircleBriefVO vo = toBrief(c);
            vo.setJoined(joinedIds.contains(c.getId()));
            vo.setCurrent(c.getId().equals(user.getCircleId()));
            return vo;
        }).toList();
    }

    @Override
    public List<CircleBriefVO> mine() {
        SysUser user = currentUser();
        List<UserCircle> memberships = userCircleMapper.selectList(new LambdaQueryWrapper<UserCircle>()
                .eq(UserCircle::getUserId, user.getId())
                .orderByDesc(UserCircle::getCreateTime)
                .orderByDesc(UserCircle::getId));
        if (memberships.isEmpty()) {
            return List.of();
        }
        List<Long> circleIds = memberships.stream().map(UserCircle::getCircleId).toList();
        Map<Long, Circle> circleMap = circleMapper.selectBatchIds(circleIds).stream()
                .collect(Collectors.toMap(Circle::getId, Function.identity()));
        return memberships.stream()
                .map(uc -> {
                    Circle circle = circleMap.get(uc.getCircleId());
                    if (circle == null) {
                        return null;
                    }
                    CircleBriefVO vo = toBrief(circle);
                    vo.setJoined(true);
                    vo.setCurrent(uc.getCircleId().equals(user.getCircleId()));
                    return vo;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void join(String inviteCode) {
        SysUser user = currentUser();
        // 先查小组邀请码
        TGroup group = tGroupMapper.selectOne(new LambdaQueryWrapper<TGroup>()
                .eq(TGroup::getInviteCode, inviteCode)
                .eq(TGroup::getStatus, 1));
        if (group != null) {
            joinGroup(user, group);
            return;
        }
        // 再查圈子邀请码
        Circle circle = circleMapper.selectOne(new LambdaQueryWrapper<Circle>()
                .eq(Circle::getInviteCode, inviteCode)
                .eq(Circle::getStatus, 1));
        if (circle == null) {
            throw new BusinessException("邀请码无效");
        }
        joinCircle(user, circle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void joinById(Long circleId) {
        SysUser user = currentUser();
        Circle circle = circleMapper.selectById(circleId);
        if (circle == null || circle.getStatus() == null || circle.getStatus() != 1) {
            throw new BusinessException("读书圈不存在或已停用");
        }
        joinCircle(user, circle);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void switchTo(Long circleId) {
        SysUser user = currentUser();
        UserCircle membership = findMembership(user.getId(), circleId);
        if (membership == null) {
            throw new BusinessException("您还未加入该学习圈");
        }
        if (!circleId.equals(user.getCircleId())) {
            syncActive(user, membership);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exit() {
        UserCircle membership = activeMembership(currentUser());
        if (membership == null) {
            throw new BusinessException("您还未加入学习圈");
        }
        removeMembership(membership);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void exit(Long circleId) {
        SysUser user = currentUser();
        UserCircle membership = findMembership(user.getId(), circleId);
        if (membership == null) {
            throw new BusinessException("您还未加入该学习圈");
        }
        removeMembership(membership);
    }

    /** 加入圈子：已是成员则仅切换为当前圈子 */
    private void joinCircle(SysUser user, Circle circle) {
        UserCircle membership = findMembership(user.getId(), circle.getId());
        if (membership != null) {
            if (circle.getId().equals(user.getCircleId())) {
                throw new BusinessException("您已在该学习圈中");
            }
            syncActive(user, membership);
            return;
        }
        UserCircle created = new UserCircle();
        created.setUserId(user.getId());
        created.setCircleId(circle.getId());
        userCircleMapper.insert(created);
        addCircleMember(circle.getId());
        syncActive(user, created);
    }

    /** 通过小组邀请码加入：小组与圈子都会记录为成员关系 */
    private void joinGroup(SysUser user, TGroup group) {
        UserCircle membership = findMembership(user.getId(), group.getCircleId());
        if (membership == null) {
            UserCircle created = new UserCircle();
            created.setUserId(user.getId());
            created.setCircleId(group.getCircleId());
            created.setGroupId(group.getId());
            userCircleMapper.insert(created);
            addCircleMember(group.getCircleId());
            addGroupMember(group.getId());
            syncActive(user, created);
            return;
        }
        if (group.getId().equals(membership.getGroupId())) {
            throw new BusinessException("您已在该小组中");
        }
        if (membership.getGroupId() != null) {
            reduceGroupMember(membership.getGroupId());
        }
        membership.setGroupId(group.getId());
        userCircleMapper.updateById(membership);
        addGroupMember(group.getId());
        syncActive(user, membership);
    }

    /** 退出成员关系，并在退出的是当前圈子时自动切换到其它已加入的圈子 */
    private void removeMembership(UserCircle membership) {
        SysUser user = currentUser();
        if (membership.getGroupId() != null) {
            reduceGroupMember(membership.getGroupId());
        }
        reduceCircleMember(membership.getCircleId());
        userCircleMapper.deleteById(membership.getId());
        if (membership.getCircleId().equals(user.getCircleId())) {
            List<UserCircle> rest = userCircleMapper.selectList(new LambdaQueryWrapper<UserCircle>()
                    .eq(UserCircle::getUserId, user.getId())
                    .orderByDesc(UserCircle::getId)
                    .last("LIMIT 1"));
            syncActive(user, rest.isEmpty() ? null : rest.get(0));
        }
    }

    /** 取当前生效的圈子成员关系，必要时修正用户表里的冗余字段 */
    private UserCircle activeMembership(SysUser user) {
        if (user.getCircleId() != null) {
            UserCircle membership = findMembership(user.getId(), user.getCircleId());
            if (membership != null) {
                return membership;
            }
        }
        List<UserCircle> list = userCircleMapper.selectList(new LambdaQueryWrapper<UserCircle>()
                .eq(UserCircle::getUserId, user.getId())
                .orderByDesc(UserCircle::getId)
                .last("LIMIT 1"));
        if (list.isEmpty()) {
            return null;
        }
        UserCircle membership = list.get(0);
        syncActive(user, membership);
        return membership;
    }

    private UserCircle findMembership(Long userId, Long circleId) {
        return userCircleMapper.selectOne(new LambdaQueryWrapper<UserCircle>()
                .eq(UserCircle::getUserId, userId)
                .eq(UserCircle::getCircleId, circleId));
    }

    private Set<Long> membershipCircleIds(Long userId) {
        return userCircleMapper.selectList(new LambdaQueryWrapper<UserCircle>()
                        .eq(UserCircle::getUserId, userId))
                .stream()
                .map(UserCircle::getCircleId)
                .collect(Collectors.toSet());
    }

    /** 同步用户表中的当前圈子/小组 */
    private void syncActive(SysUser user, UserCircle membership) {
        Long circleId = membership == null ? null : membership.getCircleId();
        Long groupId = membership == null ? null : membership.getGroupId();
        user.setCircleId(circleId);
        user.setGroupId(groupId);
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getCircleId, circleId)
                .set(SysUser::getGroupId, groupId));
    }

    private CircleBriefVO toBrief(Circle circle) {
        CircleBriefVO vo = new CircleBriefVO();
        vo.setId(circle.getId());
        vo.setName(circle.getName());
        vo.setCover(circle.getCover());
        vo.setDescription(circle.getDescription());
        vo.setMemberCount(circle.getMemberCount());
        return vo;
    }

    private void addCircleMember(Long circleId) {
        circleMapper.update(null, new LambdaUpdateWrapper<Circle>()
                .eq(Circle::getId, circleId)
                .setSql("member_count = member_count + 1"));
    }

    private void reduceCircleMember(Long circleId) {
        circleMapper.update(null, new LambdaUpdateWrapper<Circle>()
                .eq(Circle::getId, circleId)
                .setSql("member_count = GREATEST(member_count - 1, 0)"));
    }

    private void addGroupMember(Long groupId) {
        tGroupMapper.update(null, new LambdaUpdateWrapper<TGroup>()
                .eq(TGroup::getId, groupId)
                .setSql("member_count = member_count + 1"));
    }

    private void reduceGroupMember(Long groupId) {
        tGroupMapper.update(null, new LambdaUpdateWrapper<TGroup>()
                .eq(TGroup::getId, groupId)
                .setSql("member_count = GREATEST(member_count - 1, 0)"));
    }

    private SysUser currentUser() {
        SysUser user = sysUserMapper.selectById(StpUtil.getLoginIdAsLong());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
}
