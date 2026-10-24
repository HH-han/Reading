package com.readingsystem.exception;

import com.readingsystem.common.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
