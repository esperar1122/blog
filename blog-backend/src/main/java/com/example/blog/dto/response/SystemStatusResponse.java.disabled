package com.example.blog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "系统状态响应")
public class SystemStatusResponse {

    @Schema(description = "系统状态")
    private String status;

    @Schema(description = "系统运行时间（秒）")
    private Long uptime;

    @Schema(description = "系统启动时间")
    private LocalDateTime startTime;

    @Schema(description = "CPU使用率（%）")
    private Double cpuUsage;

    @Schema(description = "内存使用率（%）")
    private Double memoryUsage;

    @Schema(description = "已用内存（MB）")
    private Long usedMemory;

    @Schema(description = "总内存（MB）")
    private Long totalMemory;

    @Schema(description = "磁盘使用率（%）")
    private Double diskUsage;

    @Schema(description = "已用磁盘空间（GB）")
    private Long usedDiskSpace;

    @Schema(description = "总磁盘空间（GB）")
    private Long totalDiskSpace;

    @Schema(description = "系统负载")
    private Double systemLoad;

    @Schema(description = "活跃线程数")
    private Integer activeThreads;

    @Schema(description = "应用状态")
    private String applicationStatus;

    @Schema(description = "数据库状态")
    private String databaseStatus;

    @Schema(description = "Redis状态")
    private String redisStatus;

    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdated;
}