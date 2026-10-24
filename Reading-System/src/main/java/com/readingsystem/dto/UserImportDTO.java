package com.readingsystem.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 初始用户导入模型
 */
@Data
public class UserImportDTO {

    @ExcelProperty("登录账号")
    private String username;

    @ExcelProperty("昵称")
    private String nickname;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("职位")
    private String position;
}
