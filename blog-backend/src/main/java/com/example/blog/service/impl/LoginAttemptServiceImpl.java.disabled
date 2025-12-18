package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.blog.entity.LoginAttempt;
import com.example.blog.mapper.LoginAttemptMapper;
import com.example.blog.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private final LoginAttemptMapper loginAttemptMapper;

    @Value("${security.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.login.lock-time:30}")
    private int lockTimeMinutes;

    @Override
    public void recordFailedAttempt(String username, String ipAddress) {
        log.debug("记录登录失败尝试 - username: {}, ip: {}", username, ipAddress);

        LambdaQueryWrapper<LoginAttempt> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LoginAttempt::getUsername, username)
                   .eq(LoginAttempt::getIpAddress, ipAddress);

        LoginAttempt loginAttempt = loginAttemptMapper.selectOne(queryWrapper);

        if (loginAttempt == null) {
            loginAttempt = new LoginAttempt();
            loginAttempt.setUsername(username);
            loginAttempt.setIpAddress(ipAddress);
            loginAttempt.setAttemptCount(1);
            loginAttempt.setLastAttemptTime(LocalDateTime.now());
            loginAttempt.setCreatedAt(LocalDateTime.now());
            loginAttempt.setUpdatedAt(LocalDateTime.now());
            loginAttemptMapper.insert(loginAttempt);
        } else {
            int newCount = loginAttempt.getAttemptCount() + 1;
            loginAttempt.setAttemptCount(newCount);
            loginAttempt.setLastAttemptTime(LocalDateTime.now());
            loginAttempt.setUpdatedAt(LocalDateTime.now());

            if (newCount >= maxAttempts) {
                LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(lockTimeMinutes);
                loginAttempt.setLockedUntil(lockUntil);
                log.warn("账户已锁定 - username: {}, ip: {}, 锁定至: {}",
                        username, ipAddress, lockUntil);
            }

            loginAttemptMapper.updateById(loginAttempt);
        }
    }

    @Override
    public boolean isLocked(String username, String ipAddress) {
        LambdaQueryWrapper<LoginAttempt> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LoginAttempt::getUsername, username)
                   .eq(LoginAttempt::getIpAddress, ipAddress)
                   .isNotNull(LoginAttempt::getLockedUntil);

        LoginAttempt loginAttempt = loginAttemptMapper.selectOne(queryWrapper);

        if (loginAttempt == null || loginAttempt.getLockedUntil() == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(loginAttempt.getLockedUntil())) {
            // 锁定时间已过，清除锁定状态
            clearLock(loginAttempt);
            return false;
        }

        return true;
    }

    @Override
    public long getRemainingLockTime(String username, String ipAddress) {
        LambdaQueryWrapper<LoginAttempt> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LoginAttempt::getUsername, username)
                   .eq(LoginAttempt::getIpAddress, ipAddress)
                   .isNotNull(LoginAttempt::getLockedUntil);

        LoginAttempt loginAttempt = loginAttemptMapper.selectOne(queryWrapper);

        if (loginAttempt == null || loginAttempt.getLockedUntil() == null) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(loginAttempt.getLockedUntil())) {
            return 0;
        }

        return java.time.Duration.between(now, loginAttempt.getLockedUntil()).toMinutes();
    }

    @Override
    public void clearFailedAttempts(String username, String ipAddress) {
        LambdaQueryWrapper<LoginAttempt> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LoginAttempt::getUsername, username)
                   .eq(LoginAttempt::getIpAddress, ipAddress);

        LoginAttempt loginAttempt = loginAttemptMapper.selectOne(queryWrapper);
        if (loginAttempt != null) {
            loginAttemptMapper.deleteById(loginAttempt.getId());
            log.debug("清除登录失败记录 - username: {}, ip: {}", username, ipAddress);
        }
    }

    @Override
    public void resetAttempts(String username, String ipAddress) {
        LambdaUpdateWrapper<LoginAttempt> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(LoginAttempt::getUsername, username)
                    .eq(LoginAttempt::getIpAddress, ipAddress)
                    .set(LoginAttempt::getAttemptCount, 0)
                    .set(LoginAttempt::getLockedUntil, null)
                    .set(LoginAttempt::getUpdatedAt, LocalDateTime.now());

        loginAttemptMapper.update(null, updateWrapper);
        log.debug("重置登录失败计数 - username: {}, ip: {}", username, ipAddress);
    }

    @Override
    public int getAttemptCount(String username, String ipAddress) {
        LambdaQueryWrapper<LoginAttempt> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LoginAttempt::getUsername, username)
                   .eq(LoginAttempt::getIpAddress, ipAddress);

        LoginAttempt loginAttempt = loginAttemptMapper.selectOne(queryWrapper);
        return loginAttempt != null ? loginAttempt.getAttemptCount() : 0;
    }

    private void clearLock(LoginAttempt loginAttempt) {
        loginAttempt.setLockedUntil(null);
        loginAttempt.setAttemptCount(0);
        loginAttempt.setUpdatedAt(LocalDateTime.now());
        loginAttemptMapper.updateById(loginAttempt);
        log.debug("自动清除锁定状态 - username: {}, ip: {}",
                loginAttempt.getUsername(), loginAttempt.getIpAddress());
    }
}