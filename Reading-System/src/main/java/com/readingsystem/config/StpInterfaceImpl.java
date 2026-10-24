package com.readingsystem.config;

import cn.dev33.satoken.stp.StpInterface;
import com.readingsystem.entity.SysUser;
import com.readingsystem.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 角色/权限加载
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserMapper sysUserMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SysUser user = sysUserMapper.selectById(Long.valueOf(loginId.toString()));
        if (user == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(user.getRole());
    }
}
