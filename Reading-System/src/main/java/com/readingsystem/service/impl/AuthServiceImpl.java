package com.readingsystem.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.readingsystem.dto.LoginDTO;
import com.readingsystem.dto.RegisterDTO;
import com.readingsystem.dto.WxLoginDTO;
import com.readingsystem.entity.SysUser;
import com.readingsystem.enums.RoleEnum;
import com.readingsystem.exception.BusinessException;
import com.readingsystem.mapper.SysUserMapper;
import com.readingsystem.service.AuthService;
import com.readingsystem.vo.LoginVO;
import com.readingsystem.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;

    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (user == null || StrUtil.isBlank(user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        checkStatus(user);
        return doLogin(user);
    }

    @Override
    public LoginVO wxLogin(WxLoginDTO dto) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getOpenId, dto.getOpenId()));
        if (user == null) {
            // 自动注册为普通队员
            user = new SysUser();
            user.setOpenId(dto.getOpenId());
            user.setNickname(StrUtil.blankToDefault(dto.getNickname(), "书友" + RandomUtil.randomNumbers(6)));
            user.setAvatar(StrUtil.blankToDefault(dto.getAvatar(), ""));
            user.setRole(RoleEnum.MEMBER.name());
            user.setPosition("普通员工");
            user.setGender(0);
            user.setPoints(0);
            user.setCheckinDays(0);
            user.setStatus(1);
            sysUserMapper.insert(user);
        }
        checkStatus(user);
        return doLogin(user);
    }

    @Override
    public LoginVO register(RegisterDTO dto) {
        // 检查用户名是否已存在
        Long count = sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException("用户名已被占用");
        }
        // 创建普通队员账号
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
        user.setNickname("书友" + RandomUtil.randomNumbers(6));
        user.setAvatar("");
        user.setRole(RoleEnum.MEMBER.name());
        user.setPosition("普通员工");
        user.setGender(0);
        user.setPoints(0);
        user.setCheckinDays(0);
        user.setStatus(1);
        sysUserMapper.insert(user);
        // 注册后自动登录
        return doLogin(user);
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    private void checkStatus(SysUser user) {
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
    }

    private LoginVO doLogin(SysUser user) {
        StpUtil.login(user.getId());
        // 更新最后登录时间
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(update);

        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setUser(BeanUtil.copyProperties(user, UserInfoVO.class));
        return vo;
    }
}
