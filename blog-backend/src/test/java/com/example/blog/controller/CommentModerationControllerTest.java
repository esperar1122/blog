package com.example.blog.controller;

import com.example.blog.dto.*;
import com.example.blog.entity.*;
import com.example.blog.service.CommentModerationService;
import com.example.blog.service.CommentService;
import com.example.blog.service.SensitiveWordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentModerationController.class)
class CommentModerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentModerationService commentModerationService;

    @MockBean
    private SensitiveWordService sensitiveWordService;

    @MockBean
    private CommentService commentService;

    private CommentReport testReport;
    private CommentBlacklist testBlacklist;
    private SensitiveWord testSensitiveWord;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");

        testReport = new CommentReport();
        testReport.setId(1L);
        testReport.setCommentId(1L);
        testReport.setReporterId(2L);
        testReport.setReason(CommentReport.ReportReason.SPAM.getValue());
        testReport.setStatus(CommentReport.ReportStatus.PENDING.getValue());
        testReport.setCreateTime(LocalDateTime.now());
        testReport.setReporter(testUser);

        testBlacklist = new CommentBlacklist();
        testBlacklist.setId(1L);
        testBlacklist.setUserId(1L);
        testBlacklist.setReason("恶意评论");
        testBlacklist.setBlacklistedBy(3L);
        testBlacklist.setCreateTime(LocalDateTime.now());
        testBlacklist.setUser(testUser);

        testSensitiveWord = new SensitiveWord();
        testSensitiveWord.setId(1L);
        testSensitiveWord.setWord("测试");
        testSensitiveWord.setReplacement("***");
        testSensitiveWord.setPattern("测试");
        testSensitiveWord.setType(SensitiveWord.WordType.FILTER.getValue());
        testSensitiveWord.setCreateTime(LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "2")
    void testReportComment_Success() throws Exception {
        // Given
        CommentReportRequest request = new CommentReportRequest();
        request.setCommentId(1L);
        request.setReason(CommentReport.ReportReason.SPAM.getValue());
        request.setDescription("垃圾评论");

        when(commentModerationService.reportComment(eq(1L), eq(2L), any(CommentReportRequest.class)))
                .thenReturn(testReport);

        // When & Then
        mockMvc.perform(post("/api/v1/comments/1/report")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.reason").value("SPAM"));

        verify(commentModerationService).reportComment(eq(1L), eq(2L), any(CommentReportRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetReports_Success() throws Exception {
        // Given
        List<CommentReport> reports = Arrays.asList(testReport);
        var mockPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<CommentReport>(1, 10);
        mockPage.setRecords(reports);
        mockPage.setTotal(1);

        when(commentModerationService.getReports(any(), eq("PENDING"), isNull()))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/v1/comments/reports")
                        .param("status", "PENDING")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.list[0].id").value(1))
                .andExpect(jsonPath("$.data.total").value(1));

        verify(commentModerationService).getReports(any(), eq("PENDING"), isNull());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testReviewReport_Success() throws Exception {
        // Given
        CommentModerationRequest request = new CommentModerationRequest();
        request.setStatus("APPROVED");
        request.setReviewNote("举报成立");

        doNothing().when(commentModerationService).reviewReport(eq(1L), eq(1L), any(CommentModerationRequest.class));

        // When & Then
        mockMvc.perform(put("/api/v1/comments/reports/1/review")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(commentModerationService).reviewReport(eq(1L), eq(1L), any(CommentModerationRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testModerateComment_Success() throws Exception {
        // Given
        CommentModerationRequest request = new CommentModerationRequest();
        request.setStatus("DELETE");
        request.setReviewNote("违规评论");

        doNothing().when(commentModerationService).moderateComment(eq(1L), eq("DELETE"), eq(1L), eq("违规评论"));

        // When & Then
        mockMvc.perform(put("/api/v1/comments/1/moderate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(commentModerationService).moderateComment(eq(1L), eq("DELETE"), eq(1L), eq("违规评论"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testBatchModerateComments_Success() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("commentIds", Arrays.asList(1L, 2L, 3L));
        request.put("action", "DELETE");
        request.put("reason", "批量删除违规评论");

        doNothing().when(commentModerationService).batchModerateComments(anyList(), eq("DELETE"), eq(1L), eq("批量删除违规评论"));

        // When & Then
        mockMvc.perform(post("/api/v1/comments/batch-moderate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(commentModerationService).batchModerateComments(anyList(), eq("DELETE"), eq(1L), eq("批量删除违规评论"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddToBlacklist_Success() throws Exception {
        // Given
        BlacklistRequest request = new BlacklistRequest();
        request.setUserId(1L);
        request.setReason("恶意评论");

        when(commentModerationService.addToBlacklist(any(BlacklistRequest.class), eq(1L)))
                .thenReturn(testBlacklist);

        // When & Then
        mockMvc.perform(post("/api/v1/users/1/blacklist")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.userId").value(1));

        verify(commentModerationService).addToBlacklist(any(BlacklistRequest.class), eq(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRemoveFromBlacklist_Success() throws Exception {
        // Given
        doNothing().when(commentModerationService).removeFromBlacklist(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/users/1/blacklist")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(commentModerationService).removeFromBlacklist(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBlacklistedUsers_Success() throws Exception {
        // Given
        List<CommentBlacklist> blacklistUsers = Arrays.asList(testBlacklist);
        when(commentModerationService.getBlacklistedUsers()).thenReturn(blacklistUsers);

        // When & Then
        mockMvc.perform(get("/api/v1/blacklist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].userId").value(1));

        verify(commentModerationService).getBlacklistedUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetSensitiveWords_Success() throws Exception {
        // Given
        List<SensitiveWord> words = Arrays.asList(testSensitiveWord);
        when(sensitiveWordService.getWordsByType(isNull())).thenReturn(words);

        // When & Then
        mockMvc.perform(get("/api/v1/sensitive-words"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].word").value("测试"));

        verify(sensitiveWordService).getWordsByType(isNull());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddSensitiveWord_Success() throws Exception {
        // Given
        SensitiveWordRequest request = new SensitiveWordRequest();
        request.setWord("新词");
        request.setReplacement("***");
        request.setPattern("新词");
        request.setType(SensitiveWord.WordType.FILTER.getValue());

        when(sensitiveWordService.addSensitiveWord(any(SensitiveWordRequest.class)))
                .thenReturn(testSensitiveWord);

        // When & Then
        mockMvc.perform(post("/api/v1/sensitive-words")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.word").value("测试"));

        verify(sensitiveWordService).addSensitiveWord(any(SensitiveWordRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateSensitiveWord_Success() throws Exception {
        // Given
        SensitiveWordRequest request = new SensitiveWordRequest();
        request.setWord("更新词");
        request.setReplacement("###");
        request.setPattern("更新词");
        request.setType(SensitiveWord.WordType.BLOCK.getValue());

        when(sensitiveWordService.updateSensitiveWord(eq(1L), any(SensitiveWordRequest.class)))
                .thenReturn(testSensitiveWord);

        // When & Then
        mockMvc.perform(put("/api/v1/sensitive-words/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(sensitiveWordService).updateSensitiveWord(eq(1L), any(SensitiveWordRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteSensitiveWord_Success() throws Exception {
        // Given
        doNothing().when(sensitiveWordService).removeById(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/sensitive-words/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(sensitiveWordService).removeById(1L);
    }

    @Test
    void testFilterSensitiveWords_Success() throws Exception {
        // Given
        Map<String, String> request = Map.of("text", "这是一个测试评论");
        Map<String, Object> expectedResult = Map.of(
                "filteredText", "这是一个***评论",
                "containsBlocked", false,
                "warningWords", Arrays.asList()
        );

        when(sensitiveWordService.filterSensitiveWords("这是一个测试评论"))
                .thenReturn(expectedResult);

        // When & Then
        mockMvc.perform(post("/api/v1/comments/filter")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.filteredText").value("这是一个***评论"))
                .andExpect(jsonPath("$.data.containsBlocked").value(false));

        verify(sensitiveWordService).filterSensitiveWords("这是一个测试评论");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetCommentStatistics_Success() throws Exception {
        // Given
        Map<String, Object> statistics = Map.of(
                "totalComments", 100L,
                "activeComments", 80L,
                "deletedComments", 20L,
                "editedComments", 10L
        );

        when(commentModerationService.getCommentStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/v1/comments/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalComments").value(100))
                .andExpect(jsonPath("$.data.activeComments").value(80))
                .andExpect(jsonPath("$.data.deletedComments").value(20))
                .andExpect(jsonPath("$.data.editedComments").value(10));

        verify(commentModerationService).getCommentStatistics();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetReportStatistics_Success() throws Exception {
        // Given
        Map<String, Object> statistics = Map.of(
                "pendingReports", 5L,
                "approvedReports", 10L,
                "rejectedReports", 3L,
                "totalReports", 18L
        );

        when(commentModerationService.getReportStatistics()).thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/v1/comments/reports/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pendingReports").value(5))
                .andExpect(jsonPath("$.data.approvedReports").value(10))
                .andExpect(jsonPath("$.data.rejectedReports").value(3))
                .andExpect(jsonPath("$.data.totalReports").value(18));

        verify(commentModerationService).getReportStatistics();
    }

    @Test
    void testReportComment_Unauthorized() throws Exception {
        // Given
        CommentReportRequest request = new CommentReportRequest();
        request.setCommentId(1L);
        request.setReason(CommentReport.ReportReason.SPAM.getValue());

        // When & Then
        mockMvc.perform(post("/api/v1/comments/1/report")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(commentModerationService, never()).reportComment(anyLong(), anyLong(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetReports_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/comments/reports"))
                .andExpect(status().isForbidden());

        verify(commentModerationService, never()).getReports(any(), any(), any());
    }
}