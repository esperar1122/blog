package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.dto.BlacklistRequest;
import com.example.blog.dto.CommentModerationRequest;
import com.example.blog.dto.CommentReportRequest;
import com.example.blog.entity.Comment;
import com.example.blog.entity.CommentBlacklist;
import com.example.blog.entity.CommentReport;
import com.example.blog.mapper.CommentBlacklistMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.CommentReportMapper;
import com.example.blog.service.CommentModerationService;
import com.example.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentModerationServiceImpl extends ServiceImpl<CommentReportMapper, CommentReport>
        implements CommentModerationService {

    private final CommentReportMapper commentReportMapper;
    private final CommentMapper commentMapper;
    private final CommentBlacklistMapper commentBlacklistMapper;
    private final CommentService commentService;

    @Override
    @Transactional
    public CommentReport reportComment(Long commentId, Long reporterId, CommentReportRequest request) {
        Comment comment = commentService.getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        Boolean exists = commentReportMapper.existsByCommentIdAndReporterId(commentId, reporterId);
        if (exists != null && exists) {
            throw new RuntimeException("您已经举报过此评论");
        }

        CommentReport report = new CommentReport();
        report.setCommentId(commentId);
        report.setReporterId(reporterId);
        report.setReason(request.getReason());
        report.setDescription(request.getDescription());
        report.setStatus(CommentReport.ReportStatus.PENDING.getValue());

        save(report);
        log.info("User {} reported comment {} for reason: {}", reporterId, commentId, request.getReason());

        return report;
    }

    @Override
    @Transactional
    public void reviewReport(Long reportId, Long reviewerId, CommentModerationRequest request) {
        CommentReport report = getById(reportId);
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }

        report.setStatus(request.getStatus());
        report.setReviewerId(reviewerId);
        report.setReviewTime(LocalDateTime.now());

        updateById(report);

        if ("APPROVED".equals(request.getStatus())) {
            Comment comment = commentService.getById(report.getCommentId());
            if (comment != null) {
                comment.setStatus(Comment.Status.DELETED.getValue());
                commentService.updateById(comment);
                log.info("Comment {} deleted due to approved report", report.getCommentId());
            }
        }

        log.info("Report {} reviewed by admin {} with status: {}", reportId, reviewerId, request.getStatus());
    }

    @Override
    public IPage<CommentReport> getReports(Page<CommentReport> page, String status, String keyword) {
        if (status != null && !status.isEmpty()) {
            return commentReportMapper.selectReportsByStatus(page, status);
        }

        LambdaQueryWrapper<CommentReport> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(CommentReport::getDescription, keyword);
        }
        wrapper.orderByDesc(CommentReport::getCreateTime);

        return page(wrapper);
    }

    @Override
    public List<CommentReport> getReportsByCommentId(Long commentId) {
        return commentReportMapper.selectReportsByCommentId(commentId);
    }

    @Override
    public Map<String, Object> getCommentStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();

        long totalComments = commentService.count();
        statistics.put("totalComments", totalComments);

        wrapper.eq(Comment::getStatus, Comment.Status.NORMAL.getValue());
        long activeComments = commentService.count(wrapper);
        statistics.put("activeComments", activeComments);

        wrapper.clear();
        wrapper.eq(Comment::getStatus, Comment.Status.DELETED.getValue());
        long deletedComments = commentService.count(wrapper);
        statistics.put("deletedComments", deletedComments);

        wrapper.clear();
        wrapper.eq(Comment::getIsEdited, true);
        long editedComments = commentService.count(wrapper);
        statistics.put("editedComments", editedComments);

        return statistics;
    }

    @Override
    public Map<String, Object> getReportStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        Long pendingReports = commentReportMapper.countReportsByStatus(CommentReport.ReportStatus.PENDING.getValue());
        statistics.put("pendingReports", pendingReports != null ? pendingReports : 0);

        Long approvedReports = commentReportMapper.countReportsByStatus(CommentReport.ReportStatus.APPROVED.getValue());
        statistics.put("approvedReports", approvedReports != null ? approvedReports : 0);

        Long rejectedReports = commentReportMapper.countReportsByStatus(CommentReport.ReportStatus.REJECTED.getValue());
        statistics.put("rejectedReports", rejectedReports != null ? rejectedReports : 0);

        statistics.put("totalReports", count());

        return statistics;
    }

    @Override
    @Transactional
    public CommentBlacklist addToBlacklist(BlacklistRequest request, Long adminId) {
        CommentBlacklist existBlacklist = commentBlacklistMapper.selectByUserId(request.getUserId());
        if (existBlacklist != null && existBlacklist.isActive()) {
            throw new RuntimeException("用户已在黑名单中");
        }

        CommentBlacklist blacklist = new CommentBlacklist();
        blacklist.setUserId(request.getUserId());
        blacklist.setReason(request.getReason());
        blacklist.setBlacklistedBy(adminId);
        blacklist.setExpireTime(request.getExpireTime());

        commentBlacklistMapper.insert(blacklist);
        log.info("User {} added to blacklist by admin {}", request.getUserId(), adminId);

        return blacklist;
    }

    @Override
    @Transactional
    public void removeFromBlacklist(Long userId) {
        LambdaQueryWrapper<CommentBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommentBlacklist::getUserId, userId);
        commentBlacklistMapper.delete(wrapper);
        log.info("User {} removed from blacklist", userId);
    }

    @Override
    public List<CommentBlacklist> getBlacklistedUsers() {
        return commentBlacklistMapper.selectActiveBlacklist();
    }

    @Override
    public boolean isUserBlacklisted(Long userId) {
        Boolean isBlacklisted = commentBlacklistMapper.isUserBlacklisted(userId);
        return isBlacklisted != null && isBlacklisted;
    }

    @Override
    @Transactional
    public void moderateComment(Long commentId, String action, Long moderatorId, String reason) {
        Comment comment = commentService.getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        if ("DELETE".equalsIgnoreCase(action)) {
            comment.setStatus(Comment.Status.DELETED.getValue());
            commentService.updateById(comment);
            log.info("Comment {} deleted by moderator {}. Reason: {}", commentId, moderatorId, reason);
        } else if ("APPROVE".equalsIgnoreCase(action)) {
            comment.setStatus(Comment.Status.NORMAL.getValue());
            commentService.updateById(comment);
            log.info("Comment {} approved by moderator {}", commentId, moderatorId);
        }
    }

    @Override
    @Transactional
    public void batchModerateComments(List<Long> commentIds, String action, Long moderatorId, String reason) {
        for (Long commentId : commentIds) {
            try {
                moderateComment(commentId, action, moderatorId, reason);
            } catch (Exception e) {
                log.error("Failed to moderate comment {}: {}", commentId, e.getMessage());
            }
        }
    }

    @Override
    public boolean canEditComment(Long commentId, Long userId) {
        Comment comment = commentService.getById(commentId);
        if (comment == null) {
            return false;
        }

        if (!comment.getUserId().equals(userId)) {
            return false;
        }

        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
        return comment.getCreateTime().isAfter(thirtyMinutesAgo);
    }

    @Override
    @Transactional
    public int cleanupExpiredBlacklist() {
        return commentBlacklistMapper.deleteExpiredEntries();
    }
}