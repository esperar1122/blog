package com.example.blog.service.impl;

import com.example.blog.service.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 登录尝试服务实现（简化版本）
 */
@Slf4j
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    @Override
    public void recordFailedAttempt(String username, String ipAddress) {
        log.info("记录登录失败 - username: {}, ip: {}", username, ipAddress);
        // 简化版本：只记录日志，不做实际的限制
    }

    @Override
    public boolean isLocked(String username, String ipAddress) {
        // 简化版本：不进行锁定
        return false;
    }

    @Override
    public long getRemainingLockTime(String username, String ipAddress) {
        // 简化版本：不进行锁定
        return 0;
    }

    @Override
    public void clearFailedAttempts(String username, String ipAddress) {
        log.info("清除登录失败记录 - username: {}, ip: {}", username, ipAddress);
    }

    @Override
    public void resetAttempts(String username, String ipAddress) {
        log.info("重置登录尝试计数 - username: {}, ip: {}", username, ipAddress);
    }

    @Override
    public int getAttemptCount(String username, String ipAddress) {
        // 简化版本：返回0
        return 0;
    }
}