package com.readingsystem.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.readingsystem.constant.RedisKeyConstant;
import com.readingsystem.entity.SysUser;
import com.readingsystem.entity.TGroup;
import com.readingsystem.exception.BusinessException;
import com.readingsystem.mapper.CheckinRecordMapper;
import com.readingsystem.mapper.SysUserMapper;
import com.readingsystem.mapper.TGroupMapper;
import com.readingsystem.service.RankingService;
import com.readingsystem.vo.RankingVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 排行榜服务：优先读 Redis ZSet，未命中实时聚合并回写
 */
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final CheckinRecordMapper checkinRecordMapper;
    private final SysUserMapper sysUserMapper;
    private final TGroupMapper tGroupMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<RankingVO> rank(Integer rankType, Integer scopeType) {
        SysUser me = sysUserMapper.selectById(StpUtil.getLoginIdAsLong());
        if (me == null || me.getCircleId() == null) {
            throw new BusinessException("请先加入学习圈");
        }
        Long circleId = me.getCircleId();
        String redisKey = buildKey(rankType, scopeType, circleId);

        // 先读 Redis
        try {
            Set<ZSetOperations.TypedTuple<Object>> tuples =
                    redisTemplate.opsForZSet().reverseRangeWithScores(redisKey, 0, 49);
            if (tuples != null && !tuples.isEmpty()) {
                return buildFromRedis(tuples, scopeType);
            }
        } catch (Exception ignored) {
            // Redis 异常走实时聚合
        }

        // 实时聚合 checkin_record
        List<Map<String, Object>> rows = aggregate(rankType, scopeType, circleId);
        List<RankingVO> list = new ArrayList<>();
        int rank = 1;
        for (Map<String, Object> row : rows) {
            RankingVO vo = new RankingVO();
            vo.setRankNo(rank++);
            vo.setScopeId(((Number) row.get("scopeId")).longValue());
            vo.setScore(((Number) row.get("score")).doubleValue());
            list.add(vo);
        }
        fillNames(list, scopeType);
        writeBack(redisKey, list);
        return list;
    }

    private String buildKey(Integer rankType, Integer scopeType, Long circleId) {
        LocalDate now = LocalDate.now();
        String periodKey = periodKey(rankType, now);
        if (scopeType != null && scopeType == 2) {
            return String.format(RedisKeyConstant.RANK_GROUP_WEEK, circleId, periodKey);
        }
        return switch (rankType == null ? 1 : rankType) {
            case 2 -> String.format(RedisKeyConstant.RANK_CIRCLE_MONTH, circleId, periodKey);
            case 3 -> String.format(RedisKeyConstant.RANK_CIRCLE_YEAR, circleId, periodKey);
            default -> String.format(RedisKeyConstant.RANK_CIRCLE_WEEK, circleId, periodKey);
        };
    }

    private String periodKey(Integer rankType, LocalDate date) {
        int type = rankType == null ? 1 : rankType;
        return switch (type) {
            case 2 -> date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            case 3 -> String.valueOf(date.getYear());
            default -> date.getYear() + "-W" + date.get(WeekFields.ISO.weekOfWeekBasedYear());
        };
    }

    /** 按周期实时聚合打卡天数 */
    private List<Map<String, Object>> aggregate(Integer rankType, Integer scopeType, Long circleId) {
        String dateCondition = switch (rankType == null ? 1 : rankType) {
            case 2 -> "DATE_FORMAT(r.checkin_date, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')";
            case 3 -> "YEAR(r.checkin_date) = YEAR(CURDATE())";
            default -> "r.checkin_date >= DATE_SUB(CURDATE(), INTERVAL WEEKDAY(CURDATE()) DAY) AND r.checkin_date <= CURDATE()";
        };
        boolean byGroup = scopeType != null && scopeType == 2;
        QueryWrapper<com.readingsystem.entity.CheckinRecord> qw = new QueryWrapper<>();
        qw.select((byGroup ? "group_id" : "user_id") + " AS scopeId",
                        "COUNT(DISTINCT checkin_date" + (byGroup ? ", user_id" : "") + ") AS score")
                .eq("circle_id", circleId)
                .eq("status", 1)
                .isNotNull(byGroup, "group_id")
                .apply(dateCondition.replace("r.", ""))
                .groupBy("scopeId")
                .orderByDesc("score")
                .last("LIMIT 50");
        return checkinRecordMapper.selectMaps(qw);
    }

    private List<RankingVO> buildFromRedis(Set<ZSetOperations.TypedTuple<Object>> tuples, Integer scopeType) {
        List<RankingVO> list = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
            if (tuple.getValue() == null) {
                continue;
            }
            RankingVO vo = new RankingVO();
            vo.setRankNo(rank++);
            vo.setScopeId(Long.valueOf(tuple.getValue().toString()));
            vo.setScore(tuple.getScore() == null ? 0 : tuple.getScore());
            list.add(vo);
        }
        fillNames(list, scopeType);
        return list;
    }

    private void fillNames(List<RankingVO> list, Integer scopeType) {
        if (list.isEmpty()) {
            return;
        }
        List<Long> ids = list.stream().map(RankingVO::getScopeId).toList();
        boolean byGroup = scopeType != null && scopeType == 2;
        if (byGroup) {
            Map<Long, TGroup> map = tGroupMapper.selectBatchIds(ids).stream()
                    .collect(Collectors.toMap(TGroup::getId, g -> g));
            list.forEach(vo -> {
                TGroup g = map.get(vo.getScopeId());
                vo.setName(g == null ? "未知小组" : g.getName());
            });
        } else {
            Map<Long, SysUser> map = sysUserMapper.selectBatchIds(ids).stream()
                    .collect(Collectors.toMap(SysUser::getId, u -> u));
            list.forEach(vo -> {
                SysUser u = map.get(vo.getScopeId());
                vo.setName(u == null ? "未知用户" : u.getNickname());
                vo.setAvatar(u == null ? "" : u.getAvatar());
            });
        }
    }

    private void writeBack(String redisKey, List<RankingVO> list) {
        try {
            for (RankingVO vo : list) {
                redisTemplate.opsForZSet().add(redisKey, vo.getScopeId().toString(), vo.getScore());
            }
        } catch (Exception ignored) {
            // 回写失败不影响查询
        }
    }
}
