package com.readingsystem.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.readingsystem.common.PageResult;
import com.readingsystem.dto.UserUpdateDTO;
import com.readingsystem.entity.*;
import com.readingsystem.exception.BusinessException;
import com.readingsystem.mapper.*;
import com.readingsystem.service.UserService;
import com.readingsystem.vo.CheckinDaysVO;
import com.readingsystem.vo.FavoriteVO;
import com.readingsystem.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final UserGrowthLogMapper growthLogMapper;
    private final UserFavoriteMapper favoriteMapper;
    private final BookMapper bookMapper;
    private final CheckinRecordMapper checkinRecordMapper;
    private final CheckinRuleMapper checkinRuleMapper;

    @Override
    public UserInfoVO info() {
        SysUser user = currentUser();
        return BeanUtil.copyProperties(user, UserInfoVO.class);
    }

    @Override
    public void updateInfo(UserUpdateDTO dto) {
        SysUser update = BeanUtil.copyProperties(dto, SysUser.class);
        update.setId(StpUtil.getLoginIdAsLong());
        sysUserMapper.updateById(update);
    }

    @Override
    public PageResult<UserGrowthLog> growthPage(long current, long size) {
        Page<UserGrowthLog> page = growthLogMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<UserGrowthLog>()
                        .eq(UserGrowthLog::getUserId, StpUtil.getLoginIdAsLong())
                        .orderByDesc(UserGrowthLog::getCreateTime));
        return PageResult.of(page);
    }

    @Override
    public PageResult<FavoriteVO> favoritesPage(long current, long size) {
        Page<UserFavorite> page = favoriteMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, StpUtil.getLoginIdAsLong())
                        .orderByDesc(UserFavorite::getCreateTime));
        List<UserFavorite> records = page.getRecords();
        // 预查书籍与打卡标题
        List<Long> bookIds = records.stream()
                .filter(f -> f.getTargetType() == 1)
                .map(UserFavorite::getTargetId).toList();
        List<Long> checkinIds = records.stream()
                .filter(f -> f.getTargetType() == 2)
                .map(UserFavorite::getTargetId).toList();
        Map<Long, Book> bookMap = bookIds.isEmpty() ? Map.of()
                : bookMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, b -> b));
        Map<Long, CheckinRecord> checkinMap = checkinIds.isEmpty() ? Map.of()
                : checkinRecordMapper.selectBatchIds(checkinIds).stream()
                .collect(Collectors.toMap(CheckinRecord::getId, c -> c));

        return PageResult.of(page, f -> {
            FavoriteVO vo = new FavoriteVO();
            vo.setId(f.getId());
            vo.setTargetType(f.getTargetType());
            vo.setTargetId(f.getTargetId());
            vo.setCreateTime(f.getCreateTime());
            if (f.getTargetType() == 1) {
                Book book = bookMap.get(f.getTargetId());
                if (book != null) {
                    vo.setTitle(book.getTitle());
                    vo.setCover(book.getCover());
                }
            } else {
                CheckinRecord record = checkinMap.get(f.getTargetId());
                if (record != null) {
                    String content = record.getContent();
                    vo.setTitle(content == null ? record.getChapterTitle()
                            : (content.length() > 30 ? content.substring(0, 30) : content));
                    String media = record.getMediaUrl();
                    vo.setCover(media == null || media.isBlank() ? "" : media.split(",")[0]);
                }
            }
            return vo;
        });
    }

    @Override
    public CheckinDaysVO checkinDays() {
        SysUser user = currentUser();
        Long userId = user.getId();
        CheckinDaysVO vo = new CheckinDaysVO();
        int weekDays = nullToZero(checkinRecordMapper.countWeekDays(userId));
        vo.setWeekDays(weekDays);
        vo.setMonthDays(nullToZero(checkinRecordMapper.countMonthDays(userId)));
        vo.setYearDays(nullToZero(checkinRecordMapper.countYearDays(userId)));

        // 职位对应规则
        Integer required = null;
        Integer requiredMonth = null;
        Integer requiredYear = null;
        if (user.getCircleId() != null && user.getPosition() != null) {
            CheckinRule rule = checkinRuleMapper.selectOne(new LambdaQueryWrapper<CheckinRule>()
                    .eq(CheckinRule::getCircleId, user.getCircleId())
                    .eq(CheckinRule::getPosition, user.getPosition())
                    .eq(CheckinRule::getStatus, 1)
                    .last("limit 1"));
            if (rule != null) {
                required = rule.getRequiredDaysPerWeek();
                requiredMonth = rule.getRequiredDaysPerMonth();
                requiredYear = rule.getRequiredDaysPerYear();
            }
        }
        vo.setRequiredDaysPerWeek(required);
        vo.setRequiredDaysPerMonth(requiredMonth);
        vo.setRequiredDaysPerYear(requiredYear);
        vo.setWeekQualified(required != null && weekDays >= required);
        return vo;
    }

    private int nullToZero(Integer v) {
        return v == null ? 0 : v;
    }

    private SysUser currentUser() {
        SysUser user = sysUserMapper.selectById(StpUtil.getLoginIdAsLong());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
}
