package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.request.LogQueryRequest;
import com.example.blog.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    /**
     * 分页查询操作日志
     */
    IPage<OperationLog> selectLogPage(Page<OperationLog> page, @Param("request") LogQueryRequest request);

    /**
     * 根据时间范围删除日志
     */
    int deleteByTimeRange(@Param("startTime") LocalDateTime startTime,
                         @Param("endTime") LocalDateTime endTime);

    /**
     * 统计操作日志数量
     */
    List<Map<String, Object>> countByOperation();

    /**
     * 统计用户操作数量
     */
    List<Map<String, Object>> countByUser();

    /**
     * 获取今日操作统计
     */
    Map<String, Object> getTodayStats();

    /**
     * 获取最近N天的操作趋势
     */
    List<Map<String, Object>> getOperationTrend(@Param("days") int days);
}