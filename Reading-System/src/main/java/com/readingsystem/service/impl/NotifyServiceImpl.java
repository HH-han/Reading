package com.readingsystem.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.readingsystem.common.PageResult;
import com.readingsystem.entity.Notification;
import com.readingsystem.mapper.NotificationMapper;
import com.readingsystem.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 消息通知服务
 */
@Service
@RequiredArgsConstructor
public class NotifyServiceImpl implements NotifyService {

    private final NotificationMapper notificationMapper;

    @Override
    public PageResult<Notification> page(long current, long size) {
        Page<Notification> page = notificationMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, StpUtil.getLoginIdAsLong())
                        .orderByDesc(Notification::getCreateTime));
        return PageResult.of(page);
    }

    @Override
    public void read(Long id) {
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, StpUtil.getLoginIdAsLong())
                .set(Notification::getReadStatus, 1));
    }

    @Override
    public Long unreadCount() {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, StpUtil.getLoginIdAsLong())
                .eq(Notification::getReadStatus, 0));
    }
}
