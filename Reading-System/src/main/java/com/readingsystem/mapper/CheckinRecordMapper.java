package com.readingsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.readingsystem.entity.CheckinRecord;
import com.readingsystem.vo.CheckinFeedVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mapper
public interface CheckinRecordMapper extends BaseMapper<CheckinRecord> {

    /** 本周打卡天数 */
    Integer countWeekDays(@Param("userId") Long userId);

    /** 本月打卡天数 */
    Integer countMonthDays(@Param("userId") Long userId);

    /** 本年打卡天数 */
    Integer countYearDays(@Param("userId") Long userId);

    /** 圈子今日打卡态势 */
    Map<String, Object> todaySituation(@Param("circleId") Long circleId);

    /** 圈子今日活跃小组数 */
    Long countActiveGroups(@Param("circleId") Long circleId);

    /** 小组周排行榜 */
    List<Map<String, Object>> groupWeekRank(@Param("circleId") Long circleId, @Param("limit") Integer limit);

    /** 打卡率趋势(近N天) */
    List<Map<String, Object>> checkinTrend(@Param("circleId") Long circleId, @Param("days") Integer days);

    /** 公开动态分页(仅限用户已加入的读书圈) */
    IPage<CheckinFeedVO> selectFeedPage(Page<CheckinFeedVO> page,
                                        @Param("checkinType") Integer checkinType,
                                        @Param("circleIds") Collection<Long> circleIds);
}
