package com.readingsystem.enums;

import lombok.Getter;

/**
 * 打卡类型枚举
 */
@Getter
public enum CheckinTypeEnum {

    IMAGE(1, "图片"),
    AUDIO(2, "朗读音频"),
    NOTE(3, "心得笔记"),
    VIDEO(4, "学习录像");

    private final Integer code;
    private final String desc;

    CheckinTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static CheckinTypeEnum of(Integer code) {
        for (CheckinTypeEnum e : values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}
