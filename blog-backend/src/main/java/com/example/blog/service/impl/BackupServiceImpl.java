package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.request.BackupQueryRequest;
import com.example.blog.dto.request.BackupRequest;
import com.example.blog.dto.response.BackupResponse;
import com.example.blog.dto.response.BackupStatsResponse;
import com.example.blog.entity.BackupRecord;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.BackupRecordMapper;
import com.example.blog.service.BackupService;
import com.example.blog.utils.DatabaseBackupUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupServiceImpl implements BackupService {

    private final BackupRecordMapper backupRecordMapper;
    private final DatabaseBackupUtil databaseBackupUtil;

    @Override
    @Transactional
    public BackupResponse createBackup(BackupRequest request) {
        log.info("开始创建数据库备份: {}", request.getName());

        BackupRecord backupRecord = new BackupRecord();
        BeanUtils.copyProperties(request, backupRecord);

        // 设置创建者信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            backupRecord.setCreatedByName(auth.getName());
        }

        backupRecord.setStatus("IN_PROGRESS");
        backupRecord.setCreateTime(LocalDateTime.now());

        // 保存备份记录
        backupRecordMapper.insert(backupRecord);

        // 异步执行备份
        performAsyncBackup(backupRecord);

        // 转换为响应对象
        BackupResponse response = convertToResponse(backupRecord);
        return response;
    }

    @Async
    public void performAsyncBackup(BackupRecord backupRecord) {
        try {
            log.info("开始异步执行数据库备份: {}", backupRecord.getName());

            long startTime = System.currentTimeMillis();

            // 执行数据库备份
            String backupFilePath = databaseBackupUtil.createDatabaseBackup(backupRecord);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // 更新备份记录
            backupRecord.setFilePath(backupFilePath);
            backupRecord.setFinishTime(LocalDateTime.now());
            backupRecord.setDuration(duration);
            backupRecord.setStatus("SUCCESS");

            // 获取文件大小
            try {
                File backupFile = new File(backupFilePath);
                backupRecord.setFileSize(backupFile.length());
            } catch (Exception e) {
                log.warn("获取备份文件大小失败", e);
            }

            // 生成校验和
            try {
                backupRecord.setChecksum(databaseBackupUtil.generateChecksum(backupFilePath));
            } catch (Exception e) {
                log.warn("生成备份文件校验和失败", e);
            }

            backupRecordMapper.updateById(backupRecord);

            log.info("数据库备份完成: {}, 耗时: {}ms", backupRecord.getName(), duration);

        } catch (Exception e) {
            log.error("数据库备份失败: {}", backupRecord.getName(), e);

            // 更新失败状态
            backupRecord.setFinishTime(LocalDateTime.now());
            backupRecord.setStatus("FAILED");
            backupRecord.setErrorMessage(e.getMessage());
            backupRecordMapper.updateById(backupRecord);
        }
    }

    @Override
    @Transactional
    public boolean restoreDatabase(Long backupId) {
        log.info("开始恢复数据库: backupId={}", backupId);

        BackupRecord backupRecord = backupRecordMapper.selectById(backupId);
        if (backupRecord == null) {
            throw new BusinessException("备份记录不存在");
        }

        if (!"SUCCESS".equals(backupRecord.getStatus())) {
            throw new BusinessException("只能恢复成功的备份");
        }

        try {
            // 执行数据库恢复
            databaseBackupUtil.restoreDatabase(backupRecord.getFilePath());

            log.info("数据库恢复成功: {}", backupRecord.getName());
            return true;

        } catch (Exception e) {
            log.error("数据库恢复失败: {}", backupRecord.getName(), e);
            throw new BusinessException("数据库恢复失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteBackup(Long backupId) {
        log.info("删除备份: backupId={}", backupId);

        BackupRecord backupRecord = backupRecordMapper.selectById(backupId);
        if (backupRecord == null) {
            throw new BusinessException("备份记录不存在");
        }

        try {
            // 删除备份文件
            if (backupRecord.getFilePath() != null) {
                Files.deleteIfExists(Paths.get(backupRecord.getFilePath()));
            }

            // 删除数据库记录
            backupRecordMapper.deleteById(backupId);

            log.info("备份删除成功: {}", backupRecord.getName());

        } catch (Exception e) {
            log.error("备份删除失败: {}", backupRecord.getName(), e);
            throw new BusinessException("备份删除失败: " + e.getMessage());
        }
    }

    @Override
    public List<BackupResponse> getBackupList() {
        BackupQueryRequest request = new BackupQueryRequest();
        request.setSize(100); // 限制返回数量

        Page<BackupRecord> page = new Page<>(1, request.getSize());
        IPage<BackupRecord> backupPage = backupRecordMapper.selectBackupPage(page, request);

        return backupPage.getRecords().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    @Override
    public BackupResponse getBackupDetail(Long backupId) {
        BackupRecord backupRecord = backupRecordMapper.selectById(backupId);
        if (backupRecord == null) {
            throw new BusinessException("备份记录不存在");
        }

        BackupResponse response = convertToResponse(backupRecord);

        // 添加备份统计信息
        if ("SUCCESS".equals(backupRecord.getStatus())) {
            try {
                BackupStatsResponse stats = getBackupFileStats(backupRecord.getFilePath());
                response.setStats(stats);
            } catch (Exception e) {
                log.warn("获取备份文件统计失败", e);
            }
        }

        return response;
    }

    @Override
    public BackupStatsResponse getBackupStats() {
        Map<String, Object> statsMap = backupRecordMapper.getBackupStats();
        BackupStatsResponse response = new BackupStatsResponse();

        // 总备份数
        response.setTotalBackups(statsMap.containsKey("total") ? ((Number) statsMap.get("total")).longValue() : 0L);

        // 成功备份数
        response.setSuccessBackups(statsMap.containsKey("success") ? ((Number) statsMap.get("success")).longValue() : 0L);

        // 失败备份数
        response.setFailedBackups(statsMap.containsKey("failed") ? ((Number) statsMap.get("failed")).longValue() : 0L);

        // 正在进行的备份数
        response.setInProgressBackups(statsMap.containsKey("in_progress") ? ((Number) statsMap.get("in_progress")).longValue() : 0L);

        // 成功率
        if (response.getTotalBackups() > 0) {
            response.setSuccessRate((double) response.getSuccessBackups() / response.getTotalBackups() * 100);
        }

        // 总备份大小
        response.setTotalBackupSize(statsMap.containsKey("total_size") ? ((Number) statsMap.get("total_size")).longValue() : 0L);

        // 平均备份大小
        if (response.getSuccessBackups() > 0) {
            response.setAverageBackupSize(response.getTotalBackupSize() / response.getSuccessBackups());
        }

        // 最近备份信息
        BackupRecord latestBackup = backupRecordMapper.getLatestBackup();
        if (latestBackup != null) {
            response.setLastBackupTime(latestBackup.getCreateTime().toString());
            response.setLastBackupStatus(latestBackup.getStatus());
        }

        // 按类型统计
        List<Map<String, Object>> typeStats = backupRecordMapper.countByBackupType();
        response.setBackupTypeStats(Map.of("byType", typeStats));

        return response;
    }

    @Override
    @Async
    public void performScheduledBackup() {
        log.info("执行定时备份任务");

        try {
            BackupRequest request = new BackupRequest();
            request.setName("scheduled_backup_" + System.currentTimeMillis());
            request.setDescription("定时自动备份");
            request.setBackupType("FULL");
            request.setCompressed(true);
            request.setIncludeStructure(true);
            request.setIncludeData(true);

            createBackup(request);

        } catch (Exception e) {
            log.error("定时备份任务失败", e);
        }
    }

    @Override
    @Transactional
    public void cleanupExpiredBackups() {
        log.info("开始清理过期备份");

        LocalDateTime expireTime = LocalDateTime.now().minusDays(30); // 保留30天

        try {
            List<BackupRecord> expiredBackups = backupRecordMapper.getBackupsByTimeRange(null, expireTime);

            for (BackupRecord backup : expiredBackups) {
                try {
                    // 删除备份文件
                    if (backup.getFilePath() != null) {
                        Files.deleteIfExists(Paths.get(backup.getFilePath()));
                    }

                    // 删除数据库记录
                    backupRecordMapper.deleteById(backup.getId());

                    log.info("清理过期备份: {}", backup.getName());
                } catch (Exception e) {
                    log.error("清理过期备份失败: {}", backup.getName(), e);
                }
            }

            // 批量删除过期记录
            backupRecordMapper.deleteExpiredBackups(expireTime);

            log.info("过期备份清理完成");

        } catch (Exception e) {
            log.error("清理过期备份失败", e);
        }
    }

    private BackupResponse convertToResponse(BackupRecord record) {
        BackupResponse response = new BackupResponse();
        BeanUtils.copyProperties(record, response);

        // 设置是否可恢复
        response.setRestorable("SUCCESS".equals(record.getStatus()) && record.getFilePath() != null);

        return response;
    }

    private BackupStatsResponse getBackupFileStats(String filePath) {
        BackupStatsResponse stats = new BackupStatsResponse();

        try {
            File file = new File(filePath);
            stats.setTotalBackupSize(file.length());
            // 可以添加更多文件统计信息
        } catch (Exception e) {
            log.warn("获取备份文件统计失败", e);
        }

        return stats;
    }
}