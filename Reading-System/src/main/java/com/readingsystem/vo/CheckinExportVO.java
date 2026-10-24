package com.readingsystem.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 打卡记录导出模型
 */
@Data
public class CheckinExportVO {

    @ExcelProperty("打卡ID")
    private Long id;

    @ExcelProperty("用户昵称")
    private String nickname;

    @ExcelProperty("书籍")
    private String bookTitle;

    @ExcelProperty("章节")
    private String chapterTitle;

    @ExcelProperty("打卡类型")
    private String checkinType;

    @ExcelProperty("打卡内容")
    private String content;

    @ExcelProperty("打卡日期")
    private String checkinDate;

    @ExcelProperty("打卡时间")
    private String createTime;
}
