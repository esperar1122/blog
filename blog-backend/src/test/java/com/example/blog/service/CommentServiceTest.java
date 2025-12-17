package com.example.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.CommentQuery;
import com.example.blog.dto.CreateCommentRequest;
import com.example.blog.dto.UpdateCommentRequest;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CommentLikeMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private CommentLikeMapper commentLikeMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User testUser;
    private Article testArticle;
    private Comment testComment;
    private CreateCommentRequest createRequest;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");

        // 创建测试文章
        testArticle = new Article();
        testArticle.setId(1L);
        testArticle.setTitle("测试文章");
        testArticle.setAuthorId(1L);

        // 创建测试评论
        testComment = new Comment();
        testComment.setId(1L);
        testComment.setContent("测试评论内容");
        testComment.setArticleId(1L);
        testComment.setUserId(1L);
        testComment.setLevel(1);
        testComment.setLikeCount(0);
        testComment.setStatus(Comment.Status.NORMAL.getValue());
        testComment.setCreateTime(LocalDateTime.now());
        testComment.setUpdateTime(LocalDateTime.now());

        // 创建测试请求
        createRequest = new CreateCommentRequest();
        createRequest.setContent("这是一条新评论");
        createRequest.setArticleId(1L);
    }

    @Test
    void testCreateComment_Success() {
        // Given
        when(articleMapper.selectById(1L)).thenReturn(testArticle);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(commentMapper.insert(any(Comment.class))).thenReturn(1);
        doNothing().when(notificationService).sendNewCommentNotification(anyLong(), anyLong(), anyLong());

        // When
        Comment result = commentService.createComment(createRequest, 1L);

        // Then
        assertNotNull(result);
        assertEquals(createRequest.getContent(), result.getContent());
        assertEquals(createRequest.getArticleId(), result.getArticleId());
        assertEquals(1L, result.getUserId());
        assertEquals(Comment.Status.NORMAL.getValue(), result.getStatus());

        verify(articleMapper).selectById(1L);
        verify(userMapper).selectById(1L);
        verify(commentMapper).insert(any(Comment.class));
        verify(notificationService).sendNewCommentNotification(anyLong(), anyLong(), anyLong());
    }

    @Test
    void testCreateComment_WithParent_Success() {
        // Given
        createRequest.setParentId(1L);
        Comment parentComment = new Comment();
        parentComment.setId(1L);
        parentComment.setLevel(1);
        parentComment.setArticleId(1L);

        when(articleMapper.selectById(1L)).thenReturn(testArticle);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(commentMapper.selectById(1L)).thenReturn(parentComment);
        when(commentMapper.insert(any(Comment.class))).thenReturn(1);
        doNothing().when(notificationService).sendCommentReplyNotification(anyLong(), anyLong(), anyLong());

        // When
        Comment result = commentService.createComment(createRequest, 1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getLevel()); // 子评论的层级应该是2
        assertEquals(1L, result.getParentId());

        verify(commentMapper).selectById(1L);
        verify(notificationService).sendCommentReplyNotification(anyLong(), anyLong(), anyLong());
    }

    @Test
    void testCreateComment_ArticleNotFound_ThrowsException() {
        // Given
        when(articleMapper.selectById(1L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.createComment(createRequest, 1L);
        });

        assertEquals("文章不存在", exception.getMessage());
    }

    @Test
    void testCreateComment_ExceedMaxNestingLevel_ThrowsException() {
        // Given
        createRequest.setParentId(1L);
        Comment parentComment = new Comment();
        parentComment.setId(1L);
        parentComment.setLevel(5); // 已经是最大层级

        when(articleMapper.selectById(1L)).thenReturn(testArticle);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(commentMapper.selectById(1L)).thenReturn(parentComment);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.createComment(createRequest, 1L);
        });

        assertEquals("评论层级不能超过5层", exception.getMessage());
    }

    @Test
    void testUpdateComment_Success() {
        // Given
        UpdateCommentRequest updateRequest = new UpdateCommentRequest();
        updateRequest.setContent("更新后的评论内容");

        when(commentMapper.selectById(1L)).thenReturn(testComment);
        when(commentMapper.updateById(any(Comment.class))).thenReturn(1);

        // When
        Comment result = commentService.updateComment(1L, updateRequest, 1L);

        // Then
        assertNotNull(result);
        assertEquals(updateRequest.getContent(), result.getContent());

        verify(commentMapper).selectById(1L);
        verify(commentMapper).updateById(any(Comment.class));
    }

    @Test
    void testUpdateComment_NotOwner_ThrowsException() {
        // Given
        UpdateCommentRequest updateRequest = new UpdateCommentRequest();
        updateRequest.setContent("更新后的评论内容");

        testComment.setUserId(2L); // 设置为其他用户的评论
        when(commentMapper.selectById(1L)).thenReturn(testComment);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.updateComment(1L, updateRequest, 1L);
        });

        assertEquals("只能编辑自己的评论", exception.getMessage());
    }

    @Test
    void testDeleteComment_Success() {
        // Given
        when(commentMapper.selectById(1L)).thenReturn(testComment);
        when(articleMapper.selectById(1L)).thenReturn(testArticle);
        when(commentMapper.updateById(any(Comment.class))).thenReturn(1);
        when(commentMapper.count(any())).thenReturn(0L); // 评论数为0

        // When
        boolean result = commentService.deleteComment(1L, 1L);

        // Then
        assertTrue(result);

        verify(commentMapper).selectById(1L);
        verify(articleMapper).selectById(1L);
        verify(commentMapper).updateById(any(Comment.class));
    }

    @Test
    void testLikeComment_Success() {
        // Given
        when(commentMapper.selectById(1L)).thenReturn(testComment);
        when(commentLikeMapper.existsByCommentIdAndUserId(1L, 1L)).thenReturn(false);
        when(commentLikeMapper.insert(any())).thenReturn(1);
        when(commentMapper.updateById(any(Comment.class))).thenReturn(1);

        // When
        boolean result = commentService.likeComment(1L, 1L);

        // Then
        assertTrue(result);

        verify(commentLikeMapper).existsByCommentIdAndUserId(1L, 1L);
        verify(commentLikeMapper).insert(any());
        verify(commentMapper).updateById(any(Comment.class));
    }

    @Test
    void testLikeComment_AlreadyLiked_ThrowsException() {
        // Given
        when(commentMapper.selectById(1L)).thenReturn(testComment);
        when(commentLikeMapper.existsByCommentIdAndUserId(1L, 1L)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.likeComment(1L, 1L);
        });

        assertEquals("已经点赞过了", exception.getMessage());
    }

    @Test
    void testUnlikeComment_Success() {
        // Given
        when(commentMapper.selectById(1L)).thenReturn(testComment);
        when(commentLikeMapper.existsByCommentIdAndUserId(1L, 1L)).thenReturn(true);
        when(commentLikeMapper.deleteByCommentIdAndUserId(1L, 1L)).thenReturn(1);
        when(commentMapper.updateById(any(Comment.class))).thenReturn(1);

        // When
        boolean result = commentService.unlikeComment(1L, 1L);

        // Then
        assertTrue(result);

        verify(commentLikeMapper).existsByCommentIdAndUserId(1L, 1L);
        verify(commentLikeMapper).deleteByCommentIdAndUserId(1L, 1L);
        verify(commentMapper).updateById(any(Comment.class));
    }

    @Test
    void testGetCommentCountByArticleId_Success() {
        // Given
        when(commentMapper.count(any())).thenReturn(10L);

        // When
        long result = commentService.getCommentCountByArticleId(1L);

        // Then
        assertEquals(10L, result);
        verify(commentMapper).count(any());
    }

    @Test
    void testHasUserLikedComment_ReturnsTrue() {
        // Given
        when(commentLikeMapper.existsByCommentIdAndUserId(1L, 1L)).thenReturn(true);

        // When
        boolean result = commentService.hasUserLikedComment(1L, 1L);

        // Then
        assertTrue(result);
        verify(commentLikeMapper).existsByCommentIdAndUserId(1L, 1L);
    }
}