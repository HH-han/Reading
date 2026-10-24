package com.readingsystem.vo;

import com.readingsystem.entity.Circle;
import com.readingsystem.entity.TGroup;
import lombok.Data;

/**
 * 当前用户圈子信息
 */
@Data
public class CircleCurrentVO {

    private Circle circle;

    private TGroup group;

    /** 圈子总人数 */
    private Integer memberCount;
}
