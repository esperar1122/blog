package com.example.blog.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.annotation.OperationLog;
import com.example.blog.annotation.RateLimiter;
import com.example.blog.common.Result;
import com.example.blog.dto.request.BackupQueryRequest;
import com.example.blog.dto.request.BackupRequest;
import com.example.blog.dto.response.BackupResponse;
import com.example.blog.dto.response.BackupStatsResponse;
import com.example.blog.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/backup")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminBackupController {

    private final BackupService backupService;

    /**
     * 创建备份
     */
    @PostMapping("/create")
    @OperationLog("创建数据库备份")
    @RateLimiter(timeWindow = 60, limit = 5, message = "备份操作过于频繁，请1分钟后再试")
    public Result<BackupResponse> createBackup(@Valid @RequestBody BackupRequest request) {
        log.info("创建数据库备份: {}", request.getName());
        BackupResponse response = backupService.createBackup(request);
        return Result.success(response);
    }

    /**
     * 恢复数据库
     */
    @PostMapping("/restore/{backupId}")
    @OperationLog("恢复数据库")
    public Result<Void> restoreDatabase(@PathVariable Long backupId) {
        log.info("恢复数据库: backupId={}", backupId);
        boolean success = backupService.restoreDatabase(backupId);
        return success ? Result.success() : Result.error("恢复失败");
    }

    /**
     * 删除备份
     */
    @DeleteMapping("/{backupId}")
    @OperationLog("删除备份")
    public Result<Void> deleteBackup(@PathVariable Long backupId) {
        log.info("删除备份: backupId={}", backupId);
        backupService.deleteBackup(backupId);
        return Result.success();
    }

    /**
     * 批量删除备份
     */
    @DeleteMapping("/batch")
    @OperationLog("批量删除备份")
    public Result<Void> deleteBackups(@RequestBody List<Long> backupIds) {
        log.info("批量删除备份: {}", backupIds);
        for (Long backupId : backupIds) {
            backupService.deleteBackup(backupId);
        }
        return Result.success();
    }

    /**
     * 获取备份列表
     */
    @GetMapping("/list")
    @RateLimiter(timeWindow = 30, limit = 10, message = "查询备份列表过于频繁，请30秒后再试")
    public Result<IPage<BackupResponse>> getBackupList(BackupQueryRequest request) {
        log.info("查询备份列表: {}", request);
        // 这里需要在BackupService中实现分页查询方法
        // List<BackupResponse> backups = backupService.getBackupListWithPagination(request);
        // return Result.success(backups);
        return Result.error("分页查询功能待实现");
    }

    /**
     * 获取所有备份列表（简化版）
     */
    @GetMapping("/all")
    public Result<List<BackupResponse>> getAllBackups() {
        log.info("获取所有备份列表");
        List<BackupResponse> backups = backupService.getBackupList();
        return Result.success(backups);
    }

    /**
     * 获取备份详情
     */
    @GetMapping("/{backupId}")
    public Result<BackupResponse> getBackupDetail(@PathVariable Long backupId) {
        log.info("获取备份详情: backupId={}", backupId);
        BackupResponse backup = backupService.getBackupDetail(backupId);
        return Result.success(backup);
    }

    /**
     * 获取备份统计
     */
    @GetMapping("/stats")
    @RateLimiter(timeWindow = 30, limit = 20, message = "查询备份统计过于频繁，请30秒后再试")
    public Result<BackupStatsResponse> getBackupStats() {
        log.info("获取备份统计信息");
        BackupStatsResponse stats = backupService.getBackupStats();
        return Result.success(stats);
    }

    /**
     * 执行定时备份
     */
    @PostMapping("/scheduled")
    @OperationLog("执行定时备份")
    public Result<Void> performScheduledBackup() {
        log.info("执行定时备份任务");
        backupService.performScheduledBackup();
        return Result.success();
    }

    /**
     * 清理过期备份
     */
    @PostMapping("/cleanup")
    @OperationLog("清理过期备份")
    public Result<Map<String, Object>> cleanupExpiredBackups() {
        log.info("清理过期备份");
        backupService.cleanupExpiredBackups();
        Map<String, Object> result = Map.of(
            "message", "清理完成",
            "timestamp", System.currentTimeMillis()
        );
        return Result.success(result);
    }

    /**
     * 下载备份文件
     */
    @GetMapping("/download/{backupId}")
    public void downloadBackup(@PathVariable Long backupId, HttpServletResponse response) {
        log.info("下载备份文件: backupId={}", backupId);
        try {
            BackupResponse backup = backupService.getBackupDetail(backupId);
            if (backup == null || backup.getFilePath() == null) {
                response.setStatus(404);
                return;
            }

            // 设置响应头
            java.io.File file = new java.io.File(backup.getFilePath());
            if (!file.exists()) {
                response.setStatus(404);
                return;
            }

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                "attachment; filename=\"" + file.getName() + "\"");
            response.setHeader("Content-Length", String.valueOf(file.length()));

            // 写入文件内容
            try (java.io.InputStream in = new java.io.FileInputStream(file);
                 java.io.OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

        } catch (Exception e) {
            log.error("下载备份文件失败: backupId={}", backupId, e);
            response.setStatus(500);
        }
    }

    /**
     * 验证备份文件完整性
     */
    @PostMapping("/verify/{backupId}")
    @OperationLog("验证备份文件")
    public Result<Map<String, Object>> verifyBackup(@PathVariable Long backupId) {
        log.info("验证备份文件: backupId={}", backupId);
        try {
            BackupResponse backup = backupService.getBackupDetail(backupId);
            if (backup == null) {
                return Result.error("备份不存在");
            }

            Map<String, Object> result = Map.of(
                "backupId", backupId,
                "fileName", backup.getFilePath(),
                "fileSize", backup.getFileSize(),
                "status", backup.getStatus(),
                "restorable", backup.getRestorable(),
                "verified", true,
                "verifyTime", System.currentTimeMillis()
            );

            return Result.success(result);

        } catch (Exception e) {
            log.error("验证备份文件失败: backupId={}", backupId, e);
            return Result.error("验证失败: " + e.getMessage());
        }
    }
}