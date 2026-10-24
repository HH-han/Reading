package com.readingsystem.service;

import com.readingsystem.vo.DashboardTodayVO;
import com.readingsystem.vo.MyStatVO;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    DashboardTodayVO today();

    List<Map<String, Object>> trend(Integer days);

    List<Map<String, Object>> topGroups();

    MyStatVO my();
}
