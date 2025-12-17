package com.example.blog.controller;

import com.example.blog.common.Result;
import com.example.blog.dto.CreateTagRequest;
import com.example.blog.dto.UpdateTagRequest;
import com.example.blog.entity.Article;
import com.example.blog.entity.Tag;
import com.example.blog.service.ArticleService;
import com.example.blog.service.TagService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TagController.class)
@DisplayName("TagController 集成测试")
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TagService tagService;

    @MockBean
    private ArticleService articleService;

    private Tag testTag;

    @BeforeEach
    void setUp() {
        testTag = new Tag();
        testTag.setId(1L);
        testTag.setName("Java");
        testTag.setColor("#007396");
        testTag.setArticleCount(5);
    }

    @Test
    @DisplayName("获取所有标签 - 成功")
    void getAllTags_Success() throws Exception {
        // Given
        List<Tag> tags = Arrays.asList(testTag);
        when(tagService.getAllTags()).thenReturn(tags);

        // When & Then
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Java"));

        verify(tagService).getAllTags();
    }

    @Test
    @DisplayName("获取热门标签 - 成功")
    void getPopularTags_Success() throws Exception {
        // Given
        List<Tag> popularTags = Arrays.asList(testTag);
        when(tagService.getPopularTags(10)).thenReturn(popularTags);

        // When & Then
        mockMvc.perform(get("/api/tags/popular")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Java"));

        verify(tagService).getPopularTags(10);
    }

    @Test
    @DisplayName("获取热门标签 - 使用默认值")
    void getPopularTags_WithDefaultLimit() throws Exception {
        // Given
        when(tagService.getPopularTags(10)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/tags/popular"))
                .andExpect(status().isOk());

        verify(tagService).getPopularTags(10);
    }

    @Test
    @DisplayName("根据ID获取标签 - 成功")
    void getTagById_Success() throws Exception {
        // Given
        when(tagService.getTagById(1L)).thenReturn(testTag);

        // When & Then
        mockMvc.perform(get("/api/tags/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Java"));

        verify(tagService).getTagById(1L);
    }

    @Test
    @DisplayName("根据ID获取标签 - 不存在")
    void getTagById_NotFound() throws Exception {
        // Given
        when(tagService.getTagById(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/tags/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("标签不存在"));

        verify(tagService).getTagById(999L);
    }

    @Test
    @DisplayName("创建标签 - 需要管理员权限")
    void createTag_WithoutAdmin() throws Exception {
        // Given
        CreateTagRequest request = new CreateTagRequest();
        request.setName("New Tag");
        request.setColor("#FF0000");

        // When & Then
        mockMvc.perform(post("/api/tags")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(tagService, never()).createTag(any());
    }

    @Test
    @DisplayName("创建标签 - 管理员成功")
    @WithMockUser(roles = "ADMIN")
    void createTag_WithAdmin_Success() throws Exception {
        // Given
        CreateTagRequest request = new CreateTagRequest();
        request.setName("Spring Boot");
        request.setColor("#6DB33F");

        when(tagService.createTag(any(Tag.class))).thenReturn(testTag);

        // When & Then
        mockMvc.perform(post("/api/tags")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Java"));

        verify(tagService).createTag(any(Tag.class));
    }

    @Test
    @DisplayName("创建标签 - 验证失败")
    @WithMockUser(roles = "ADMIN")
    void createTag_ValidationFailed() throws Exception {
        // Given
        CreateTagRequest request = new CreateTagRequest();
        request.setName(""); // 空名称，应该验证失败
        request.setColor("invalid-color"); // 无效颜色格式

        // When & Then
        mockMvc.perform(post("/api/tags")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("更新标签 - 需要管理员权限")
    void updateTag_WithoutAdmin() throws Exception {
        // Given
        UpdateTagRequest request = new UpdateTagRequest();
        request.setName("Updated Tag");

        // When & Then
        mockMvc.perform(put("/api/tags/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(tagService, never()).updateTag(any(), any());
    }

    @Test
    @DisplayName("更新标签 - 管理员成功")
    @WithMockUser(roles = "ADMIN")
    void updateTag_WithAdmin_Success() throws Exception {
        // Given
        UpdateTagRequest request = new UpdateTagRequest();
        request.setName("Spring Framework");
        request.setColor("#6DB33F");

        when(tagService.updateTag(eq(1L), any(Tag.class))).thenReturn(testTag);

        // When & Then
        mockMvc.perform(put("/api/tags/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(tagService).updateTag(eq(1L), any(Tag.class));
    }

    @Test
    @DisplayName("删除标签 - 需要管理员权限")
    void deleteTag_WithoutAdmin() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/tags/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(tagService, never()).deleteTag(any());
    }

    @Test
    @DisplayName("删除标签 - 管理员成功")
    @WithMockUser(roles = "ADMIN")
    void deleteTag_WithAdmin_Success() throws Exception {
        // Given
        when(tagService.deleteTag(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/tags/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("标签删除成功"));

        verify(tagService).deleteTag(1L);
    }

    @Test
    @DisplayName("删除标签 - 标签不存在")
    @WithMockUser(roles = "ADMIN")
    void deleteTag_WithAdmin_NotFound() throws Exception {
        // Given
        when(tagService.deleteTag(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/tags/999")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("标签不存在"));

        verify(tagService).deleteTag(999L);
    }

    @Test
    @DisplayName("检查标签名称是否存在 - 存在")
    void checkNameExists_True() throws Exception {
        // Given
        when(tagService.existsByName("Java")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/tags/check/name")
                        .param("name", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));

        verify(tagService).existsByName("Java");
    }

    @Test
    @DisplayName("检查标签名称是否存在 - 排除指定ID")
    void checkNameExists_ExcludeId() throws Exception {
        // Given
        when(tagService.existsByNameAndExcludeId("Java", 1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/tags/check/name")
                        .param("name", "Java")
                        .param("excludeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("搜索标签 - 成功")
    void searchTags_Success() throws Exception {
        // Given
        List<Tag> searchResults = Arrays.asList(testTag);
        when(tagService.searchTagsByName("Java", 20)).thenReturn(searchResults);

        // When & Then
        mockMvc.perform(get("/api/tags/search")
                        .param("name", "Java")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Java"));

        verify(tagService).searchTagsByName("Java", 20);
    }

    @Test
    @DisplayName("获取标签下的文章 - 成功")
    void getArticlesByTag_Success() throws Exception {
        // Given
        when(tagService.getTagById(1L)).thenReturn(testTag);

        Page<Article> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList());
        when(articleService.getPublishedArticlesWithPagination(any(Page.class), isNull(), eq(1L), isNull()))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/tags/1/articles")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.tag.name").value("Java"));

        verify(tagService).getTagById(1L);
        verify(articleService).getPublishedArticlesWithPagination(any(Page.class), isNull(), eq(1L), isNull());
    }

    @Test
    @DisplayName("获取标签下的文章 - 标签不存在")
    void getArticlesByTag_TagNotFound() throws Exception {
        // Given
        when(tagService.getTagById(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/tags/999/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("标签不存在"));

        verify(tagService).getTagById(999L);
        verify(articleService, never()).getPublishedArticlesWithPagination(any(), any(), any(), any());
    }

    @Test
    @DisplayName("获取标签下的文章 - 使用默认分页参数")
    void getArticlesByTag_WithDefaultPagination() throws Exception {
        // Given
        when(tagService.getTagById(1L)).thenReturn(testTag);
        Page<Article> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList());
        when(articleService.getPublishedArticlesWithPagination(any(Page.class), isNull(), eq(1L), isNull()))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/tags/1/articles"))
                .andExpect(status().isOk());

        // Verify default parameters are used
        verify(articleService).getPublishedArticlesWithPagination(any(Page.class), isNull(), eq(1L), isNull());
    }
}