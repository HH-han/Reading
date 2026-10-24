package com.readingsystem.common;

/**
 * 响应码常量
 */
public interface ResultCode {

    Integer SUCCESS = 200;
    Integer FAIL = 500;
    Integer UNAUTHORIZED = 401;
    Integer FORBIDDEN = 403;
    Integer NOT_FOUND = 404;
    Integer PARAM_ERROR = 400;
}
