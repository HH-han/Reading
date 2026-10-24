package com.readingsystem.log;

import com.readingsystem.entity.SysOperLog;
import com.readingsystem.mapper.SysOperLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 操作日志记录器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperLogRecorder {

    private final SysOperLogMapper operLogMapper;

    @Async
    public void record(SysOperLog operLog) {
        try {
            operLogMapper.insert(operLog);
        } catch (Exception e) {
            log.error("操作日志写入失败: {}", e.getMessage());
        }
    }
}
