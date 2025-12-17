package com.example.blog.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.common.Result;
import com.example.blog.dto.request.LogQueryRequest;
import com.example.blog.dto.response.LogStatsResponse;
import com.example.blog.entity.OperationLog;
import com.example.blog.service.AdminLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminLogController {

    private final AdminLogService adminLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping
    public Result<IPage<OperationLog>> getLogs(@Valid LogQueryRequest request) {
        log.info("查询操作日志: {}", request);
        IPage<OperationLog> result = adminLogService.getLogPage(request);
        return Result.success(result);
    }

    /**
     * 获取日志详情
     */
    @GetMapping("/{id}")
    public Result<OperationLog> getLogDetail(@PathVariable Long id) {
        log.info("获取日志详情: {}", id);
        OperationLog log = adminLogService.getLogById(id);
        return Result.success(log);
    }

    /**
     * 删除操作日志
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteLog(@PathVariable Long id) {
        log.info("删除操作日志: {}", id);
        adminLogService.deleteLog(id);
        return Result.success();
    }

    /**
     * 批量删除操作日志
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteLogs(@RequestBody List<Long> ids) {
        log.info("批量删除操作日志: {}", ids);
        adminLogService.deleteLogs(ids);
        return Result.success();
    }

    /**
     * 按时间范围删除日志
     */
    @DeleteMapping("/by-time-range")
    public Result<Void> deleteLogsByTimeRange(
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        log.info("按时间范围删除操作日志: {} - {}", startTime, endTime);
        adminLogService.deleteLogsByTimeRange(startTime, endTime);
        return Result.success();
    }

    /**
     * 获取操作日志统计
     */
    @GetMapping("/stats")
    public Result<LogStatsResponse> getLogStats() {
        log.info("获取操作日志统计");
        LogStatsResponse stats = adminLogService.getLogStats();
        return Result.success(stats);
    }

    /**
     * 获取操作类型统计
     */
    @GetMapping("/operation-stats")
    public Result<List<Map<String, Object>>> getOperationStats() {
        log.info("获取操作类型统计");
        List<Map<String, Object>> stats = adminLogService.getOperationStats();
        return Result.success(stats);
    }

    /**
     * 获取用户操作统计
     */
    @GetMapping("/user-stats")
    public Result<List<Map<String, Object>>> getUserStats() {
        log.info("获取用户操作统计");
        List<Map<String, Object>> stats = adminLogService.getUserStats();
        return Result.success(stats);
    }

    /**
     * 获取操作趋势数据
     */
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getOperationTrend(
            @RequestParam(defaultValue = "7") int days) {
        log.info("获取操作趋势数据: {} 天", days);
        List<Map<String, Object>> trend = adminLogService.getOperationTrend(days);
        return Result.success(trend);
    }

    /**
     * 导出操作日志
     */
    @GetMapping("/export")
    public void exportLogs(
            @Valid LogQueryRequest request,
            HttpServletResponse response) {
        log.info("导出操作日志");
        adminLogService.exportLogs(request, response);
    }

    /**
     * 清空所有日志
     */
    @DeleteMapping("/clear")
    public Result<Void> clearAllLogs() {
        log.info("清空所有操作日志");
        adminLogService.clearAllLogs();
        return Result.success();
    }
}