package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.request.BackupQueryRequest;
import com.example.blog.entity.BackupRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BackupRecordMapper extends BaseMapper<BackupRecord> {

    /**
     * 分页查询备份记录
     */
    IPage<BackupRecord> selectBackupPage(Page<BackupRecord> page, @Param("request") BackupQueryRequest request);

    /**
     * 获取备份统计信息
     */
    Map<String, Object> getBackupStats();

    /**
     * 获取最近备份记录
     */
    BackupRecord getLatestBackup();

    /**
     * 获取指定时间范围内的备份记录
     */
    List<BackupRecord> getBackupsByTimeRange(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 按备份类型统计
     */
    List<Map<String, Object>> countByBackupType();

    /**
     * 按状态统计
     */
    List<Map<String, Object>> countByStatus();

    /**
     * 获取正在进行的备份
     */
    List<BackupRecord> getInProgressBackups();

    /**
     * 删除过期备份记录
     */
    int deleteExpiredBackups(@Param("expireTime") LocalDateTime expireTime);
}