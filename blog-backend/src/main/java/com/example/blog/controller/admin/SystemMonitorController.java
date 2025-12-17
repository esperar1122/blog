package com.example.blog.controller.admin;

import com.example.blog.common.Result;
import com.example.blog.annotation.RateLimiter;
import com.example.blog.dto.response.SystemMetricsResponse;
import com.example.blog.dto.response.SystemStatusResponse;
import com.example.blog.dto.response.DatabaseStatusResponse;
import com.example.blog.dto.response.RedisStatusResponse;
import com.example.blog.service.SystemMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统监控控制器
 * 提供系统性能和状态监控API
 */
@Slf4j
@RestController
@RequestMapping("/admin/system")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemMonitorController {

    private final SystemMonitorService systemMonitorService;

    /**
     * 获取系统状态概览
     */
    @GetMapping("/status")
    @RateLimiter(timeWindow = 5, limit = 10, message = "系统状态查询过于频繁，请5秒后再试")
    public Result<SystemStatusResponse> getSystemStatus() {
        log.info("获取系统状态概览");
        SystemStatusResponse status = systemMonitorService.getSystemStatus();
        return Result.success(status);
    }

    /**
     * 获取系统性能指标
     */
    @GetMapping("/metrics")
    @RateLimiter(timeWindow = 5, limit = 8, message = "系统指标查询过于频繁，请5秒后再试")
    public Result<SystemMetricsResponse> getSystemMetrics() {
        log.info("获取系统性能指标");
        SystemMetricsResponse metrics = systemMonitorService.getSystemMetrics();
        return Result.success(metrics);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    @RateLimiter(timeWindow = 3, limit = 20, message = "健康检查过于频繁，请3秒后再试")
    public Result<Map<String, Object>> healthCheck() {
        log.info("执行系统健康检查");
        Map<String, Object> health = systemMonitorService.healthCheck();
        return Result.success(health);
    }

    /**
     * 获取数据库状态监控
     */
    @GetMapping("/database")
    public Result<DatabaseStatusResponse> getDatabaseStatus() {
        log.info("获取数据库状态监控");
        DatabaseStatusResponse status = systemMonitorService.getDatabaseStatus();
        return Result.success(status);
    }

    /**
     * 获取Redis状态监控
     */
    @GetMapping("/redis")
    public Result<RedisStatusResponse> getRedisStatus() {
        log.info("获取Redis状态监控");
        RedisStatusResponse status = systemMonitorService.getRedisStatus();
        return Result.success(status);
    }

    /**
     * 获取应用信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getApplicationInfo() {
        log.info("获取应用信息");
        Map<String, Object> info = systemMonitorService.getApplicationInfo();
        return Result.success(info);
    }

    /**
     * 获取系统负载信息
     */
    @GetMapping("/load")
    public Result<Map<String, Object>> getSystemLoad() {
        log.info("获取系统负载信息");
        Map<String, Object> load = systemMonitorService.getSystemLoad();
        return Result.success(load);
    }
}