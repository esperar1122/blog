package com.example.blog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "缓存统计响应")
public class CacheStatsResponse {

    @Schema(description = "Redis状态")
    private String status;

    @Schema(description = "Redis版本")
    private String version;

    @Schema(description = "运行模式")
    private String mode;

    @Schema(description = "连接的客户端数")
    private Integer connectedClients;

    @Schema(description = "总键数量")
    private Long totalKeys;

    @Schema(description = "已用内存（字节）")
    private Long usedMemory;

    @Schema(description = "峰值内存（字节）")
    private Long usedMemoryPeak;

    @Schema(description = "内存使用率（%）")
    private Double memoryUsageRate;

    @Schema(description = "内存碎片率")
    private Double memFragmentationRatio;

    @Schema(description = "命中率（%）")
    private Double hitRate;

    @Schema(description = "键空间命中数")
    private Long keyspaceHits;

    @Schema(description = "键空间未命中数")
    private Long keyspaceMisses;

    @Schema(description = "过期键数量")
    private Long expiredKeys;

    @Schema(description = "淘汰键数量")
    private Long evictedKeys;

    @Schema(description = "每秒操作数")
    private Double instantaneousOpsPerSec;

    @Schema(description = "数据库信息")
    private List<DatabaseInfo> databases;

    @Schema(description = "缓存类型分布")
    private Map<String, Long> keyTypeDistribution;

    @Schema(description = "大键列表")
    private List<BigKeyInfo> bigKeys;

    @Schema(description = "热键列表")
    private List<HotKeyInfo> hotKeys;

    @Data
    @Schema(description = "数据库信息")
    public static class DatabaseInfo {
        @Schema(description = "数据库编号")
        private Integer database;

        @Schema(description = "键数量")
        private Long keys;

        @Schema(description = "过期键数量")
        private Long expires;

        @Schema(description = "平均TTL")
        private Long avgTtl;
    }

    @Data
    @Schema(description = "大键信息")
    public static class BigKeyInfo {
        @Schema(description = "键名")
        private String key;

        @Schema(description = "键类型")
        private String type;

        @Schema(description = "大小（字节）")
        private Long size;

        @Schema(description = "元素数量")
        private Long elementCount;
    }

    @Data
    @Schema(description = "热键信息")
    public static class HotKeyInfo {
        @Schema(description = "键名")
        private String key;

        @Schema(description = "访问次数")
        private Long accessCount;

        @Schema(description = "访问频率（次/秒）")
        private Double accessRate;

        @Schema(description = "最后访问时间")
        private String lastAccessTime;
    }
}