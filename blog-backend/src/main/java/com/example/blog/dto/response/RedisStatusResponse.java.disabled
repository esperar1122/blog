package com.example.blog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "Redis状态响应")
public class RedisStatusResponse {

    @Schema(description = "Redis状态")
    private String status;

    @Schema(description = "Redis版本")
    private String version;

    @Schema(description = "运行模式")
    private String mode;

    @Schema(description = "操作系统")
    private String os;

    @Schema(description = "架构")
    private String arch;

    @Schema(description = "进程ID")
    private Integer processId;

    @Schema(description = "运行时间（秒）")
    private Long uptimeInSeconds;

    @Schema(description = "客户端连接信息")
    private ClientInfo clientInfo;

    @Schema(description = "内存使用情况")
    private MemoryInfo memoryInfo;

    @Schema(description = "持久化信息")
    private PersistenceInfo persistenceInfo;

    @Schema(description = "统计信息")
    private StatsInfo statsInfo;

    @Schema(description = "复制信息")
    private ReplicationInfo replicationInfo;

    @Schema(description = "CPU使用情况")
    private CpuInfo cpuInfo;

    @Schema(description = "键空间信息")
    private List<KeyspaceInfo> keyspaces;

    @Schema(description = "慢查询日志")
    private SlowLogInfo slowLog;

    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdated;

    @Data
    @Schema(description = "客户端连接信息")
    public static class ClientInfo {
        @Schema(description = "连接的客户端数")
        private Integer connectedClients;

        @Schema(description = "客户端最大缓冲区")
        private Long clientRecentMaxInputBuffer;

        @Schema(description = "客户端最大输出缓冲区")
        private Long clientRecentMaxOutputBuffer;

        @Schema(description = "阻塞的客户端数")
        private Integer blockedClients;

        @Schema(description = "跟踪的客户端数")
        private Integer trackingClients;

        @Schema(description = "客户端超时数量")
        private Integer clientTimeouts;
    }

    @Data
    @Schema(description = "内存使用情况")
    public static class MemoryInfo {
        @Schema(description = "已用内存（字节）")
        private Long usedMemory;

        @Schema(description = "峰值内存（字节）")
        private Long usedMemoryPeak;

        @Schema(description = "系统内存（字节）")
        private Long totalSystemMemory;

        @Schema(description = "内存碎片率")
        private Double memFragmentationRatio;

        @Schema(description = "内存使用率（%）")
        private Double memoryUsagePercent;

        @Schema(description = "Lua脚本使用内存（字节）")
        private Long usedMemoryLua;

        @Schema(description = "数据集大小（字节）")
        private Long datasetSize;
    }

    @Data
    @Schema(description = "持久化信息")
    public static class PersistenceInfo {
        @Schema(description = "是否启用AOF")
        private Boolean aofEnabled;

        @Schema(description = "是否启用RDB")
        private Boolean rdbEnabled;

        @Schema(description = "RDB保存是否成功")
        private Boolean rdbSaveSuccess;

        @Schema(description = "最后保存时间")
        private String rdbLastSaveTime;

        @Schema(description = "AOF重写是否进行中")
        private Boolean aofRewriteInProgress;

        @Schema(description = "RDB保存是否进行中")
        private Boolean rdbSaveInProgress;

        @Schema(description = "AOF当前大小（字节）")
        private Long aofCurrentSize;

        @Schema(description = "AOF基础大小（字节）")
        private Long aofBaseSize;
    }

    @Data
    @Schema(description = "统计信息")
    public static class StatsInfo {
        @Schema(description = "总连接数")
        private Long totalConnectionsReceived;

        @Schema(description = "总命令数")
        private Long totalCommandsProcessed;

        @Schema(description = "每秒操作数")
        private Double instantaneousOpsPerSec;

        @Schema(description = "键空间命中数")
        private Long keyspaceHits;

        @Schema(description = "键空间未命中数")
        private Long keyspaceMisses;

        @Schema(description = "缓存命中率（%）")
        private Double hitRate;

        @Schema(description = "过期键数量")
        private Long expiredKeys;

        @Schema(description = "淘汰键数量")
        private Long evictedKeys;
    }

    @Data
    @Schema(description = "复制信息")
    public static class ReplicationInfo {
        @Schema(description = "角色（master/slave）")
        private String role;

        @Schema(description = "连接的从节点数")
        private Integer connectedSlaves;

        @Schema(description = "主节点IP")
        private String masterHost;

        @Schema(description = "主节点端口")
        private Integer masterPort;

        @Schema(description = "主从同步偏移量")
        private Long masterReplOffset;
    }

    @Data
    @Schema(description = "CPU使用情况")
    public static class CpuInfo {
        @Schema(description = "CPU使用率")
        private Double usedCpu;

        @Schema(description = "用户态CPU使用率")
        private Double usedCpuUser;

        @Schema(description = "系统态CPU使用率")
        private Double usedCpuSys;

        @Schema(description = "子进程CPU使用率")
        private Double usedCpuUserChildren;

        @Schema(description = "子进程系统态CPU使用率")
        private Double usedCpuSysChildren;
    }

    @Data
    @Schema(description = "键空间信息")
    public static class KeyspaceInfo {
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
    @Schema(description = "慢查询日志")
    public static class SlowLogInfo {
        @Schema(description = "慢查询日志长度")
        private Integer slowLogLength;

        @Schema(description = "慢查询日志最大长度")
        private Integer slowLogMaxLen;

        @Schema(description = "慢查询阈值（微秒）")
        private Long slowLogSlowerThan;
    }
}