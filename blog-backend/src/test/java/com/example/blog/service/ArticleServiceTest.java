package com.example.blog.service;

import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleTag;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private ArticleTagMapper articleTagMapper;

    @Mock
    private ArticleLikeMapper articleLikeMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ArticleServiceImpl articleService;

    private Article testArticle;
    private User testUser;

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
        testArticle.setViewCount(0);
        testArticle.setLikeCount(0);
        testArticle.setCommentCount(0);
        testArticle.setIsTop(false);

        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");
    }

    @Test
    void getArticleById_WithExistingId_ShouldReturnArticle() {
        // Given
        when(articleMapper.selectById(1L)).thenReturn(testArticle);

        // When
        Article result = articleService.getArticleById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试文章", result.getTitle());
        verify(articleMapper).selectById(1L);
    }

    @Test
    void getArticleById_WithNonExistingId_ShouldThrowException() {
        // Given
        when(articleMapper.selectById(999L)).thenReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.getArticleById(999L);
        });

        assertEquals("文章不存在", exception.getMessage());
        verify(articleMapper).selectById(999L);
    }

    @Test
    void createArticle_WithValidData_ShouldCreateArticle() {
        // Given
        List<Long> tagIds = Arrays.asList(1L, 2L);
        when(articleMapper.insert(any(Article.class))).thenReturn(1);
        when(categoryMapper.incrementArticleCount(1L)).thenReturn(1);
        when(tagMapper.incrementArticleCount(anyLong())).thenReturn(1);

        // When
        Article result = articleService.createArticle(testArticle, tagIds);

        // Then
        assertNotNull(result);
        assertEquals("测试文章", result.getTitle());
        assertEquals(Article.Status.DRAFT.getValue(), result.getStatus());
        assertEquals(0, result.getViewCount());
        assertFalse(result.getIsTop());
        verify(articleMapper).insert(any(Article.class));
        verify(articleTagMapper, times(2)).insert(any(ArticleTag.class));
        verify(categoryMapper).incrementArticleCount(1L);
        verify(tagMapper, times(2)).incrementArticleCount(anyLong());
    }

    @Test
    void updateArticle_WithValidDataAndAuthor_ShouldUpdateArticle() {
        // Given
        List<Long> oldTagIds = Arrays.asList(1L);
        List<Long> newTagIds = Arrays.asList(2L);
        Article updateData = new Article();
        updateData.setTitle("更新后的标题");
        updateData.setContent("更新后的内容");

        when(articleMapper.selectById(1L)).thenReturn(testArticle);
        when(articleMapper.updateById(any(Article.class))).thenReturn(1);
        when(articleTagMapper.selectTagIdsByArticleId(1L)).thenReturn(oldTagIds);
        doNothing().when(articleTagMapper).deleteArticleTagsByArticleId(1L);
        when(tagMapper.incrementArticleCount(anyLong())).thenReturn(1);
        when(tagMapper.decrementArticleCount(anyLong())).thenReturn(1);

        // When
        Article result = articleService.updateArticle(1L, updateData, newTagIds, 1L);

        // Then
        assertNotNull(result);
        assertEquals("更新后的标题", result.getTitle());
        assertEquals("更新后的内容", result.getContent());
        verify(articleMapper).updateById(any(Article.class));
        verify(articleTagMapper).deleteArticleTagsByArticleId(1L);
        verify(tagMapper).decrementArticleCount(1L);
        verify(tagMapper).incrementArticleCount(2L);
    }

    @Test
    void updateArticle_WithUnauthorizedUser_ShouldThrowException() {
        // Given
        Article updateData = new Article();
        updateData.setTitle("更新后的标题");
        testArticle.setAuthorId(2L); // Different author

        when(articleMapper.selectById(1L)).thenReturn(testArticle);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.updateArticle(1L, updateData, null, 1L);
        });

        assertEquals("无权限修改此文章", exception.getMessage());
        verify(articleMapper, never()).updateById(any());
    }

    @Test
    void deleteArticle_WithAuthorizedUser_ShouldSoftDeleteArticle() {
        // Given
        when(articleMapper.selectById(1L)).thenReturn(testArticle);
        when(articleMapper.updateById(any(Article.class))).thenReturn(1);
        when(articleTagMapper.selectTagIdsByArticleId(1L)).thenReturn(Arrays.asList(1L));
        doNothing().when(articleTagMapper).deleteArticleTagsByArticleId(1L);
        doNothing().when(articleLikeMapper).deleteLikesByArticleId(1L);
        when(categoryMapper.decrementArticleCount(1L)).thenReturn(1);
        when(tagMapper.decrementArticleCount(anyLong())).thenReturn(1);

        // When
        boolean result = articleService.deleteArticle(1L, 1L);

        // Then
        assertTrue(result);
        assertEquals(Article.Status.DELETED.getValue(), testArticle.getStatus());
        verify(articleMapper).updateById(testArticle);
        verify(articleTagMapper).deleteArticleTagsByArticleId(1L);
        verify(articleLikeMapper).deleteLikesByArticleId(1L);
        verify(categoryMapper).decrementArticleCount(1L);
        verify(tagMapper).decrementArticleCount(1L);
    }

    @Test
    void publishArticle_WithAuthorizedUser_ShouldPublishArticle() {
        // Given
        when(articleMapper.selectById(1L)).thenReturn(testArticle);
        when(articleMapper.updateById(any(Article.class))).thenReturn(1);

        // When
        boolean result = articleService.publishArticle(1L, 1L);

        // Then
        assertTrue(result);
        assertEquals(Article.Status.PUBLISHED.getValue(), testArticle.getStatus());
        assertNotNull(testArticle.getPublishTime());
        verify(articleMapper).updateById(testArticle);
    }

    @Test
    void likeArticle_WithNewLike_ShouldCreateLikeAndIncrementCount() {
        // Given
        when(articleLikeMapper.existsByArticleIdAndUserId(1L, 1L)).thenReturn(false);
        when(articleLikeMapper.insert(any())).thenReturn(1);
        when(articleMapper.incrementLikeCount(1L)).thenReturn(1);
        when(articleMapper.selectById(1L)).thenReturn(testArticle);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        doNothing().when(notificationService).createLikeNotification(anyLong(), anyString(), anyString(), anyLong());

        // When
        boolean result = articleService.likeArticle(1L, 1L);

        // Then
        assertTrue(result);
        verify(articleLikeMapper).insert(any());
        verify(articleMapper).incrementLikeCount(1L);
        verify(notificationService).createLikeNotification(eq(1L), anyString(), anyString(), eq(1L));
    }

    @Test
    void likeArticle_WithExistingLike_ShouldNotCreateLike() {
        // Given
        when(articleLikeMapper.existsByArticleIdAndUserId(1L, 1L)).thenReturn(true);

        // When
        boolean result = articleService.likeArticle(1L, 1L);

        // Then
        assertFalse(result);
        verify(articleLikeMapper, never()).insert(any());
        verify(articleMapper, never()).incrementLikeCount(anyLong());
    }

    @Test
    void toggleLike_WithNoExistingLike_ShouldCreateLike() {
        // Given
        when(articleLikeMapper.existsByArticleIdAndUserId(1L, 1L)).thenReturn(false);
        when(articleMapper.selectById(1L)).thenReturn(testArticle);
        when(articleLikeMapper.insert(any())).thenReturn(1);
        when(articleMapper.incrementLikeCount(1L)).thenReturn(1);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // When
        boolean result = articleService.toggleLike(1L, 1L);

        // Then
        assertTrue(result);
        verify(articleLikeMapper).insert(any());
        verify(articleMapper).incrementLikeCount(1L);
    }

    @Test
    void toggleLike_WithExistingLike_ShouldRemoveLike() {
        // Given
        when(articleLikeMapper.existsByArticleIdAndUserId(1L, 1L)).thenReturn(true);
        when(articleLikeMapper.deleteByArticleIdAndUserId(1L, 1L)).thenReturn(1);
        when(articleMapper.decrementLikeCount(1L)).thenReturn(1);

        // When
        boolean result = articleService.toggleLike(1L, 1L);

        // Then
        assertFalse(result);
        verify(articleLikeMapper).deleteByArticleIdAndUserId(1L, 1L);
        verify(articleMapper).decrementLikeCount(1L);
    }

    @Test
    void incrementViewCount_ShouldCallMapper() {
        // Given
        when(articleMapper.incrementViewCount(1L)).thenReturn(1);

        // When
        boolean result = articleService.incrementViewCount(1L);

        // Then
        assertTrue(result);
        verify(articleMapper).incrementViewCount(1L);
    }

    @Test
    void searchArticles_WithKeyword_ShouldReturnArticles() {
        // Given
        List<Article> searchResults = Arrays.asList(testArticle);
        when(articleMapper.selectList(any())).thenReturn(searchResults);

        // When
        List<Article> result = articleService.searchArticles("测试", 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("测试文章", result.get(0).getTitle());
    }

    @Test
    void countArticlesByAuthor_ShouldReturnCorrectCount() {
        // Given
        when(articleMapper.selectCount(any())).thenReturn(5L);

        // When
        int result = articleService.countArticlesByAuthor(1L, Article.Status.PUBLISHED.getValue());

        // Then
        assertEquals(5, result);
        verify(articleMapper).selectCount(any());
    }
}