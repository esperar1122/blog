package com.example.blog.service.impl;

import com.example.blog.entity.AdminLog;
import com.example.blog.mapper.AdminLogMapper;
import com.example.blog.service.AdminLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminLogServiceImpl implements AdminLogService {

    private final AdminLogMapper adminLogMapper;

    @Override
    public void log(Long adminId, String action, String description) {
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setOperation(action);
        log.setDetails(description);
        log.setCreateTime(LocalDateTime.now());

        adminLogMapper.insert(log);
    }
}