package com.readingsystem.utils;

import java.time.LocalDate;
import java.time.temporal.WeekFields;

/**
 * 周期标识工具
 */
public class DateUtils {

    /** 周周期标识 如 2026-W42 */
    public static String weekKey(LocalDate date) {
        return date.getYear() + "-W" + date.get(WeekFields.ISO.weekOfWeekBasedYear());
    }

    /** 月周期标识 如 2026-10 */
    public static String monthKey(LocalDate date) {
        return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
    }

    /** 年周期标识 如 2026 */
    public static String yearKey(LocalDate date) {
        return String.valueOf(date.getYear());
    }
}
