package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.CommentReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CommentReportMapper extends BaseMapper<CommentReport> {

    /**
     * Get reports by status with pagination
     */
    IPage<CommentReport> selectReportsByStatus(Page<CommentReport> page, @Param("status") String status);

    /**
     * Get reports by comment ID
     */
    List<CommentReport> selectReportsByCommentId(@Param("commentId") Long commentId);

    /**
     * Get reports by reporter ID
     */
    List<CommentReport> selectReportsByReporterId(@Param("reporterId") Long reporterId);

    /**
     * Get reports created in date range
     */
    List<CommentReport> selectReportsByDateRange(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * Count reports by status
     */
    Long countReportsByStatus(@Param("status") String status);

    /**
     * Check if user has already reported a comment
     */
    Boolean existsByCommentIdAndReporterId(@Param("commentId") Long commentId,
                                          @Param("reporterId") Long reporterId);
}