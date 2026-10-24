package com.readingsystem.service;

import com.readingsystem.common.PageResult;
import com.readingsystem.entity.StudyJob;
import com.readingsystem.entity.TGroup;
import com.readingsystem.vo.CircleBriefVO;
import com.readingsystem.vo.CircleCurrentVO;
import com.readingsystem.vo.UserInfoVO;

import java.util.List;

public interface CircleService {

    CircleCurrentVO current();

    List<TGroup> groups();

    PageResult<UserInfoVO> members(Long groupId, long current, long size);

    /** 成员查看本圈已发布的作业 */
    PageResult<StudyJob> jobPage(long current, long size);

    /** 已存在的读书圈列表（通讯录加入用） */
    List<CircleBriefVO> available();

    /** 我加入的读书圈列表 */
    List<CircleBriefVO> mine();

    void join(String inviteCode);

    void joinById(Long circleId);

    /** 切换当前使用的读书圈 */
    void switchTo(Long circleId);

    /** 退出当前读书圈 */
    void exit();

    /** 退出指定读书圈 */
    void exit(Long circleId);
}
