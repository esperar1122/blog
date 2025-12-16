package com.example.blog.service;

public interface LoginAttemptService {

    void recordFailedAttempt(String username, String ipAddress);

    boolean isLocked(String username, String ipAddress);

    long getRemainingLockTime(String username, String ipAddress);

    void clearFailedAttempts(String username, String ipAddress);

    void resetAttempts(String username, String ipAddress);

    int getAttemptCount(String username, String ipAddress);
}