package com.example.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.Result;
import com.example.blog.dto.request.ArticleReviewRequest;
import com.example.blog.dto.request.BatchOperationRequest;
import com.example.blog.dto.response.ContentStatsResponse;
import com.example.blog.dto.response.ExportResponse;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.entity.SensitiveWord;
import com.example.blog.entity.SystemSettings;
import com.example.blog.service.AdminContentService;
import com.example.blog.service.SensitiveWordService;
import com.example.blog.service.SystemSettingsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
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

@WebMvcTest(AdminContentController.class)
@ActiveProfiles("test")
public class AdminContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminContentService adminContentService;

    @MockBean
    private SensitiveWordService sensitiveWordService;

    @MockBean
    private SystemSettingsService systemSettingsService;

    @Autowired
    private ObjectMapper objectMapper;

    private Article mockArticle;
    private Comment mockComment;
    private Page<Article> mockArticlePage;
    private Page<Comment> mockCommentPage;

    @BeforeEach
    void setUp() {
        mockArticle = new Article();
        mockArticle.setId(1L);
        mockArticle.setTitle("测试文章");
        mockArticle.setContent("测试内容");
        mockArticle.setStatus("PUBLISHED");
        mockArticle.setAuthorName("测试作者");
        mockArticle.setCreateTime(LocalDateTime.now());

        mockComment = new Comment();
        mockComment.setId(1L);
        mockComment.setContent("测试评论");
        mockComment.setArticleId(1L);
        mockComment.setUserId(1L);
        mockComment.setUserName("测试用户");
        mockComment.setStatus("NORMAL");
        mockComment.setCreateTime(LocalDateTime.now());

        mockArticlePage = new Page<>(1, 20);
        mockArticlePage.setRecords(Arrays.asList(mockArticle));
        mockArticlePage.setTotal(1);

        mockCommentPage = new Page<>(1, 20);
        mockCommentPage.setRecords(Arrays.asList(mockComment));
        mockCommentPage.setTotal(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetArticles() throws Exception {
        when(adminContentService.getArticleList(any())).thenReturn(mockArticlePage);

        mockMvc.perform(get("/admin/content/articles")
                .param("page", "1")
                .param("size", "20")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].title").value("测试文章"))
                .andExpect(jsonPath("$.data.total").value(1));

        verify(adminContentService, times(1)).getArticleList(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetComments() throws Exception {
        when(adminContentService.getCommentList(any())).thenReturn(mockCommentPage);

        mockMvc.perform(get("/admin/content/comments")
                .param("page", "1")
                .param("size", "20")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].content").value("测试评论"))
                .andExpect(jsonPath("$.data.total").value(1));

        verify(adminContentService, times(1)).getCommentList(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testReviewArticle() throws Exception {
        ArticleReviewRequest request = new ArticleReviewRequest();
        request.setId(1L);
        request.setTitle("审核后的文章");
        request.setContent("审核后的内容");
        request.setStatus("PUBLISHED");

        when(adminContentService.reviewArticle(any(), any())).thenReturn(mockArticle);

        mockMvc.perform(put("/admin/content/articles/review")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpected(jsonPath("$.data.title").value("测试文章"));

        verify(adminContentService, times(1)).reviewArticle(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testBatchOperateArticles() throws Exception {
        BatchOperationRequest request = new BatchOperationRequest();
        request.setOperationType("delete");
        request.setIds(Arrays.asList(1L, 2L));

        doNothing().when(adminContentService).batchOperateArticles(any(), any());

        mockMvc.perform(post("/admin/content/batch-articles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminContentService, times(1)).batchOperateArticles(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testBatchOperateComments() throws Exception {
        BatchOperationRequest request = new BatchOperationRequest();
        request.setOperationType("delete");
        request.setIds(Arrays.asList(1L, 2L));

        doNothing().when(adminContentService).batchOperateComments(any(), any());

        mockMvc.perform(post("/admin/content/batch-comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(adminContentService, times(1)).batchOperateComments(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testExportArticles() throws Exception {
        ExportResponse exportResponse = ExportResponse.builder()
                .fileName("articles_export.json")
                .downloadUrl("/admin/download/articles_export.json")
                .fileSize(1024L)
                .format("json")
                .recordCount(1)
                .exportTime("2023-12-16 16:00:00")
                .build();

        when(adminContentService.exportArticles(any(), eq("json"))).thenReturn(Result.success(exportResponse));

        mockMvc.perform(get("/admin/content/export/articles")
                .param("format", "json")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.fileName").value("articles_export.json"))
                .andExpect(jsonPath("$.data.format").value("json"));

        verify(adminContentService, times(1)).exportArticles(any(), eq("json"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetContentStats() throws Exception {
        ContentStatsResponse stats = ContentStatsResponse.builder()
                .totalArticles(100L)
                .publishedArticles(80L)
                .draftArticles(20L)
                .totalComments(200L)
                .activeComments(190L)
                .deletedComments(10L)
                .todayArticles(5L)
                .todayComments(15L)
                .build();

        when(adminContentService.getContentStats()).thenReturn(stats);

        mockMvc.perform(get("/admin/content/stats")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalArticles").value(100))
                .andExpect(jsonPath("$.data.publishedArticles").value(80))
                .andExpect(jsonPath("$.data.totalComments").value(200));

        verify(adminContentService, times(1)).getContentStats();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCheckContent() throws Exception {
        when(adminContentService.checkContent(anyString())).thenReturn(true);

        mockMvc.perform(post("/admin/content/check-content")
                .param("content", "包含敏感词的内容")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        verify(adminContentService, times(1)).checkContent(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testFilterContent() throws Exception {
        when(adminContentService.filterContent(anyString())).thenReturn("过滤后的内容");

        mockMvc.perform(post("/admin/content/filter-content")
                .param("content", "包含敏感词的内容")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("过滤后的内容"));

        verify(adminContentService, times(1)).filterContent(anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetSensitiveWords() throws Exception {
        SensitiveWord word = new SensitiveWord();
        word.setId(1L);
        word.setWord("敏感词");
        word.setType("WORD");
        word.setLevel("MEDIUM");
        word.setStatus("ACTIVE");

        Page<SensitiveWord> wordPage = new Page<>(1, 20);
        wordPage.setRecords(Arrays.asList(word));
        wordPage.setTotal(1);

        when(sensitiveWordService.getWordList(anyInt(), anyInt(), any(), any())).thenReturn(wordPage);

        mockMvc.perform(get("/admin/content/sensitive-words")
                .param("page", "1")
                .param("size", "20")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].word").value("敏感词"));

        verify(sensitiveWordService, times(1)).getWordList(anyInt(), anyInt(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddSensitiveWord() throws Exception {
        SensitiveWord word = new SensitiveWord();
        word.setWord("新敏感词");
        word.setType("WORD");
        word.setLevel("HIGH");
        word.setStatus("ACTIVE");

        when(sensitiveWordService.addWord(any())).thenReturn(word);

        mockMvc.perform(post("/admin/content/sensitive-words")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(word)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(sensitiveWordService, times(1)).addWord(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetSystemSettings() throws Exception {
        SystemSettings setting = new SystemSettings();
        setting.setId(1L);
        setting.setSettingKey("site.title");
        setting.setSettingValue("博客系统");
        setting.setType("STRING");
        setting.setIsPublic(true);

        List<SystemSettings> settings = Arrays.asList(setting);

        when(systemSettingsService.getAllSettings()).thenReturn(settings);

        mockMvc.perform(get("/admin/content/system-settings")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].settingKey").value("site.title"));

        verify(systemSettingsService, times(1)).getAllSettings();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateSystemSetting() throws Exception {
        SystemSettings setting = new SystemSettings();
        setting.setSettingKey("site.title");
        setting.setSettingValue("新标题");
        setting.setType("STRING");
        setting.setIsPublic(true);

        doNothing().when(systemSettingsService).updateSetting(any());

        mockMvc.perform(put("/admin/content/system-settings")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(setting)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(systemSettingsService, times(1)).updateSetting(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testBatchUpdateSystemSettings() throws Exception {
        Map<String, String> settings = new HashMap<>();
        settings.put("site.title", "批量更新标题");
        settings.put("site.description", "批量更新描述");

        doNothing().when(systemSettingsService).batchUpdateSettings(any());

        mockMvc.perform(put("/admin/content/system-settings/batch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(settings)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(systemSettingsService, times(1)).batchUpdateSettings(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDownloadFile() throws Exception {
        mockMvc.perform(get("/admin/content/export/test.json")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=test.json")))
                .andExpect(header().string("Content-Type", "application/octet-stream"));
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/admin/content/articles")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testNonAdminAccess() throws Exception {
        mockMvc.perform(get("/admin/content/articles")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}