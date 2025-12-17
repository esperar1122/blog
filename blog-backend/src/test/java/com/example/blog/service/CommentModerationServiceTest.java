package com.example.blog.service;

import com.example.blog.dto.CommentModerationRequest;
import com.example.blog.dto.CommentReportRequest;
import com.example.blog.entity.Comment;
import com.example.blog.entity.CommentBlacklist;
import com.example.blog.entity.CommentReport;
import com.example.blog.entity.User;
import com.example.blog.mapper.CommentBlacklistMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.CommentReportMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentModerationServiceTest {

    @Mock
    private CommentReportMapper commentReportMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private CommentBlacklistMapper commentBlacklistMapper;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentModerationServiceImpl commentModerationService;

    private Comment testComment;
    private User testUser;
    private CommentReport testReport;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setContent("测试评论内容");
        testComment.setUserId(1L);
        testComment.setArticleId(1L);
        testComment.setStatus(Comment.Status.NORMAL.getValue());
        testComment.setCreateTime(LocalDateTime.now());

        testReport = new CommentReport();
        testReport.setId(1L);
        testReport.setCommentId(1L);
        testReport.setReporterId(2L);
        testReport.setReason(CommentReport.ReportReason.SPAM.getValue());
        testReport.setStatus(CommentReport.ReportStatus.PENDING.getValue());
        testReport.setCreateTime(LocalDateTime.now());
    }

    @Test
    void testReportComment_Success() {
        // Given
        Long commentId = 1L;
        Long reporterId = 2L;
        CommentReportRequest request = new CommentReportRequest();
        request.setCommentId(commentId);
        request.setReason(CommentReport.ReportReason.SPAM.getValue());
        request.setDescription("垃圾评论");

        when(commentService.getById(commentId)).thenReturn(testComment);
        when(commentReportMapper.existsByCommentIdAndReporterId(commentId, reporterId)).thenReturn(false);
        when(commentReportMapper.insert(any(CommentReport.class))).thenReturn(1);

        // When
        CommentReport result = commentModerationService.reportComment(commentId, reporterId, request);

        // Then
        assertNotNull(result);
        assertEquals(commentId, result.getCommentId());
        assertEquals(reporterId, result.getReporterId());
        assertEquals(CommentReport.ReportStatus.PENDING.getValue(), result.getStatus());
        verify(commentReportMapper).insert(any(CommentReport.class));
    }

    @Test
    void testReportComment_CommentNotFound() {
        // Given
        Long commentId = 999L;
        Long reporterId = 2L;
        CommentReportRequest request = new CommentReportRequest();
        request.setCommentId(commentId);
        request.setReason(CommentReport.ReportReason.SPAM.getValue());

        when(commentService.getById(commentId)).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            commentModerationService.reportComment(commentId, reporterId, request);
        });
    }

    @Test
    void testReportComment_AlreadyReported() {
        // Given
        Long commentId = 1L;
        Long reporterId = 2L;
        CommentReportRequest request = new CommentReportRequest();
        request.setCommentId(commentId);
        request.setReason(CommentReport.ReportReason.SPAM.getValue());

        when(commentService.getById(commentId)).thenReturn(testComment);
        when(commentReportMapper.existsByCommentIdAndReporterId(commentId, reporterId)).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            commentModerationService.reportComment(commentId, reporterId, request);
        });
    }

    @Test
    void testReviewReport_Approve() {
        // Given
        Long reportId = 1L;
        Long reviewerId = 3L;
        CommentModerationRequest request = new CommentModerationRequest();
        request.setStatus("APPROVED");
        request.setReviewNote("举报成立");

        when(commentModerationService.getById(reportId)).thenReturn(testReport);
        when(commentService.getById(testReport.getCommentId())).thenReturn(testComment);
        when(commentService.updateById(any(Comment.class))).thenReturn(true);
        when(commentModerationService.updateById(any(CommentReport.class))).thenReturn(true);

        // When
        commentModerationService.reviewReport(reportId, reviewerId, request);

        // Then
        verify(commentService).updateById(argThat(comment ->
            comment.getStatus().equals(Comment.Status.DELETED.getValue())
        ));
        verify(commentModerationService).updateById(argThat(report ->
            report.getStatus().equals("APPROVED") &&
            report.getReviewerId().equals(reviewerId)
        ));
    }

    @Test
    void testReviewReport_Reject() {
        // Given
        Long reportId = 1L;
        Long reviewerId = 3L;
        CommentModerationRequest request = new CommentModerationRequest();
        request.setStatus("REJECTED");
        request.setReviewNote("举报不成立");

        when(commentModerationService.getById(reportId)).thenReturn(testReport);
        when(commentModerationService.updateById(any(CommentReport.class))).thenReturn(true);

        // When
        commentModerationService.reviewReport(reportId, reviewerId, request);

        // Then
        verify(commentModerationService).updateById(argThat(report ->
            report.getStatus().equals("REJECTED") &&
            report.getReviewerId().equals(reviewerId)
        ));
        // Comment should not be deleted
        verify(commentService, never()).updateById(any(Comment.class));
    }

    @Test
    void testAddToBlacklist_Success() {
        // Given
        Long userId = 1L;
        Long adminId = 3L;
        var request = new com.example.blog.dto.BlacklistRequest();
        request.setUserId(userId);
        request.setReason("恶意评论");
        request.setExpireTime(LocalDateTime.now().plusDays(30));

        when(commentBlacklistMapper.selectByUserId(userId)).thenReturn(null);
        when(commentBlacklistMapper.insert(any(CommentBlacklist.class))).thenReturn(1);

        // When
        CommentBlacklist result = commentModerationService.addToBlacklist(request, adminId);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(adminId, result.getBlacklistedBy());
        verify(commentBlacklistMapper).insert(any(CommentBlacklist.class));
    }

    @Test
    void testAddToBlacklist_AlreadyBlacklisted() {
        // Given
        Long userId = 1L;
        Long adminId = 3L;
        var request = new com.example.blog.dto.BlacklistRequest();
        request.setUserId(userId);
        request.setReason("恶意评论");

        CommentBlacklist existingBlacklist = new CommentBlacklist();
        existingBlacklist.setUserId(userId);
        existingBlacklist.setExpireTime(LocalDateTime.now().plusDays(1));

        when(commentBlacklistMapper.selectByUserId(userId)).thenReturn(existingBlacklist);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            commentModerationService.addToBlacklist(request, adminId);
        });
    }

    @Test
    void testRemoveFromBlacklist_Success() {
        // Given
        Long userId = 1L;
        when(commentBlacklistMapper.delete(any())).thenReturn(1);

        // When
        commentModerationService.removeFromBlacklist(userId);

        // Then
        verify(commentBlacklistMapper).delete(argThat(wrapper ->
            wrapper.getEntity().getUserId().equals(userId)
        ));
    }

    @Test
    void testIsUserBlacklisted_True() {
        // Given
        Long userId = 1L;
        when(commentBlacklistMapper.isUserBlacklisted(userId)).thenReturn(true);

        // When
        boolean result = commentModerationService.isUserBlacklisted(userId);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsUserBlacklisted_False() {
        // Given
        Long userId = 1L;
        when(commentBlacklistMapper.isUserBlacklisted(userId)).thenReturn(false);

        // When
        boolean result = commentModerationService.isUserBlacklisted(userId);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanEditComment_WithinTimeLimit() {
        // Given
        Long commentId = 1L;
        Long userId = 1L;
        testComment.setCreateTime(LocalDateTime.now().minusMinutes(15)); // 15分钟前

        when(commentService.getById(commentId)).thenReturn(testComment);

        // When
        boolean result = commentModerationService.canEditComment(commentId, userId);

        // Then
        assertTrue(result);
    }

    @Test
    void testCanEditComment_ExceedsTimeLimit() {
        // Given
        Long commentId = 1L;
        Long userId = 1L;
        testComment.setCreateTime(LocalDateTime.now().minusMinutes(45)); // 45分钟前

        when(commentService.getById(commentId)).thenReturn(testComment);

        // When
        boolean result = commentModerationService.canEditComment(commentId, userId);

        // Then
        assertFalse(result);
    }

    @Test
    void testCanEditComment_DifferentUser() {
        // Given
        Long commentId = 1L;
        Long userId = 2L; // 不同的用户
        testComment.setCreateTime(LocalDateTime.now().minusMinutes(15));

        when(commentService.getById(commentId)).thenReturn(testComment);

        // When
        boolean result = commentModerationService.canEditComment(commentId, userId);

        // Then
        assertFalse(result);
    }

    @Test
    void testGetCommentStatistics() {
        // Given
        when(commentService.count()).thenReturn(100L);
        when(commentService.count(argThat(wrapper ->
            wrapper.getEntity().getStatus().equals(Comment.Status.NORMAL.getValue())
        ))).thenReturn(80L);
        when(commentService.count(argThat(wrapper ->
            wrapper.getEntity().getStatus().equals(Comment.Status.DELETED.getValue())
        ))).thenReturn(20L);
        when(commentService.count(argThat(wrapper ->
            wrapper.getEntity().getIsEdited().equals(true)
        ))).thenReturn(10L);

        // When
        var result = commentModerationService.getCommentStatistics();

        // Then
        assertEquals(100L, result.get("totalComments"));
        assertEquals(80L, result.get("activeComments"));
        assertEquals(20L, result.get("deletedComments"));
        assertEquals(10L, result.get("editedComments"));
    }

    @Test
    void testBatchModerateComments_Delete() {
        // Given
        List<Long> commentIds = Arrays.asList(1L, 2L, 3L);
        String action = "DELETE";
        Long moderatorId = 3L;
        String reason = "批量删除违规评论";

        Comment comment1 = new Comment();
        comment1.setId(1L);
        comment1.setStatus(Comment.Status.NORMAL.getValue());

        Comment comment2 = new Comment();
        comment2.setId(2L);
        comment2.setStatus(Comment.Status.NORMAL.getValue());

        Comment comment3 = new Comment();
        comment3.setId(3L);
        comment3.setStatus(Comment.Status.NORMAL.getValue());

        when(commentService.getById(1L)).thenReturn(comment1);
        when(commentService.getById(2L)).thenReturn(comment2);
        when(commentService.getById(3L)).thenReturn(comment3);
        when(commentService.updateById(any(Comment.class))).thenReturn(true);

        // When
        commentModerationService.batchModerateComments(commentIds, action, moderatorId, reason);

        // Then
        verify(commentService, times(3)).updateById(argThat(comment ->
            comment.getStatus().equals(Comment.Status.DELETED.getValue())
        ));
    }

    @Test
    void testCleanupExpiredBlacklist() {
        // Given
        when(commentBlacklistMapper.deleteExpiredEntries()).thenReturn(5);

        // When
        int result = commentModerationService.cleanupExpiredBlacklist();

        // Then
        assertEquals(5, result);
        verify(commentBlacklistMapper).deleteExpiredEntries();
    }
}