package com.example.blog.integration;

import com.example.blog.BlogApplication;
import com.example.blog.dto.request.ArticleReviewRequest;
import com.example.blog.dto.request.BatchOperationRequest;
import com.example.blog.dto.request.ContentQueryRequest;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.entity.SensitiveWord;
import com.example.blog.entity.User;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.repository.AdminLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BlogApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class AdminContentIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AdminLogRepository adminLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    private User testUser;
    private Article testArticle;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        // Clean up database
        adminLogRepository.deleteAll();
        commentMapper.delete(null);
        articleMapper.delete(null);
        userMapper.delete(null);

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole("ADMIN");
        testUser.setStatus("ACTIVE");
        testUser.setCreateTime(LocalDateTime.now());
        userMapper.insert(testUser);

        // Create test article
        testArticle = new Article();
        testArticle.setTitle("测试文章标题");
        testArticle.setContent("这是测试文章的内容，包含一些测试文字");
        testArticle.setSummary("测试摘要");
        testArticle.setAuthorId(testUser.getId());
        testArticle.setStatus("DRAFT");
        testArticle.setViewCount(0);
        testArticle.setLikeCount(0);
        testArticle.setCommentCount(0);
        testArticle.setIsTop(false);
        testArticle.setCreateTime(LocalDateTime.now());
        articleMapper.insert(testArticle);

        // Create test comment
        testComment = new Comment();
        testComment.setContent("这是一条测试评论");
        testComment.setArticleId(testArticle.getId());
        testComment.setUserId(testUser.getId());
        testComment.setStatus("NORMAL");
        testComment.setLevel(1);
        testComment.setLikeCount(0);
        testComment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(testComment);
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testGetArticles_Success() throws Exception {
        mockMvc.perform(get("/admin/content/articles")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].title").value("测试文章标题"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testGetComments_Success() throws Exception {
        mockMvc.perform(get("/admin/content/comments")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].content").value("这是一条测试评论"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testReviewArticle_Success() throws Exception {
        ArticleReviewRequest request = new ArticleReviewRequest();
        request.setId(testArticle.getId());
        request.setTitle("审核后的文章标题");
        request.setContent("审核后的文章内容");
        request.setStatus("PUBLISHED");
        request.setSummary("审核后的摘要");

        mockMvc.perform(put("/admin/content/articles/review")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("审核后的文章标题"));

        // Verify article was updated in database
        Article updatedArticle = articleMapper.selectById(testArticle.getId());
        assertEquals("审核后的文章标题", updatedArticle.getTitle());
        assertEquals("PUBLISHED", updatedArticle.getStatus());
        assertNotNull(updatedArticle.getPublishTime());

        // Verify audit log was created
        assertEquals(1, adminLogRepository.count());
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testBatchOperateArticles_Delete() throws Exception {
        BatchOperationRequest request = new BatchOperationRequest();
        request.setOperationType("delete");
        request.setIds(java.util.Arrays.asList(testArticle.getId()));

        mockMvc.perform(post("/admin/content/batch-articles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify article was marked as deleted
        Article updatedArticle = articleMapper.selectById(testArticle.getId());
        assertEquals("DELETED", updatedArticle.getStatus());

        // Verify audit log was created
        assertEquals(1, adminLogRepository.count());
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testBatchOperateArticles_Publish() throws Exception {
        BatchOperationRequest request = new BatchOperationRequest();
        request.setOperationType("publish");
        request.setIds(java.util.Arrays.asList(testArticle.getId()));

        mockMvc.perform(post("/admin/content/batch-articles")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify article was published
        Article updatedArticle = articleMapper.selectById(testArticle.getId());
        assertEquals("PUBLISHED", updatedArticle.getStatus());
        assertNotNull(updatedArticle.getPublishTime());
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testBatchOperateComments_Delete() throws Exception {
        BatchOperationRequest request = new BatchOperationRequest();
        request.setOperationType("delete");
        request.setIds(java.util.Arrays.asList(testComment.getId()));

        mockMvc.perform(post("/admin/content/batch-comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // Verify comment was marked as deleted
        Comment updatedComment = commentMapper.selectById(testComment.getId());
        assertEquals("DELETED", updatedComment.getStatus());
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testExportArticles() throws Exception {
        mockMvc.perform(get("/admin/content/export/articles")
                .param("format", "json")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.fileName").exists())
                .andExpect(jsonPath("$.data.format").value("json"))
                .andExpect(jsonPath("$.data.recordCount").value(1));
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testGetContentStats() throws Exception {
        mockMvc.perform(get("/admin/content/stats")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalArticles").value(1))
                .andExpect(jsonPath("$.data.totalComments").value(1))
                .andExpect(jsonPath("$.data.draftArticles").value(1));
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testCheckContent() throws Exception {
        mockMvc.perform(post("/admin/content/check-content")
                .param("content", "正常测试内容")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testFilterContent() throws Exception {
        mockMvc.perform(post("/admin/content/filter-content")
                .param("content", "正常测试内容")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpected(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testAddSensitiveWord() throws Exception {
        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setWord("测试敏感词");
        sensitiveWord.setType("WORD");
        sensitiveWord.setLevel("MEDIUM");
        sensitiveWord.setStatus("ACTIVE");

        mockMvc.perform(post("/admin/content/sensitive-words")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sensitiveWord)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testGetSensitiveWords() throws Exception {
        mockMvc.perform(get("/admin/content/sensitive-words")
                .param("page", "1")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testGetSystemSettings() throws Exception {
        mockMvc.perform(get("/admin/content/system-settings")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testUpdateSystemSetting() throws Exception {
        var setting = new com.example.blog.entity.SystemSettings();
        setting.setSettingKey("test.setting");
        setting.setSettingValue("test.value");
        setting.setType("STRING");
        setting.setIsPublic(false);

        mockMvc.perform(put("/admin/content/system-settings")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(setting)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testBatchUpdateSystemSettings() throws Exception {
        java.util.Map<String, String> settings = new java.util.HashMap<>();
        settings.put("test.setting1", "value1");
        settings.put("test.setting2", "value2");

        mockMvc.perform(put("/admin/content/system-settings/batch")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(settings)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
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

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testSearchArticlesByKeyword() throws Exception {
        mockMvc.perform(get("/admin/content/articles")
                .param("keyword", "测试")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testFilterArticlesByStatus() throws Exception {
        mockMvc.perform(get("/admin/content/articles")
                .param("status", "DRAFT")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].status").value("DRAFT"));
    }

    @Test
    @WithMockUser(username = "1:testuser", roles = "ADMIN")
    void testFilterCommentsByStatus() throws Exception {
        mockMvc.perform(get("/admin/content/comments")
                .param("status", "NORMAL")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].status").value("NORMAL"));
    }
}