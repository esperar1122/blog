package com.example.blog.service;

import com.example.blog.dto.response.SystemMetricsResponse;
import com.example.blog.dto.response.SystemStatusResponse;
import com.example.blog.dto.response.DatabaseStatusResponse;
import com.example.blog.dto.response.RedisStatusResponse;

import java.util.Map;

/**
 * 系统监控服务接口
 */
public interface SystemMonitorService {

    /**
     * 获取系统状态概览
     * @return 系统状态响应
     */
    SystemStatusResponse getSystemStatus();

    /**
     * 获取系统性能指标
     * @return 系统指标响应
     */
    SystemMetricsResponse getSystemMetrics();

    /**
     * 健康检查
     * @return 健康状态信息
     */
    Map<String, Object> healthCheck();

    /**
     * 获取数据库状态监控
     * @return 数据库状态响应
     */
    DatabaseStatusResponse getDatabaseStatus();

    /**
     * 获取Redis状态监控
     * @return Redis状态响应
     */
    RedisStatusResponse getRedisStatus();

    /**
     * 获取应用信息
     * @return 应用信息
     */
    Map<String, Object> getApplicationInfo();

    /**
     * 获取系统负载信息
     * @return 系统负载信息
     */
    Map<String, Object> getSystemLoad();
}