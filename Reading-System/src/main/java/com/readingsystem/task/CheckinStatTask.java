package com.readingsystem.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.readingsystem.entity.CheckinRecord;
import com.readingsystem.entity.RankingSnapshot;
import com.readingsystem.entity.UserCheckinDailyStat;
import com.readingsystem.mapper.CircleMapper;
import com.readingsystem.mapper.CheckinRecordMapper;
import com.readingsystem.mapper.RankingSnapshotMapper;
import com.readingsystem.mapper.UserCheckinDailyStatMapper;
import com.readingsystem.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 打卡统计定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CheckinStatTask {

    private final CheckinRecordMapper checkinRecordMapper;
    private final UserCheckinDailyStatMapper dailyStatMapper;
    private final RankingSnapshotMapper rankingSnapshotMapper;
    private final CircleMapper circleMapper;

    /** 每日 00:05 把昨天的打卡聚合进每日统计宽表 */
    @Scheduled(cron = "0 5 0 * * ?")
    public void aggregateDailyStat() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        QueryWrapper<CheckinRecord> qw = new QueryWrapper<>();
        qw.select("user_id AS userId", "circle_id AS circleId", "group_id AS groupId",
                        "COUNT(CASE WHEN checkin_type = 2 THEN 1 END) AS audioCount",
                        "COUNT(CASE WHEN checkin_type = 3 THEN 1 END) AS noteCount",
                        "COUNT(CASE WHEN checkin_type NOT IN (2,3) THEN 1 END) AS readCount")
                .eq("checkin_date", yesterday)
                .eq("status", 1)
                .groupBy("user_id", "circle_id", "group_id");
        List<Map<String, Object>> rows = checkinRecordMapper.selectMaps(qw);
        int count = 0;
        for (Map<String, Object> row : rows) {
            Long userId = ((Number) row.get("userId")).longValue();
            Long exist = dailyStatMapper.selectCount(new LambdaQueryWrapper<UserCheckinDailyStat>()
                    .eq(UserCheckinDailyStat::getUserId, userId)
                    .eq(UserCheckinDailyStat::getStatDate, yesterday));
            if (exist != null && exist > 0) {
                continue;
            }
            UserCheckinDailyStat stat = new UserCheckinDailyStat();
            stat.setUserId(userId);
            stat.setCircleId(((Number) row.get("circleId")).longValue());
            stat.setGroupId(row.get("groupId") == null ? null : ((Number) row.get("groupId")).longValue());
            stat.setStatDate(yesterday);
            stat.setHasCheckin(1);
            stat.setAudioCount(((Number) row.get("audioCount")).intValue());
            stat.setReadCount(((Number) row.get("readCount")).intValue());
            stat.setNoteCount(((Number) row.get("noteCount")).intValue());
            dailyStatMapper.insert(stat);
            count++;
        }
        log.info("每日打卡统计完成: {} 新增 {} 条", yesterday, count);
    }

    /** 每周一 00:30 刷新上周个人周榜快照 */
    @Scheduled(cron = "0 30 0 ? * MON")
    public void refreshWeekRankingSnapshot() {
        LocalDate statDate = LocalDate.now().minusDays(1);
        String periodKey = DateUtils.weekKey(statDate);
        List<Long> circleIds = circleMapper.selectList(null).stream()
                .map(com.readingsystem.entity.Circle::getId).toList();
        for (Long circleId : circleIds) {
            QueryWrapper<CheckinRecord> qw = new QueryWrapper<>();
            qw.select("user_id AS scopeId", "group_id AS groupId",
                            "COUNT(DISTINCT checkin_date) AS score")
                    .eq("circle_id", circleId)
                    .eq("status", 1)
                    .apply("checkin_date >= DATE_SUB(DATE '" + statDate + "', INTERVAL WEEKDAY(DATE '" + statDate + "') DAY)")
                    .apply("checkin_date <= DATE '" + statDate + "'")
                    .groupBy("user_id", "group_id")
                    .orderByDesc("score")
                    .last("LIMIT 100");
            List<Map<String, Object>> rows = checkinRecordMapper.selectMaps(qw);
            // 清旧快照
            rankingSnapshotMapper.delete(new LambdaQueryWrapper<RankingSnapshot>()
                    .eq(RankingSnapshot::getRankType, 1)
                    .eq(RankingSnapshot::getScopeType, 1)
                    .eq(RankingSnapshot::getCircleId, circleId)
                    .eq(RankingSnapshot::getPeriodKey, periodKey));
            int rank = 1;
            for (Map<String, Object> row : rows) {
                RankingSnapshot snapshot = new RankingSnapshot();
                snapshot.setRankType(1);
                snapshot.setScopeType(1);
                snapshot.setScopeId(((Number) row.get("scopeId")).longValue());
                snapshot.setCircleId(circleId);
                snapshot.setGroupId(row.get("groupId") == null ? null : ((Number) row.get("groupId")).longValue());
                snapshot.setPeriodKey(periodKey);
                snapshot.setScore(((Number) row.get("score")).intValue());
                snapshot.setRankNo(rank++);
                snapshot.setStatDate(statDate);
                rankingSnapshotMapper.insert(snapshot);
            }
        }
        log.info("周榜快照刷新完成: {}", periodKey);
    }
}
