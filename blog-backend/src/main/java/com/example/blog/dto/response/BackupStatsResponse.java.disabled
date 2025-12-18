package com.example.blog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "备份统计响应")
public class BackupStatsResponse {

    @Schema(description = "总备份数")
    private Long totalBackups;

    @Schema(description = "成功备份数")
    private Long successBackups;

    @Schema(description = "失败备份数")
    private Long failedBackups;

    @Schema(description = "正在进行的备份数")
    private Long inProgressBackups;

    @Schema(description = "成功率")
    private Double successRate;

    @Schema(description = "总备份大小（字节）")
    private Long totalBackupSize;

    @Schema(description = "平均备份大小（字节）")
    private Long averageBackupSize;

    @Schema(description = "最近备份时间")
    private String lastBackupTime;

    @Schema(description = "最近备份状态")
    private String lastBackupStatus;

    @Schema(description = "存储空间使用率")
    private Double storageUsageRate;

    @Schema(description = "今日备份统计")
    private Map<String, Object> todayStats;

    @Schema(description = "本周备份统计")
    private Map<String, Object> weekStats;

    @Schema(description = "本月备份统计")
    private Map<String, Object> monthStats;

    @Schema(description = "按类型统计")
    private Map<String, Object> backupTypeStats;
}