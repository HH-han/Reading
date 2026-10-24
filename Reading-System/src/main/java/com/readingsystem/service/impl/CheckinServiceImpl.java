package com.readingsystem.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.readingsystem.common.PageResult;
import com.readingsystem.constant.RedisKeyConstant;
import com.readingsystem.constant.SystemConstant;
import com.readingsystem.dto.AdminCheckinDTO;
import com.readingsystem.dto.CheckinAddDTO;
import com.readingsystem.dto.CheckinUpdateDTO;
import com.readingsystem.dto.CommentAddDTO;
import com.readingsystem.entity.*;
import com.readingsystem.enums.CheckinTypeEnum;
import com.readingsystem.exception.BusinessException;
import com.readingsystem.mapper.*;
import com.readingsystem.service.CheckinService;
import com.readingsystem.vo.CheckinFeedVO;
import com.readingsystem.vo.CommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 打卡服务
 */
@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final CheckinRecordMapper checkinRecordMapper;
    private final SysUserMapper sysUserMapper;
    private final BookMapper bookMapper;
    private final BookChapterMapper bookChapterMapper;
    private final CheckinTemplateMapper checkinTemplateMapper;
    private final UserGrowthLogMapper growthLogMapper;
    private final UserCheckinDailyStatMapper dailyStatMapper;
    private final UserLikeMapper userLikeMapper;
    private final CheckinCommentMapper commentMapper;
    private final UserCircleMapper userCircleMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(CheckinAddDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getCircleId() == null) {
            throw new BusinessException("请先加入学习圈再打卡");
        }
        checkTemplate(user.getCircleId(), dto);

        CheckinRecord record = new CheckinRecord();
        BeanUtil.copyProperties(dto, record);
        record.setUserId(userId);
        record.setCircleId(user.getCircleId());
        record.setGroupId(user.getGroupId());
        fillChapter(record, dto.getChapterId());
        return doCheckin(record, LocalDate.now(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long adminAdd(AdminCheckinDTO dto) {
        Long operatorId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException("队员不存在");
        }
        if (user.getCircleId() == null) {
            throw new BusinessException("该队员尚未加入学习圈");
        }
        CheckinRecord record = new CheckinRecord();
        BeanUtil.copyProperties(dto, record);
        record.setCircleId(user.getCircleId());
        record.setGroupId(user.getGroupId());
        record.setAuditRemark(dto.getRemark());
        record.setIsPublic(1);
        fillChapter(record, dto.getChapterId());
        LocalDate date = dto.getCheckinDate() == null ? LocalDate.now() : dto.getCheckinDate();
        return doCheckin(record, date, operatorId);
    }

    /**
     * 打卡核心逻辑：落库 + 累计天数 + 书籍热度 + 积分 + 每日统计 + Redis排行
     */
    private Long doCheckin(CheckinRecord record, LocalDate checkinDate, Long auditUserId) {
        if (record.getBookId() != null && bookMapper.selectById(record.getBookId()) == null) {
            throw new BusinessException("书籍不存在");
        }
        Long userId = record.getUserId();

        record.setCheckinDate(checkinDate);
        record.setStatus(SystemConstant.CHECKIN_STATUS_PASS);
        record.setAuditUserId(auditUserId);
        if (record.getIsPublic() == null) {
            record.setIsPublic(1);
        }
        checkinRecordMapper.insert(record);

        // 当日首次打卡才累计打卡天数
        Long todayCount = checkinRecordMapper.selectCount(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, userId)
                .eq(CheckinRecord::getCheckinDate, checkinDate)
                .eq(CheckinRecord::getStatus, 1)
                .ne(CheckinRecord::getId, record.getId()));
        if (todayCount == 0) {
            sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                    .eq(SysUser::getId, userId)
                    .setSql("checkin_days = checkin_days + 1"));
        }

        // 书籍打卡人数+1(图片打卡可不关联书籍)
        if (record.getBookId() != null) {
            bookMapper.update(null, new LambdaUpdateWrapper<Book>()
                    .eq(Book::getId, record.getBookId())
                    .setSql("checkin_count = checkin_count + 1"));
        }

        // 积分 +2 并记录成长日志
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .setSql("points = points + " + SystemConstant.CHECKIN_POINTS));
        UserGrowthLog growthLog = new UserGrowthLog();
        growthLog.setUserId(userId);
        growthLog.setChangeType(1);
        growthLog.setPoints(SystemConstant.CHECKIN_POINTS);
        growthLog.setRemark("打卡奖励");
        growthLog.setBizId(record.getId());
        growthLogMapper.insert(growthLog);

        // 每日统计宽表
        upsertDailyStat(record, checkinDate);

        // Redis ZSet 排行(周/月/年个人榜)
        syncRank(record, checkinDate);
        return record.getId();
    }

    private void upsertDailyStat(CheckinRecord record, LocalDate date) {
        String column = statColumn(record.getCheckinType());
        UserCheckinDailyStat exist = dailyStatMapper.selectOne(new LambdaQueryWrapper<UserCheckinDailyStat>()
                .eq(UserCheckinDailyStat::getUserId, record.getUserId())
                .eq(UserCheckinDailyStat::getStatDate, date));
        if (exist == null) {
            UserCheckinDailyStat stat = new UserCheckinDailyStat();
            stat.setUserId(record.getUserId());
            stat.setCircleId(record.getCircleId());
            stat.setGroupId(record.getGroupId());
            stat.setStatDate(date);
            stat.setHasCheckin(1);
            stat.setAudioCount(0);
            stat.setReadCount(0);
            stat.setNoteCount(0);
            dailyStatMapper.insert(stat);
            dailyStatMapper.update(null, new LambdaUpdateWrapper<UserCheckinDailyStat>()
                    .eq(UserCheckinDailyStat::getUserId, record.getUserId())
                    .eq(UserCheckinDailyStat::getStatDate, date)
                    .setSql(column + " = " + column + " + 1"));
        } else {
            dailyStatMapper.update(null, new LambdaUpdateWrapper<UserCheckinDailyStat>()
                    .eq(UserCheckinDailyStat::getId, exist.getId())
                    .set(UserCheckinDailyStat::getHasCheckin, 1)
                    .setSql(column + " = " + column + " + 1"));
        }
    }

    private void syncRank(CheckinRecord record, LocalDate date) {
        syncRank(record, date, 1);
    }

    private void syncRank(CheckinRecord record, LocalDate date, double delta) {
        try {
            WeekFields weekFields = WeekFields.ISO;
            String weekKey = date.getYear() + "-W" + date.get(weekFields.weekOfWeekBasedYear());
            String monthKey = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            String yearKey = String.valueOf(date.getYear());
            String userId = record.getUserId().toString();
            String[] keys = {
                    String.format(RedisKeyConstant.RANK_CIRCLE_WEEK, record.getCircleId(), weekKey),
                    String.format(RedisKeyConstant.RANK_CIRCLE_MONTH, record.getCircleId(), monthKey),
                    String.format(RedisKeyConstant.RANK_CIRCLE_YEAR, record.getCircleId(), yearKey)
            };
            for (String key : keys) {
                redisTemplate.opsForZSet().incrementScore(key, userId, delta);
                // 删除打卡扣分后分数不大于0时从榜单移除，避免出现负分
                if (delta < 0) {
                    Double score = redisTemplate.opsForZSet().score(key, userId);
                    if (score != null && score <= 0) {
                        redisTemplate.opsForZSet().remove(key, userId);
                    }
                }
            }
        } catch (Exception ignored) {
            // Redis 异常不影响打卡
        }
    }

    /** 打卡类型对应的每日统计列 */
    private String statColumn(Integer checkinType) {
        return switch (checkinType == null ? 3 : checkinType) {
            case 2 -> "audio_count";
            case 3 -> "note_count";
            default -> "read_count";
        };
    }

    /** 书籍打卡人数增减 */
    private void changeBookCount(Long bookId, int delta) {
        if (bookId == null || bookMapper.selectById(bookId) == null) {
            return;
        }
        String sql = delta > 0 ? "checkin_count = checkin_count + 1" : "checkin_count = GREATEST(checkin_count - 1, 0)";
        bookMapper.update(null, new LambdaUpdateWrapper<Book>()
                .eq(Book::getId, bookId)
                .setSql(sql));
    }

    /** 每日统计宽表分类计数增减 */
    private void changeDailyStat(CheckinRecord record, Integer checkinType, int delta) {
        String column = statColumn(checkinType);
        String sql = delta > 0 ? column + " = " + column + " + 1" : column + " = GREATEST(" + column + " - 1, 0)";
        dailyStatMapper.update(null, new LambdaUpdateWrapper<UserCheckinDailyStat>()
                .eq(UserCheckinDailyStat::getUserId, record.getUserId())
                .eq(UserCheckinDailyStat::getStatDate, record.getCheckinDate())
                .setSql(sql));
    }

    /** 校验打卡模板必填规则 */
    private void checkTemplate(Long circleId, CheckinAddDTO dto) {
        Integer checkinType = dto.getCheckinType();
        if (CheckinTypeEnum.of(checkinType) == null) {
            throw new BusinessException("不支持的打卡类型");
        }
        List<CheckinTemplate> templates = checkinTemplateMapper.selectList(new LambdaQueryWrapper<CheckinTemplate>()
                .eq(CheckinTemplate::getCircleId, circleId)
                .eq(CheckinTemplate::getStatus, 1)
                .orderByAsc(CheckinTemplate::getId));
        if (templates.isEmpty()) {
            // 未配置模板时沿用默认规则：除图片打卡外必须选择书籍
            if (!CheckinTypeEnum.IMAGE.getCode().equals(checkinType) && dto.getBookId() == null) {
                throw new BusinessException("书籍不能为空");
            }
            return;
        }
        CheckinTemplate tpl = templates.get(0);
        if (StrUtil.isNotBlank(tpl.getCheckinTypes())
                && !StrUtil.split(tpl.getCheckinTypes(), ',').contains(String.valueOf(checkinType))) {
            throw new BusinessException("当前模板不允许该打卡类型");
        }
        // 书籍/章节/图片/音频/心得是否必填由模板按打卡类型分别配置
        Map<String, Boolean> need = resolveRequire(tpl, checkinType);
        if (need.get("book") && dto.getBookId() == null) {
            throw new BusinessException("模板要求必须选择书籍");
        }
        boolean hasChapter = dto.getChapterId() != null || StrUtil.isNotBlank(dto.getChapterTitle());
        if (need.get("chapter") && !hasChapter) {
            throw new BusinessException("模板要求必须填写章节标题");
        }
        if (need.get("image") && StrUtil.isBlank(dto.getMediaUrl())) {
            throw new BusinessException("模板要求必须上传图片");
        }
        if (need.get("audio") && StrUtil.isBlank(dto.getAudioUrl())) {
            throw new BusinessException("模板要求必须上传音频");
        }
        String content = dto.getContent();
        int wordCount = content == null ? 0 : content.trim().length();
        if (need.get("note") && wordCount == 0) {
            throw new BusinessException("模板要求必须填写心得");
        }
        if (need.get("note") && tpl.getMinWordCount() != null && tpl.getMinWordCount() > 0 && wordCount < tpl.getMinWordCount()) {
            throw new BusinessException("心得最少" + tpl.getMinWordCount() + "字");
        }
    }

    /**
     * 解析当前打卡类型需要展示并必填的项。
     * 模板配置了 typeConfig(JSON) 时按类型分别判断，否则回退到旧的全局必填开关。
     */
    private Map<String, Boolean> resolveRequire(CheckinTemplate tpl, Integer checkinType) {
        boolean imageType = CheckinTypeEnum.IMAGE.getCode().equals(checkinType);
        boolean audioType = CheckinTypeEnum.AUDIO.getCode().equals(checkinType);
        boolean noteType = CheckinTypeEnum.NOTE.getCode().equals(checkinType);
        Map<String, Boolean> need = new HashMap<>();
        if (StrUtil.isBlank(tpl.getTypeConfig())) {
            // 旧模板：书籍必填沿用历史规则（图片打卡可不选），章节不做强制
            need.put("book", !imageType || isRequire(tpl.getRequireBook()));
            need.put("chapter", isRequire(tpl.getRequireChapter()));
            need.put("image", imageType || isRequire(tpl.getRequireImage()));
            need.put("audio", audioType || isRequire(tpl.getRequireAudio()));
            need.put("note", noteType || isRequire(tpl.getRequireContent()));
            return need;
        }
        JSONObject cfg = null;
        try {
            cfg = JSONUtil.parseObj(tpl.getTypeConfig()).getJSONObject(String.valueOf(checkinType));
        } catch (Exception ignored) {
            // 配置格式异常时仅校验该类型的核心内容
        }
        // 打卡类型自身的核心内容默认必填（图片/音频/心得）
        need.put("book", cfg != null && cfg.getInt("book", 0) == 1);
        need.put("chapter", cfg != null && cfg.getInt("chapter", 0) == 1);
        need.put("image", imageType || (cfg != null && cfg.getInt("image", 0) == 1));
        need.put("audio", audioType || (cfg != null && cfg.getInt("audio", 0) == 1));
        need.put("note", noteType || (cfg != null && cfg.getInt("note", 0) == 1));
        return need;
    }

    private boolean isRequire(Integer flag) {
        return flag != null && flag == 1;
    }

    private void fillChapter(CheckinRecord record, Long chapterId) {
        if (chapterId != null) {
            BookChapter chapter = bookChapterMapper.selectById(chapterId);
            if (chapter != null) {
                record.setChapterTitle(chapter.getTitle());
            }
        }
        if (record.getChapterTitle() == null) {
            record.setChapterTitle("");
        }
    }

    @Override
    public PageResult<CheckinFeedVO> feed(String tab, long current, long size) {
        Integer type = switch (tab == null ? "" : tab) {
            case "audio" -> CheckinTypeEnum.AUDIO.getCode();
            case "note" -> CheckinTypeEnum.NOTE.getCode();
            case "image" -> CheckinTypeEnum.IMAGE.getCode();
            default -> null;
        };
        // 动态仅展示当前选中读书圈内成员发布的内容
        SysUser user = sysUserMapper.selectById(StpUtil.getLoginIdAsLong());
        if (user == null || user.getCircleId() == null) {
            return PageResult.empty(current, size);
        }
        Long currentCircleId = user.getCircleId();
        boolean joined = userCircleMapper.selectCount(new LambdaQueryWrapper<UserCircle>()
                .eq(UserCircle::getUserId, user.getId())
                .eq(UserCircle::getCircleId, currentCircleId)) > 0;
        if (!joined) {
            return PageResult.empty(current, size);
        }
        Page<CheckinFeedVO> page = new Page<>(current, size);
        IPage<CheckinFeedVO> result = checkinRecordMapper.selectFeedPage(page, type, List.of(currentCircleId));
        return PageResult.of(result);
    }

    @Override
    public PageResult<CheckinRecord> my(long current, long size) {
        Page<CheckinRecord> page = checkinRecordMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<CheckinRecord>()
                        .eq(CheckinRecord::getUserId, StpUtil.getLoginIdAsLong())
                        .orderByDesc(CheckinRecord::getCreateTime));
        return PageResult.of(page);
    }

    @Override
    public CheckinRecord detail(Long id) {
        CheckinRecord record = checkinRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("打卡记录不存在");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        if (!record.getUserId().equals(userId) && !StpUtil.hasRole("SUPER_ADMIN") && !StpUtil.hasRole("TEAM_ADMIN")) {
            throw new BusinessException("无权查看该打卡记录");
        }
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMy(Long id, CheckinUpdateDTO dto) {
        CheckinRecord record = checkinRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("打卡记录不存在");
        }
        if (!record.getUserId().equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException("只能修改自己的打卡记录");
        }
        // 修改后的内容仍需符合当前读书圈模板的必填规则
        CheckinAddDTO check = new CheckinAddDTO();
        check.setBookId(dto.getBookId());
        check.setChapterId(dto.getChapterId());
        check.setChapterTitle(dto.getChapterTitle());
        check.setCheckinType(dto.getCheckinType());
        check.setContent(dto.getContent());
        check.setMediaUrl(dto.getMediaUrl());
        check.setAudioUrl(dto.getAudioUrl());
        checkTemplate(record.getCircleId(), check);

        String chapterTitle = dto.getChapterTitle() == null ? "" : dto.getChapterTitle();
        if (dto.getChapterId() != null) {
            BookChapter chapter = bookChapterMapper.selectById(dto.getChapterId());
            if (chapter != null) {
                chapterTitle = chapter.getTitle();
            }
        }
        LambdaUpdateWrapper<CheckinRecord> update = new LambdaUpdateWrapper<CheckinRecord>()
                .eq(CheckinRecord::getId, id)
                .set(CheckinRecord::getBookId, dto.getBookId())
                .set(CheckinRecord::getChapterId, dto.getChapterId())
                .set(CheckinRecord::getChapterTitle, chapterTitle)
                .set(CheckinRecord::getCheckinType, dto.getCheckinType())
                .set(CheckinRecord::getContent, dto.getContent() == null ? "" : dto.getContent())
                .set(CheckinRecord::getMediaUrl, dto.getMediaUrl() == null ? "" : dto.getMediaUrl())
                .set(CheckinRecord::getAudioUrl, dto.getAudioUrl() == null ? "" : dto.getAudioUrl())
                .set(CheckinRecord::getAudioDuration, dto.getAudioDuration() == null ? 0 : dto.getAudioDuration());
        if (dto.getIsPublic() != null) {
            update.set(CheckinRecord::getIsPublic, dto.getIsPublic());
        }
        checkinRecordMapper.update(null, update);

        // 书籍或打卡类型变化时同步热度与每日分类统计
        if (!Objects.equals(record.getBookId(), dto.getBookId())) {
            changeBookCount(record.getBookId(), -1);
            changeBookCount(dto.getBookId(), 1);
        }
        if (!Objects.equals(record.getCheckinType(), dto.getCheckinType())) {
            changeDailyStat(record, record.getCheckinType(), -1);
            changeDailyStat(record, dto.getCheckinType(), 1);
        }
    }

    @Override
    public CheckinTemplate currentTemplate() {
        SysUser user = sysUserMapper.selectById(StpUtil.getLoginIdAsLong());
        if (user == null) {
            return null;
        }
        // 以用户当前选择的读书圈为准（sys_user.circle_id 失效时回退到已加入的圈子）
        UserCircle membership = activeMembership(user);
        if (membership == null) {
            return null;
        }
        Long circleId = membership.getCircleId();
        List<CheckinTemplate> templates = checkinTemplateMapper.selectList(new LambdaQueryWrapper<CheckinTemplate>()
                .eq(CheckinTemplate::getCircleId, circleId)
                .eq(CheckinTemplate::getStatus, 1)
                .orderByAsc(CheckinTemplate::getId));
        return templates.isEmpty() ? null : templates.get(0);
    }

    /** 取当前选择的读书圈成员关系，必要时按最近加入的圈子修正用户表冗余字段 */
    private UserCircle activeMembership(SysUser user) {
        if (user.getCircleId() != null) {
            UserCircle membership = userCircleMapper.selectOne(new LambdaQueryWrapper<UserCircle>()
                    .eq(UserCircle::getUserId, user.getId())
                    .eq(UserCircle::getCircleId, user.getCircleId()));
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
        user.setCircleId(membership.getCircleId());
        user.setGroupId(membership.getGroupId());
        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getCircleId, membership.getCircleId())
                .set(SysUser::getGroupId, membership.getGroupId()));
        return membership;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        CheckinRecord record = checkinRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("打卡记录不存在");
        }
        Long userId = StpUtil.getLoginIdAsLong();
        if (!record.getUserId().equals(userId) && !StpUtil.hasRole("SUPER_ADMIN") && !StpUtil.hasRole("TEAM_ADMIN")) {
            throw new BusinessException("只能删除自己的打卡记录");
        }
        checkinRecordMapper.deleteById(id);

        // 回退该条打卡产生的热度、分类统计与排行分数
        changeBookCount(record.getBookId(), -1);
        changeDailyStat(record, record.getCheckinType(), -1);
        syncRank(record, record.getCheckinDate(), -1);
        // 当天已无有效打卡时回退打卡天数
        Long rest = checkinRecordMapper.selectCount(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, record.getUserId())
                .eq(CheckinRecord::getCheckinDate, record.getCheckinDate())
                .eq(CheckinRecord::getStatus, SystemConstant.CHECKIN_STATUS_PASS));
        if (rest == 0) {
            sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                    .eq(SysUser::getId, record.getUserId())
                    .setSql("checkin_days = GREATEST(checkin_days - 1, 0)"));
            dailyStatMapper.update(null, new LambdaUpdateWrapper<UserCheckinDailyStat>()
                    .eq(UserCheckinDailyStat::getUserId, record.getUserId())
                    .eq(UserCheckinDailyStat::getStatDate, record.getCheckinDate())
                    .set(UserCheckinDailyStat::getHasCheckin, 0));
        }
    }

    @Override
    public void like(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserLike like = new UserLike();
        like.setUserId(userId);
        like.setTargetType(SystemConstant.LIKE_TARGET_CHECKIN);
        like.setTargetId(id);
        try {
            userLikeMapper.insert(like);
        } catch (DuplicateKeyException e) {
            return;
        }
        checkinRecordMapper.update(null, new LambdaUpdateWrapper<CheckinRecord>()
                .eq(CheckinRecord::getId, id)
                .setSql("like_count = like_count + 1"));
    }

    @Override
    public void unlike(Long id) {
        int rows = userLikeMapper.delete(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, StpUtil.getLoginIdAsLong())
                .eq(UserLike::getTargetType, SystemConstant.LIKE_TARGET_CHECKIN)
                .eq(UserLike::getTargetId, id));
        if (rows > 0) {
            checkinRecordMapper.update(null, new LambdaUpdateWrapper<CheckinRecord>()
                    .eq(CheckinRecord::getId, id)
                    .setSql("like_count = GREATEST(like_count - 1, 0)"));
        }
    }

    @Override
    public List<CommentVO> comments(Long id) {
        List<CheckinComment> comments = commentMapper.selectList(new LambdaQueryWrapper<CheckinComment>()
                .eq(CheckinComment::getCheckinId, id)
                .eq(CheckinComment::getStatus, 1)
                .orderByAsc(CheckinComment::getCreateTime));
        if (comments.isEmpty()) {
            return List.of();
        }
        List<Long> userIds = comments.stream().map(CheckinComment::getUserId).distinct().toList();
        Map<Long, SysUser> userMap = sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
        return comments.stream().map(c -> {
            CommentVO vo = BeanUtil.copyProperties(c, CommentVO.class);
            SysUser u = userMap.get(c.getUserId());
            if (u != null) {
                vo.setNickname(u.getNickname());
                vo.setAvatar(u.getAvatar());
            }
            return vo;
        }).toList();
    }

    @Override
    public Long addComment(Long id, CommentAddDTO dto) {
        CheckinRecord record = checkinRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("打卡记录不存在");
        }
        CheckinComment comment = new CheckinComment();
        comment.setCheckinId(id);
        comment.setUserId(StpUtil.getLoginIdAsLong());
        comment.setParentId(dto.getParentId());
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1);
        commentMapper.insert(comment);
        checkinRecordMapper.update(null, new LambdaUpdateWrapper<CheckinRecord>()
                .eq(CheckinRecord::getId, id)
                .setSql("comment_count = comment_count + 1"));
        return comment.getId();
    }
}
