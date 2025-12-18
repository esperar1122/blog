package com.example.blog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "系统指标响应")
public class SystemMetricsResponse {

    @Schema(description = "JVM内存指标")
    private JvmMemoryMetrics jvmMemory;

    @Schema(description = "CPU指标")
    private CpuMetrics cpu;

    @Schema(description = "内存指标")
    private MemoryMetrics memory;

    @Schema(description = "垃圾回收指标")
    private List<GcMetrics> garbageCollection;

    @Schema(description = "线程指标")
    private ThreadMetrics threads;

    @Schema(description = "类加载指标")
    private ClassLoadingMetrics classLoading;

    @Schema(description = "自定义指标")
    private Map<String, Object> customMetrics;

    @Data
    @Schema(description = "JVM内存指标")
    public static class JvmMemoryMetrics {
        @Schema(description = "已用堆内存（MB）")
        private Long usedHeapMemory;

        @Schema(description = "最大堆内存（MB）")
        private Long maxHeapMemory;

        @Schema(description = "已用非堆内存（MB）")
        private Long usedNonHeapMemory;

        @Schema(description = "最大非堆内存（MB）")
        private Long maxNonHeapMemory;

        @Schema(description = "堆内存使用率（%）")
        private Double heapMemoryUsage;

        @Schema(description = "非堆内存使用率（%）")
        private Double nonHeapMemoryUsage;
    }

    @Data
    @Schema(description = "CPU指标")
    public static class CpuMetrics {
        @Schema(description = "系统CPU使用率（%）")
        private Double systemCpuUsage;

        @Schema(description = "进程CPU使用率（%）")
        private Double processCpuUsage;

        @Schema(description = "可用CPU核心数")
        private Integer availableProcessors;

        @Schema(description = "CPU负载平均值")
        private Double systemLoadAverage;
    }

    @Data
    @Schema(description = "内存指标")
    public static class MemoryMetrics {
        @Schema(description = "系统总内存（MB）")
        private Long totalSystemMemory;

        @Schema(description = "系统已用内存（MB）")
        private Long usedSystemMemory;

        @Schema(description = "系统可用内存（MB）")
        private Long freeSystemMemory;

        @Schema(description = "系统内存使用率（%）")
        private Double systemMemoryUsage;

        @Schema(description = "交换空间总大小（MB）")
        private Long totalSwapSpace;

        @Schema(description = "已用交换空间（MB）")
        private Long usedSwapSpace;

        @Schema(description = "交换空间使用率（%）")
        private Double swapSpaceUsage;
    }

    @Data
    @Schema(description = "垃圾回收指标")
    public static class GcMetrics {
        @Schema(description = "GC名称")
        private String name;

        @Schema(description = "GC次数")
        private Long collectionCount;

        @Schema(description = "GC耗时（毫秒）")
        private Long collectionTime;
    }

    @Data
    @Schema(description = "线程指标")
    public static class ThreadMetrics {
        @Schema(description = "当前线程数")
        private Integer threadCount;

        @Schema(description = "峰值线程数")
        private Integer peakThreadCount;

        @Schema(description = "守护线程数")
        private Integer daemonThreadCount;

        @Schema(description = "总启动线程数")
        private Long totalStartedThreadCount;

        @Schema(description = "阻塞线程数")
        private Integer blockedThreadCount;

        @Schema(description = "等待线程数")
        private Integer waitingThreadCount;
    }

    @Data
    @Schema(description = "类加载指标")
    public static class ClassLoadingMetrics {
        @Schema(description = "已加载类数")
        private Long loadedClassCount;

        @Schema(description = "总加载类数")
        private Long totalLoadedClassCount;

        @Schema(description = "已卸载类数")
        private Long unloadedClassCount;
    }
}