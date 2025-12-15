package com.example.blog.controller;

import com.example.blog.common.Result;
import com.example.blog.dto.CreateArticleRequest;
import com.example.blog.dto.UpdateArticleRequest;
import com.example.blog.entity.Article;
import com.example.blog.service.ArticleService;
import com.example.blog.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Article testArticle;
    private CreateArticleRequest createRequest;
    private UpdateArticleRequest updateRequest;
    private String validToken;

    @BeforeEach
    void setUp() {
        // 创建测试文章
        testArticle = new Article();
        testArticle.setId(1L);
        testArticle.setTitle("测试文章");
        testArticle.setContent("这是测试内容");
        testArticle.setSummary("测试摘要");
        testArticle.setStatus(Article.Status.DRAFT.getValue());
        testArticle.setAuthorId(1L);
        testArticle.setCategoryId(1L);

        // 创建请求对象
        createRequest = new CreateArticleRequest();
        createRequest.setTitle("新文章");
        createRequest.setContent("文章内容");
        createRequest.setSummary("文章摘要");
        createRequest.setCategoryId(1L);
        createRequest.setTagIds(Arrays.asList(1L, 2L));

        updateRequest = new UpdateArticleRequest();
        updateRequest.setTitle("更新文章");
        updateRequest.setContent("更新内容");
        updateRequest.setSummary("更新摘要");
        updateRequest.setCategoryId(1L);
        updateRequest.setTagIds(Arrays.asList(1L));

        validToken = "Bearer valid.jwt.token";
    }

    @Test
    void getArticles_ShouldReturnArticleList() throws Exception {
        // Given
        List<Article> articles = Arrays.asList(testArticle);
        when(articleService.getArticlesWithPagination(any(), any(), any(), any(), any()))
                .thenReturn(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10));

        // When & Then
        mockMvc.perform(get("/articles")
                        .param("page", "1")
                        .param("size", "10")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpected(content().contentType(MediaType.APPLICATION_JSON));

        verify(articleService).getArticlesWithPagination(any(), any(), any(), any(), any());
    }

    @Test
    void getArticle_WithValidId_ShouldReturnArticle() throws Exception {
        // Given
        when(articleService.getArticleWithDetails(1L)).thenReturn(testArticle);
        when(articleService.incrementViewCount(1L)).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);

        // When & Then
        mockMvc.perform(get("/articles/1")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.data.id").value(1));

        verify(articleService).getArticleWithDetails(1L);
        verify(articleService).incrementViewCount(1L);
    }

    @Test
    void createArticle_WithValidData_ShouldCreateArticle() throws Exception {
        // Given
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(articleService.createArticle(any(Article.class), any())).thenReturn(testArticle);

        // When & Then
        mockMvc.perform(post("/articles")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.message").value("文章创建成功"));

        verify(articleService).createArticle(any(Article.class), any());
    }

    @Test
    void updateArticle_WithValidData_ShouldUpdateArticle() throws Exception {
        // Given
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(articleService.updateArticle(eq(1L), any(Article.class), any(), eq(1L)))
                .thenReturn(testArticle);

        // When & Then
        mockMvc.perform(put("/articles/1")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.message").value("文章更新成功"));

        verify(articleService).updateArticle(eq(1L), any(Article.class), any(), eq(1L));
    }

    @Test
    void deleteArticle_WithValidId_ShouldDeleteArticle() throws Exception {
        // Given
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(articleService.deleteArticle(1L, 1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/articles/1")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.message").value("文章删除成功"));

        verify(articleService).deleteArticle(1L, 1L);
    }

    @Test
    void publishArticle_WithValidId_ShouldPublishArticle() throws Exception {
        // Given
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(articleService.publishArticle(1L, 1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/articles/1/publish")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.message").value("文章发布成功"));

        verify(articleService).publishArticle(1L, 1L);
    }

    @Test
    void likeArticle_WithValidId_ShouldToggleLike() throws Exception {
        // Given
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(articleService.toggleLike(1L, 1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/articles/1/like")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.data.liked").value(true));

        verify(articleService).toggleLike(1L, 1L);
    }

    @Test
    void setArticleTop_WithValidId_ShouldSetTopStatus() throws Exception {
        // Given
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(articleService.setArticleTop(1L, true, 1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/articles/1/top")
                        .header("Authorization", validToken)
                        .param("isTop", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.message").value("文章置顶成功"));

        verify(articleService).setArticleTop(1L, true, 1L);
    }

    @Test
    void searchArticles_WithKeyword_ShouldReturnArticles() throws Exception {
        // Given
        List<Article> articles = Arrays.asList(testArticle);
        when(articleService.searchArticles("测试", 20)).thenReturn(articles);

        // When & Then
        mockMvc.perform(get("/articles/search")
                        .param("keyword", "测试")
                        .param("limit", "20")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(articleService).searchArticles("测试", 20);
    }

    @Test
    void getDraftArticles_ShouldReturnDraftArticles() throws Exception {
        // Given
        List<Article> drafts = Arrays.asList(testArticle);
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(articleService.getDraftArticles(1L)).thenReturn(drafts);

        // When & Then
        mockMvc.perform(get("/articles/drafts")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(articleService).getDraftArticles(1L);
    }

    @Test
    void getArticleStats_ShouldReturnStats() throws Exception {
        // Given
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(articleService.countArticlesByAuthor(1L, null)).thenReturn(10);
        when(articleService.countArticlesByAuthor(1L, Article.Status.PUBLISHED.getValue())).thenReturn(7);
        when(articleService.countArticlesByAuthor(1L, Article.Status.DRAFT.getValue())).thenReturn(3);

        // When & Then
        mockMvc.perform(get("/articles/stats")
                        .header("Authorization", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.data.totalArticles").value(10))
                .andExpected(jsonPath("$.data.publishedArticles").value(7))
                .andExpected(jsonPath("$.data.draftArticles").value(3));
    }

    @Test
    void createArticle_WithoutToken_ShouldReturn401() throws Exception {
        // When & Then
        mockMvc.perform(post("/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createArticle_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        createRequest.setTitle(""); // Empty title should fail validation
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/articles")
                        .header("Authorization", validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }
}