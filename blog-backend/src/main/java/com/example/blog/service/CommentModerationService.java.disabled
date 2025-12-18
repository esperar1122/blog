package com.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.dto.BlacklistRequest;
import com.example.blog.dto.CommentModerationRequest;
import com.example.blog.dto.CommentReportRequest;
import com.example.blog.entity.Comment;
import com.example.blog.entity.CommentBlacklist;
import com.example.blog.entity.CommentReport;

import java.util.List;
import java.util.Map;

public interface CommentModerationService extends IService<CommentReport> {

    /**
     * Report a comment
     */
    CommentReport reportComment(Long commentId, Long reporterId, CommentReportRequest request);

    /**
     * Review and handle comment report
     */
    void reviewReport(Long reportId, Long reviewerId, CommentModerationRequest request);

    /**
     * Get reports with pagination and filters
     */
    IPage<CommentReport> getReports(Page<CommentReport> page, String status, String keyword);

    /**
     * Get reports by comment ID
     */
    List<CommentReport> getReportsByCommentId(Long commentId);

    /**
     * Get comment statistics
     */
    Map<String, Object> getCommentStatistics();

    /**
     * Get report statistics
     */
    Map<String, Object> getReportStatistics();

    /**
     * Add user to blacklist
     */
    CommentBlacklist addToBlacklist(BlacklistRequest request, Long adminId);

    /**
     * Remove user from blacklist
     */
    void removeFromBlacklist(Long userId);

    /**
     * Get all blacklisted users
     */
    List<CommentBlacklist> getBlacklistedUsers();

    /**
     * Check if user is blacklisted
     */
    boolean isUserBlacklisted(Long userId);

    /**
     * Moderate comment (approve/delete)
     */
    void moderateComment(Long commentId, String action, Long moderatorId, String reason);

    /**
     * Batch moderate comments
     */
    void batchModerateComments(List<Long> commentIds, String action, Long moderatorId, String reason);

    /**
     * Check if user can edit comment (within 30 minutes of creation)
     */
    boolean canEditComment(Long commentId, Long userId);

    /**
     * Clean up expired blacklist entries
     */
    int cleanupExpiredBlacklist();
}