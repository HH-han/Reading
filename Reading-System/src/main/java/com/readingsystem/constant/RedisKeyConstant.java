package com.readingsystem.constant;

/**
 * Redis Key 常量
 */
public interface RedisKeyConstant {

    /** 圈子个人周榜 ZSet: rank:circle:{circleId}:week:{periodKey} */
    String RANK_CIRCLE_WEEK = "rank:circle:%d:week:%s";

    /** 圈子个人月榜 ZSet */
    String RANK_CIRCLE_MONTH = "rank:circle:%d:month:%s";

    /** 圈子个人年榜 ZSet */
    String RANK_CIRCLE_YEAR = "rank:circle:%d:year:%s";

    /** 圈子小组周榜 ZSet */
    String RANK_GROUP_WEEK = "rank:group:%d:week:%s";

    /** 用户当日打卡锁 */
    String CHECKIN_LOCK = "lock:checkin:%d:%s";
}
