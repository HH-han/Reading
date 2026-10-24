package com.readingsystem.aspect;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.readingsystem.annotation.OperLog;
import com.readingsystem.entity.SysOperLog;
import com.readingsystem.log.OperLogRecorder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/**
 * 操作日志切面
 */
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private final OperLogRecorder operLogRecorder;

    @Around("@annotation(operLogAnn)")
    public Object around(ProceedingJoinPoint joinPoint, OperLog operLogAnn) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long cost = System.currentTimeMillis() - start;

        try {
            SysOperLog operLog = new SysOperLog();
            operLog.setUserId(StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : 0L);
            operLog.setModule(operLogAnn.module());
            operLog.setOperation(operLogAnn.operation());
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            operLog.setMethod(signature.getDeclaringType().getSimpleName() + "." + signature.getName());
            operLog.setParams(safeArgs(joinPoint.getArgs()));
            operLog.setIp(getIp());
            operLog.setCostTime(cost);
            operLogRecorder.record(operLog);
        } catch (Exception ignored) {
            // 日志失败不影响业务
        }
        return result;
    }

    private String safeArgs(Object[] args) {
        try {
            Object[] filtered = Arrays.stream(args)
                    .map(a -> (a instanceof MultipartFile) ? "[file]" : a)
                    .toArray();
            String json = JSONUtil.toJsonStr(filtered);
            return json.length() > 2000 ? json.substring(0, 2000) : json;
        } catch (Exception e) {
            return "";
        }
    }

    private String getIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "";
        }
        HttpServletRequest request = attrs.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
