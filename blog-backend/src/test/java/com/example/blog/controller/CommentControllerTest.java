package com.example.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.Result;
import com.example.blog.dto.CommentQuery;
import com.example.blog.dto.CommentResponse;
import com.example.blog.dto.CreateCommentRequest;
import com.example.blog.dto.UpdateCommentRequest;
import com.example.blog.entity.Comment;
import com.example.blog.service.CommentService;
import com.example.blog.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Comment testComment;
    private CreateCommentRequest createRequest;
    private UpdateCommentRequest updateRequest;
    private String testToken;

    @BeforeEach
    void setUp() {
        // 创建测试评论
        testComment = new Comment();
        testComment.setId(1L);
        testComment.setContent("测试评论");
        testComment.setArticleId(1L);
        testComment.setUserId(1L);
        testComment.setLevel(1);
        testComment.setLikeCount(0);
        testComment.setStatus(Comment.Status.NORMAL.getValue());
        testComment.setCreateTime(LocalDateTime.now());
        testComment.setUpdateTime(LocalDateTime.now());

        // 创建测试请求
        createRequest = new CreateCommentRequest();
        createRequest.setContent("新评论内容");
        createRequest.setArticleId(1L);

        updateRequest = new UpdateCommentRequest();
        updateRequest.setContent("更新后的评论内容");

        // 创建测试token
        testToken = "Bearer test.jwt.token";
    }

    @Test
    void testCreateComment_Success() throws Exception {
        // Given
        when(commentService.createComment(any(CreateCommentRequest.class), anyLong()))
                .thenReturn(testComment);
        when(JwtUtil.getUserIdFromRequest(any())).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/api/v1/comments")
                        .header("Authorization", testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.content").value("测试评论"));

        verify(commentService).createComment(any(CreateCommentRequest.class), eq(1L));
    }

    @Test
    void testCreateComment_Unauthorized_ReturnsError() throws Exception {
        // Given
        when(JwtUtil.getUserIdFromRequest(any())).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/v1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateComment_Success() throws Exception {
        // Given
        when(commentService.updateComment(eq(1L), any(UpdateCommentRequest.class), anyLong()))
                .thenReturn(testComment);
        when(JwtUtil.getUserIdFromRequest(any())).thenReturn(1L);

        // When & Then
        mockMvc.perform(put("/api/v1/comments/1")
                        .header("Authorization", testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(commentService).updateComment(eq(1L), any(UpdateCommentRequest.class), eq(1L));
    }

    @Test
    void testDeleteComment_Success() throws Exception {
        // Given
        when(commentService.deleteComment(eq(1L), anyLong())).thenReturn(true);
        when(JwtUtil.getUserIdFromRequest(any())).thenReturn(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/comments/1")
                        .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(commentService).deleteComment(eq(1L), eq(1L));
    }

    @Test
    void testGetComments_Success() throws Exception {
        // Given
        CommentQuery query = new CommentQuery();
        query.setArticleId(1L);
        query.setPage(1);
        query.setSize(20);

        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(1L);
        commentResponse.setContent("测试评论");
        commentResponse.setArticleId(1L);

        Page<CommentResponse> mockPage = new Page<>(1, 20, 1);
        mockPage.setRecords(Arrays.asList(commentResponse));

        when(commentService.getCommentsByArticleId(any(CommentQuery.class)))
                .thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/api/v1/comments")
                        .param("articleId", "1")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].id").value(1L));

        verify(commentService).getCommentsByArticleId(any(CommentQuery.class));
    }

    @Test
    void testGetNestedComments_Success() throws Exception {
        // Given
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(1L);
        commentResponse.setContent("测试评论");
        commentResponse.setArticleId(1L);

        List<CommentResponse> comments = Arrays.asList(commentResponse);

        when(commentService.getNestedCommentsByArticleId(anyLong(), anyString(), anyString()))
                .thenReturn(comments);

        // When & Then
        mockMvc.perform(get("/api/v1/comments/nested")
                        .param("articleId", "1")
                        .param("sortBy", "createTime")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L));

        verify(commentService).getNestedCommentsByArticleId(eq(1L), eq("createTime"), eq("desc"));
    }

    @Test
    void testGetArticleComments_Success() throws Exception {
        // Given
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(1L);
        commentResponse.setContent("测试评论");
        commentResponse.setArticleId(1L);

        List<CommentResponse> comments = Arrays.asList(commentResponse);

        when(commentService.getNestedCommentsByArticleId(anyLong(), anyString(), anyString()))
                .thenReturn(comments);

        // When & Then
        mockMvc.perform(get("/api/v1/comments/articles/1/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1L));

        verify(commentService).getNestedCommentsByArticleId(eq(1L), eq("createTime"), eq("desc"));
    }

    @Test
    void testLikeComment_Success() throws Exception {
        // Given
        when(commentService.likeComment(eq(1L), anyLong())).thenReturn(true);
        when(JwtUtil.getUserIdFromRequest(any())).thenReturn(1L);

        // When & Then
        mockMvc.perform(post("/api/v1/comments/1/like")
                        .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(commentService).likeComment(eq(1L), eq(1L));
    }

    @Test
    void testUnlikeComment_Success() throws Exception {
        // Given
        when(commentService.unlikeComment(eq(1L), anyLong())).thenReturn(true);
        when(JwtUtil.getUserIdFromRequest(any())).thenReturn(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/comments/1/like")
                        .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(commentService).unlikeComment(eq(1L), eq(1L));
    }

    @Test
    void testCheckIfLiked_Success() throws Exception {
        // Given
        when(commentService.hasUserLikedComment(eq(1L), anyLong())).thenReturn(true);
        when(JwtUtil.getUserIdFromRequest(any())).thenReturn(1L);

        // When & Then
        mockMvc.perform(get("/api/v1/comments/1/liked")
                        .header("Authorization", testToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.liked").value(true));

        verify(commentService).hasUserLikedComment(eq(1L), eq(1L));
    }

    @Test
    void testGetCommentCount_Success() throws Exception {
        // Given
        when(commentService.getCommentCountByArticleId(1L)).thenReturn(10L);

        // When & Then
        mockMvc.perform(get("/api/v1/comments/articles/1/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.count").value(10));

        verify(commentService).getCommentCountByArticleId(1L);
    }

    @Test
    void testGetMyComments_Success() throws Exception {
        // Given
        when(commentService.getCommentsByUserId(eq(1L), any(Page.class)))
                .thenReturn(new Page<>(1, 10, 5));
        when(JwtUtil.getUserIdFromRequest(any())).thenReturn(1L);

        // When & Then
        mockMvc.perform(get("/api/v1/comments/user/me")
                        .header("Authorization", testToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(commentService).getCommentsByUserId(eq(1L), any(Page.class));
    }

    @Test
    void testCreateComment_ValidationError_ReturnsError() throws Exception {
        // Given - 创建无效的请求（内容为空）
        CreateCommentRequest invalidRequest = new CreateCommentRequest();
        invalidRequest.setContent("");
        invalidRequest.setArticleId(1L);

        // When & Then
        mockMvc.perform(post("/api/v1/comments")
                        .header("Authorization", testToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}