package com.example.blog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "数据库状态响应")
public class DatabaseStatusResponse {

    @Schema(description = "数据库状态")
    private String status;

    @Schema(description = "数据库版本")
    private String version;

    @Schema(description = "连接URL")
    private String url;

    @Schema(description = "数据库连接池状态")
    private ConnectionPoolStatus connectionPool;

    @Schema(description = "活跃连接数")
    private Integer activeConnections;

    @Schema(description = "空闲连接数")
    private Integer idleConnections;

    @Schema(description = "总连接数")
    private Integer totalConnections;

    @Schema(description = "最大连接数")
    private Integer maxConnections;

    @Schema(description = "连接使用率（%）")
    private Double connectionUsageRate;

    @Schema(description = "数据库大小（MB）")
    private Long databaseSize;

    @Schema(description = "表数量")
    private Integer tableCount;

    @Schema(description = "数据库性能指标")
    private DatabaseMetrics metrics;

    @Schema(description = "表空间使用情况")
    private List<TableSpaceInfo> tableSpaces;

    @Schema(description = "慢查询统计")
    private SlowQueryStats slowQueryStats;

    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdated;

    @Data
    @Schema(description = "连接池状态")
    public static class ConnectionPoolStatus {
        @Schema(description = "活动连接数")
        private Integer active;

        @Schema(description = "空闲连接数")
        private Integer idle;

        @Schema(description = "总连接数")
        private Integer total;

        @Schema(description = "最大连接数")
        private Integer max;

        @Schema(description = "最小空闲连接数")
        private Integer minIdle;

        @Schema(description = "等待获取连接的线程数")
        private Integer waiting;

        @Schema(description = "连接池使用率（%）")
        private Double usagePercent;
    }

    @Data
    @Schema(description = "数据库性能指标")
    public static class DatabaseMetrics {
        @Schema(description = "查询总数")
        private Long totalQueries;

        @Schema(description = "每秒查询数（QPS）")
        private Double queriesPerSecond;

        @Schema(description = "平均查询时间（毫秒）")
        private Double avgQueryTime;

        @Schema(description = "最大查询时间（毫秒）")
        private Long maxQueryTime;

        @Schema(description = "事务总数")
        private Long totalTransactions;

        @Schema(description = "每秒事务数（TPS）")
        private Double transactionsPerSecond;

        @Schema(description = "缓存命中率（%）")
        private Double cacheHitRate;

        @Schema(description = "索引使用率（%）")
        private Double indexUsageRate;
    }

    @Data
    @Schema(description = "表空间信息")
    public static class TableSpaceInfo {
        @Schema(description = "表空间名称")
        private String name;

        @Schema(description = "总大小（MB）")
        private Long totalSize;

        @Schema(description = "已使用大小（MB）")
        private Long usedSize;

        @Schema(description = "使用率（%）")
        private Double usagePercent;

        @Schema(description = "表数量")
        private Integer tableCount;
    }

    @Data
    @Schema(description = "慢查询统计")
    public static class SlowQueryStats {
        @Schema(description = "慢查询总数")
        private Long totalSlowQueries;

        @Schema(description = "今日慢查询数")
        private Long todaySlowQueries;

        @Schema(description = "最慢的查询")
        private Map<String, Object> slowestQuery;

        @Schema(description = "慢查询阈值（秒）")
        private Long slowQueryThreshold;
    }
}