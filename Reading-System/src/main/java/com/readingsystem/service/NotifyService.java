package com.readingsystem.service;

import com.readingsystem.common.PageResult;
import com.readingsystem.entity.Notification;

public interface NotifyService {

    PageResult<Notification> page(long current, long size);

    void read(Long id);

    Long unreadCount();
}
