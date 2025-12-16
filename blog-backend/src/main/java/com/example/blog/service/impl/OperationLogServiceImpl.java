package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.request.LogQueryRequest;
import com.example.blog.dto.response.LogStatsResponse;
import com.example.blog.entity.OperationLog;
import com.example.blog.mapper.OperationLogMapper;
import com.example.blog.service.AdminLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements AdminLogService {

    private final OperationLogMapper operationLogMapper;

    @Override
    public void log(Long adminId, String action, String description) {
        OperationLog operationLog = new OperationLog();
        operationLog.setUserId(adminId);
        operationLog.setOperation(action);
        operationLog.setResult(description);
        operationLog.setStatus(1);
        operationLog.setCreateTime(LocalDateTime.now());

        operationLogMapper.insert(operationLog);
    }

    @Override
    public IPage<OperationLog> getLogPage(LogQueryRequest request) {
        Page<OperationLog> page = new Page<>(request.getCurrent(), request.getSize());
        return operationLogMapper.selectLogPage(page, request);
    }

    @Override
    public OperationLog getLogById(Long id) {
        return operationLogMapper.selectById(id);
    }

    @Override
    public void deleteLog(Long id) {
        operationLogMapper.deleteById(id);
    }

    @Override
    public void deleteLogs(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            operationLogMapper.deleteBatchIds(ids);
        }
    }

    @Override
    public void deleteLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        operationLogMapper.deleteByTimeRange(startTime, endTime);
    }

    @Override
    public LogStatsResponse getLogStats() {
        LogStatsResponse response = new LogStatsResponse();

        // 总日志数
        QueryWrapper<OperationLog> totalWrapper = new QueryWrapper<>();
        response.setTotalLogs(operationLogMapper.selectCount(totalWrapper));

        // 今日日志数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        QueryWrapper<OperationLog> todayWrapper = new QueryWrapper<>();
        todayWrapper.ge("create_time", todayStart);
        response.setTodayLogs(operationLogMapper.selectCount(todayWrapper));

        // 本周日志数
        LocalDateTime weekStart = todayStart.minusDays(7);
        QueryWrapper<OperationLog> weekWrapper = new QueryWrapper<>();
        weekWrapper.ge("create_time", weekStart);
        response.setWeekLogs(operationLogMapper.selectCount(weekWrapper));

        // 本月日志数
        LocalDateTime monthStart = todayStart.withDayOfMonth(1);
        QueryWrapper<OperationLog> monthWrapper = new QueryWrapper<>();
        monthWrapper.ge("create_time", monthStart);
        response.setMonthLogs(operationLogMapper.selectCount(monthWrapper));

        // 成功/失败统计
        QueryWrapper<OperationLog> successWrapper = new QueryWrapper<>();
        successWrapper.eq("status", 1);
        response.setSuccessCount(operationLogMapper.selectCount(successWrapper));

        QueryWrapper<OperationLog> failureWrapper = new QueryWrapper<>();
        failureWrapper.eq("status", 0);
        response.setFailureCount(operationLogMapper.selectCount(failureWrapper));

        if (response.getTotalLogs() > 0) {
            response.setSuccessRate((double) response.getSuccessCount() / response.getTotalLogs() * 100);
        }

        // 平均响应时间
        QueryWrapper<OperationLog> avgTimeWrapper = new QueryWrapper<>();
        avgTimeWrapper.select("AVG(time) as avgTime");
        Map<String, Object> avgResult = operationLogMapper.selectMaps(avgTimeWrapper).get(0);
        if (avgResult.get("avgTime") != null) {
            response.setAvgResponseTime(((Number) avgResult.get("avgTime")).doubleValue());
        }

        // 获取其他统计数据
        response.setTopUsers(getUserStats());
        response.setTopOperations(getOperationStats());
        response.setDailyStats(getOperationTrend(7));

        return response;
    }

    @Override
    public List<Map<String, Object>> getOperationStats() {
        return operationLogMapper.countByOperation();
    }

    @Override
    public List<Map<String, Object>> getUserStats() {
        return operationLogMapper.countByUser();
    }

    @Override
    public List<Map<String, Object>> getOperationTrend(int days) {
        return operationLogMapper.getOperationTrend(days);
    }

    @Override
    public void exportLogs(LogQueryRequest request, HttpServletResponse response) {
        try {
            // 设置响应头
            response.setContentType("text/csv");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition",
                "attachment; filename=operation_logs_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

            // 获取数据
            request.setSize(10000); // 限制导出数量
            IPage<OperationLog> logs = getLogPage(request);

            // 写入CSV
            try (OutputStream out = response.getOutputStream();
                 PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {

                // 写入BOM以支持中文
                out.write(0xEF);
                out.write(0xBB);
                out.write(0xBF);

                // 写入表头
                writer.println("ID,用户名,操作类型,请求方法,执行时长(ms),IP地址,用户代理,操作结果,状态,创建时间");

                // 写入数据
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                for (OperationLog log : logs.getRecords()) {
                    writer.println(String.format("%d,%s,%s,%s,%d,%s,%s,%s,%s,%s",
                        log.getId(),
                        log.getUsername(),
                        log.getOperation(),
                        log.getMethod(),
                        log.getTime(),
                        log.getIp(),
                        escapeCsv(log.getUserAgent()),
                        escapeCsv(log.getResult()),
                        log.getStatus() == 1 ? "成功" : "失败",
                        log.getCreateTime().format(formatter)
                    ));
                }
            }

        } catch (IOException e) {
            log.error("导出日志失败", e);
            throw new RuntimeException("导出日志失败", e);
        }
    }

    @Override
    public void clearAllLogs() {
        operationLogMapper.delete(null);
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}