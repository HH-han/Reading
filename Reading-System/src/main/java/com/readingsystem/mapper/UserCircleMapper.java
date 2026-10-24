package com.readingsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.readingsystem.entity.SysUser;
import com.readingsystem.entity.UserCircle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserCircleMapper extends BaseMapper<UserCircle> {

    /**
     * 分页查询某读书圈下的成员（含关键词过滤）
     */
    @Select("<script>" +
            "SELECT u.* FROM sys_user u INNER JOIN user_circle uc ON uc.user_id = u.id " +
            "WHERE uc.circle_id = #{circleId} AND u.deleted = 0 AND u.status = 1 " +
            "<if test='groupId != null'> AND uc.group_id = #{groupId} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (u.nickname LIKE CONCAT('%', #{keyword}, '%') " +
            "OR u.phone LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY u.checkin_days DESC, u.id ASC" +
            "</script>")
    IPage<SysUser> pageMembers(IPage<SysUser> page,
                               @Param("circleId") Long circleId,
                               @Param("groupId") Long groupId,
                               @Param("keyword") String keyword);
}
