package com.readingsystem.constant;

/**
 * 系统常量
 */
public interface SystemConstant {

    /** 打卡通过状态 */
    Integer CHECKIN_STATUS_PASS = 1;

    /** 打卡积分奖励 */
    Integer CHECKIN_POINTS = 2;

    /** 默认密码 */
    String DEFAULT_PASSWORD = "123456";

    /** 点赞目标-打卡记录 */
    Integer LIKE_TARGET_CHECKIN = 1;

    /** 收藏目标-书籍 */
    Integer FAVORITE_TARGET_BOOK = 1;

    /** 收藏目标-打卡记录 */
    Integer FAVORITE_TARGET_CHECKIN = 2;

    /** 通知类型-系统 */
    Integer NOTIFY_SYSTEM = 1;

    /** 通知类型-作业 */
    Integer NOTIFY_JOB = 3;
}
