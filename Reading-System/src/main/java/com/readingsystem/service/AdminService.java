package com.readingsystem.service;

import com.readingsystem.common.PageResult;
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
import com.readingsystem.entity.Book;
import com.readingsystem.entity.BookChapter;
import com.readingsystem.entity.CheckinRule;
import com.readingsystem.entity.CheckinTemplate;
import com.readingsystem.entity.Circle;
import com.readingsystem.entity.StudyJob;
import com.readingsystem.entity.TGroup;
import com.readingsystem.vo.AdminCheckinVO;
import com.readingsystem.vo.CircleBriefVO;
import com.readingsystem.vo.PositionVO;
import com.readingsystem.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminService {

    PageResult<StudyJob> jobPage(Long circleId, long current, long size);

    Long saveJob(JobDTO dto);

    void updateJob(JobDTO dto);

    void deleteJob(Long id);

    List<Circle> circleList();

    Long saveCircle(CircleDTO dto);

    void updateCircle(CircleDTO dto);

    void deleteCircle(Long id);

    PageResult<UserInfoVO> circleMembers(Long circleId, String keyword, long current, long size);

    void inviteMember(Long circleId, Long userId);

    void removeMember(Long circleId, Long userId);

    List<TGroup> groupList(Long circleId);

    Long saveGroup(GroupDTO dto);

    void updateGroup(GroupDTO dto);

    void deleteGroup(Long id);

    PageResult<UserInfoVO> groupMembers(Long groupId, long current, long size);

    void assignMemberGroup(Long circleId, Long userId, Long groupId);

    PageResult<UserInfoVO> userPage(String keyword, long current, long size);

    List<CheckinRule> ruleList(Long circleId);

    Long saveRule(RuleDTO dto);

    void updateRule(RuleDTO dto);

    void deleteRule(Long id);

    List<CheckinTemplate> templateList(Long circleId);

    Long saveTemplate(TemplateDTO dto);

    void updateTemplate(TemplateDTO dto);

    void deleteTemplate(Long id);

    void exportCheckin(Long circleId, String startDate, String endDate, HttpServletResponse response);

    Integer importUsers(MultipartFile file);

    void updateUserStatus(Long userId, Integer status);

    void updateUserPosition(Long userId, String position);

    List<CircleBriefVO> userCircles(Long userId);

    void cancelAccount();

    // ================= 数据管理 =================

    PageResult<Book> dataBookPage(String keyword, String category, Integer status, long current, long size);

    Long saveBook(BookDTO dto);

    void updateBook(BookDTO dto);

    void deleteBook(Long id);

    List<BookChapter> chapterList(Long bookId);

    Long saveChapter(ChapterDTO dto);

    void updateChapter(ChapterDTO dto);

    void deleteChapter(Long id);

    PageResult<UserInfoVO> dataUserPage(String keyword, Long circleId, String role, long current, long size);

    Long saveUser(AdminUserDTO dto);

    void updateUser(AdminUserDTO dto);

    void deleteUser(Long id);

    PageResult<AdminCheckinVO> dataCheckinPage(String keyword, Long userId, Long circleId, String startDate,
                                               String endDate, Integer status, long current, long size);

    void updateCheckin(CheckinEditDTO dto);

    void deleteCheckin(Long id);

    // ================= 职位管理 =================

    List<PositionVO> positionList(String keyword);

    Long savePosition(PositionDTO dto);

    void updatePosition(PositionDTO dto);

    void deletePosition(Long id);
}
