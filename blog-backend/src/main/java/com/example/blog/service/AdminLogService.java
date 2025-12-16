package com.example.blog.service;

public interface AdminLogService {

    void log(Long adminId, String action, String description);
}