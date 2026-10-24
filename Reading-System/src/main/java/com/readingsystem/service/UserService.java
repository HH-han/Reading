package com.readingsystem.service;

import com.readingsystem.common.PageResult;
import com.readingsystem.dto.UserUpdateDTO;
import com.readingsystem.entity.UserGrowthLog;
import com.readingsystem.vo.CheckinDaysVO;
import com.readingsystem.vo.FavoriteVO;
import com.readingsystem.vo.UserInfoVO;

public interface UserService {

    UserInfoVO info();

    void updateInfo(UserUpdateDTO dto);

    PageResult<UserGrowthLog> growthPage(long current, long size);

    PageResult<FavoriteVO> favoritesPage(long current, long size);

    CheckinDaysVO checkinDays();
}
