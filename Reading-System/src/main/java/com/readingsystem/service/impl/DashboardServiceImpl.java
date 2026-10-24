package com.readingsystem.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.readingsystem.entity.CheckinRecord;
import com.readingsystem.entity.Circle;
import com.readingsystem.entity.SysUser;
import com.readingsystem.exception.BusinessException;
import com.readingsystem.mapper.CheckinRecordMapper;
import com.readingsystem.mapper.CircleMapper;
import com.readingsystem.mapper.SysUserMapper;
import com.readingsystem.service.DashboardService;
import com.readingsystem.vo.DashboardTodayVO;
import com.readingsystem.vo.MyStatVO;
import com.readingsystem.vo.RankingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计看板服务
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CheckinRecordMapper checkinRecordMapper;
    private final SysUserMapper sysUserMapper;
    private final CircleMapper circleMapper;

    @Override
    public DashboardTodayVO today() {
        SysUser me = currentUserWithCircle();
        Long circleId = me.getCircleId();
        Map<String, Object> situation = checkinRecordMapper.todaySituation(circleId);

        Circle circle = circleMapper.selectById(circleId);
        long totalMembers = circle == null || circle.getMemberCount() == null ? 0 : circle.getMemberCount();
        long totalUsers = getLong(situation, "totalUsers");

        // 今日活跃小组数(按小组去重)
        Long activeGroups = checkinRecordMapper.countActiveGroups(circleId);

        DashboardTodayVO vo = new DashboardTodayVO();
        vo.setCheckinUsers(totalUsers);
        vo.setTotalMembers(totalMembers);
        vo.setCompleteRate(totalMembers == 0 ? 0D : Math.round(totalUsers * 10000.0 / totalMembers) / 100.0);
        vo.setActiveGroups(activeGroups);
        vo.setAudioUsers(getLong(situation, "audioUsers"));
        vo.setReadUsers(getLong(situation, "readUsers"));
        vo.setNoteUsers(getLong(situation, "noteUsers"));
        return vo;
    }

    @Override
    public List<Map<String, Object>> trend(Integer days) {
        SysUser me = currentUserWithCircle();
        List<Map<String, Object>> rows = checkinRecordMapper.checkinTrend(me.getCircleId(), days == null ? 30 : days);
        Circle circle = circleMapper.selectById(me.getCircleId());
        long totalMembers = circle == null || circle.getMemberCount() == null ? 0 : circle.getMemberCount();
        // 补充当日打卡率(0-100)
        for (Map<String, Object> row : rows) {
            Object users = row.get("checkinUsers");
            long checkinUsers = users == null ? 0 : ((Number) users).longValue();
            row.put("rate", totalMembers == 0 ? 0D : Math.round(checkinUsers * 10000.0 / totalMembers) / 100.0);
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> topGroups() {
        SysUser me = currentUserWithCircle();
        return checkinRecordMapper.groupWeekRank(me.getCircleId(), 5);
    }

    @Override
    public MyStatVO my() {
        SysUser me = currentUserWithCircle();
        MyStatVO vo = new MyStatVO();
        vo.setCheckinDays(me.getCheckinDays());

        // 本周个人打卡天数排行(圈子/小组)
        List<Map<String, Object>> circleRankRows = weekPersonRank(me.getCircleId(), null);
        vo.setCircleRank(rankOf(circleRankRows, me.getId()));
        vo.setGroupRank(me.getGroupId() == null ? null
                : rankOf(weekPersonRank(me.getCircleId(), me.getGroupId()), me.getId()));

        // 本周前三名
        List<Long> topIds = circleRankRows.stream().limit(3)
                .map(r -> ((Number) r.get("scopeId")).longValue()).toList();
        Map<Long, SysUser> userMap = topIds.isEmpty() ? Map.of()
                : sysUserMapper.selectBatchIds(topIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
        vo.setTopThree(circleRankRows.stream().limit(3).map(r -> {
            RankingVO item = new RankingVO();
            long uid = ((Number) r.get("scopeId")).longValue();
            item.setScopeId(uid);
            item.setScore(((Number) r.get("score")).doubleValue());
            SysUser u = userMap.get(uid);
            item.setName(u == null ? "未知用户" : u.getNickname());
            item.setAvatar(u == null ? "" : u.getAvatar());
            return item;
        }).collect(Collectors.toList()));
        // 补排名
        for (int i = 0; i < vo.getTopThree().size(); i++) {
            vo.getTopThree().get(i).setRankNo(i + 1);
        }
        return vo;
    }

    /** 本周个人打卡天数排行 */
    private List<Map<String, Object>> weekPersonRank(Long circleId, Long groupId) {
        QueryWrapper<CheckinRecord> qw = new QueryWrapper<>();
        qw.select("user_id AS scopeId", "COUNT(DISTINCT checkin_date) AS score")
                .eq("circle_id", circleId)
                .eq(groupId != null, "group_id", groupId)
                .eq("status", 1)
                .apply("checkin_date >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY)")
                .apply("checkin_date <= CURDATE()")
                .groupBy("user_id")
                .orderByDesc("score");
        return checkinRecordMapper.selectMaps(qw);
    }

    private Integer rankOf(List<Map<String, Object>> rows, Long userId) {
        for (int i = 0; i < rows.size(); i++) {
            if (((Number) rows.get(i).get("scopeId")).longValue() == userId) {
                return i + 1;
            }
        }
        return null;
    }

    private long getLong(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) {
            return 0L;
        }
        return ((Number) map.get(key)).longValue();
    }

    private SysUser currentUserWithCircle() {
        SysUser me = sysUserMapper.selectById(StpUtil.getLoginIdAsLong());
        if (me == null) {
            throw new BusinessException("用户不存在");
        }
        if (me.getCircleId() == null) {
            throw new BusinessException("请先加入学习圈");
        }
        return me;
    }
}
