package com.example.blog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "日志统计响应")
public class LogStatsResponse {

    @Schema(description = "总日志数")
    private Long totalLogs;

    @Schema(description = "今日日志数")
    private Long todayLogs;

    @Schema(description = "本周日志数")
    private Long weekLogs;

    @Schema(description = "本月日志数")
    private Long monthLogs;

    @Schema(description = "成功操作数")
    private Long successCount;

    @Schema(description = "失败操作数")
    private Long failureCount;

    @Schema(description = "成功率")
    private Double successRate;

    @Schema(description = "平均响应时间（毫秒）")
    private Double avgResponseTime;

    @Schema(description = "最活跃用户")
    private List<Map<String, Object>> topUsers;

    @Schema(description = "最常用操作")
    private List<Map<String, Object>> topOperations;

    @Schema(description = "最近7天的统计数据")
    private List<Map<String, Object>> dailyStats;

    @Schema(description = "按小时统计的数据")
    private List<Map<String, Object>> hourlyStats;
}