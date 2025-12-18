package com.example.blog.service;

import com.example.blog.dto.request.BackupRequest;
import com.example.blog.dto.response.BackupResponse;
import com.example.blog.dto.response.BackupStatsResponse;

import java.util.List;

/**
 * 数据备份服务接口
 */
public interface BackupService {

    /**
     * 创建数据库备份
     * @param request 备份请求
     * @return 备份响应
     */
    BackupResponse createBackup(BackupRequest request);

    /**
     * 恢复数据库
     * @param backupId 备份ID
     * @return 恢复结果
     */
    boolean restoreDatabase(Long backupId);

    /**
     * 删除备份文件
     * @param backupId 备份ID
     */
    void deleteBackup(Long backupId);

    /**
     * 获取备份列表
     * @return 备份列表
     */
    List<BackupResponse> getBackupList();

    /**
     * 获取备份详情
     * @param backupId 备份ID
     * @return 备份详情
     */
    BackupResponse getBackupDetail(Long backupId);

    /**
     * 获取备份统计信息
     * @return 备份统计
     */
    BackupStatsResponse getBackupStats();

    /**
     * 执行定时备份
     */
    void performScheduledBackup();

    /**
     * 清理过期备份
     */
    void cleanupExpiredBackups();
}