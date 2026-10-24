package com.readingsystem.service;

import com.readingsystem.common.PageResult;
import com.readingsystem.dto.AdminCheckinDTO;
import com.readingsystem.dto.CheckinAddDTO;
import com.readingsystem.dto.CheckinUpdateDTO;
import com.readingsystem.dto.CommentAddDTO;
import com.readingsystem.entity.CheckinRecord;
import com.readingsystem.entity.CheckinTemplate;
import com.readingsystem.vo.CheckinFeedVO;
import com.readingsystem.vo.CommentVO;

import java.util.List;

public interface CheckinService {

    Long add(CheckinAddDTO dto);

    Long adminAdd(AdminCheckinDTO dto);

    PageResult<CheckinFeedVO> feed(String tab, long current, long size);

    PageResult<CheckinRecord> my(long current, long size);

    /** 打卡详情（本人或管理员可查看） */
    CheckinRecord detail(Long id);

    /** 本人修改自己的打卡记录 */
    void updateMy(Long id, CheckinUpdateDTO dto);

    /** 当前用户所属学习圈生效的打卡模板，无则返回 null */
    CheckinTemplate currentTemplate();

    void delete(Long id);

    void like(Long id);

    void unlike(Long id);

    List<CommentVO> comments(Long id);

    Long addComment(Long id, CommentAddDTO dto);
}
