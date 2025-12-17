package com.example.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.request.ArticleReviewRequest;
import com.example.blog.dto.request.BatchOperationRequest;
import com.example.blog.dto.request.ContentQueryRequest;
import com.example.blog.dto.response.ContentStatsResponse;
import com.example.blog.dto.response.ExportResponse;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.UserMapper;
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
public class AdminContentServiceTest {

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SensitiveWordService sensitiveWordService;

    @Mock
    private AdminLogService adminLogService;

    @InjectMocks
    private AdminContentServiceImpl adminContentService;

    private Article mockArticle;
    private Comment mockComment;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        mockArticle = new Article();
        mockArticle.setId(1L);
        mockArticle.setTitle("测试文章");
        mockArticle.setContent("测试内容");
        mockArticle.setAuthorId(1L);
        mockArticle.setStatus("PUBLISHED");
        mockArticle.setCreateTime(LocalDateTime.now());

        mockComment = new Comment();
        mockComment.setId(1L);
        mockComment.setContent("测试评论");
        mockComment.setArticleId(1L);
        mockComment.setUserId(1L);
        mockComment.setStatus("NORMAL");
        mockComment.setCreateTime(LocalDateTime.now());
    }

    @Test
    void testGetArticleList() {
        ContentQueryRequest request = new ContentQueryRequest();
        request.setPage(0);
        request.setSize(20);
        request.setKeyword("测试");
        request.setStatus("PUBLISHED");

        Page<Article> expectedPage = new Page<>(1, 20);
        expectedPage.setRecords(Arrays.asList(mockArticle));
        expectedPage.setTotal(1);

        when(articleMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(expectedPage);
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        IPage<Article> result = adminContentService.getArticleList(request);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("测试文章", result.getRecords().get(0).getTitle());
        assertEquals("testuser", result.getRecords().get(0).getAuthorName());

        verify(articleMapper, times(1)).selectPage(any(Page.class), any(QueryWrapper.class));
        verify(userMapper, times(1)).selectById(1L);
    }

    @Test
    void testGetCommentList() {
        ContentQueryRequest request = new ContentQueryRequest();
        request.setPage(0);
        request.setSize(20);
        request.setKeyword("测试");
        request.setStatus("NORMAL");

        Page<Comment> expectedPage = new Page<>(1, 20);
        expectedPage.setRecords(Arrays.asList(mockComment));
        expectedPage.setTotal(1);

        when(commentMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(expectedPage);
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        IPage<Comment> result = adminContentService.getCommentList(request);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("测试评论", result.getRecords().get(0).getContent());
        assertEquals("testuser", result.getRecords().get(0).getUserName());

        verify(commentMapper, times(1)).selectPage(any(Page.class), any(QueryWrapper.class));
        verify(userMapper, times(1)).selectById(1L);
    }

    @Test
    void testReviewArticle_Success() {
        ArticleReviewRequest request = new ArticleReviewRequest();
        request.setId(1L);
        request.setTitle("审核后的文章");
        request.setContent("审核后的内容");
        request.setStatus("PUBLISHED");
        request.setSummary("审核后的摘要");

        when(articleMapper.selectById(1L)).thenReturn(mockArticle);
        when(articleMapper.updateById(any(Article.class))).thenReturn(1);

        Article result = adminContentService.reviewArticle(request, 1L);

        assertNotNull(result);
        assertEquals("审核后的文章", result.getTitle());
        assertEquals("审核后的内容", result.getContent());
        assertEquals("PUBLISHED", result.getStatus());
        assertNotNull(result.getPublishTime());

        verify(articleMapper, times(1)).selectById(1L);
        verify(articleMapper, times(1)).updateById(any(Article.class));
        verify(adminLogService, times(1)).log(eq(1L), eq("REVIEW_ARTICLE"), anyString());
    }

    @Test
    void testReviewArticle_ArticleNotFound() {
        ArticleReviewRequest request = new ArticleReviewRequest();
        request.setId(999L);
        request.setTitle("审核后的文章");

        when(articleMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            adminContentService.reviewArticle(request, 1L);
        });

        assertEquals("文章不存在", exception.getMessage());
        verify(articleMapper, times(1)).selectById(999L);
        verify(articleMapper, never()).updateById(any(Article.class));
    }

    @Test
    void testBatchOperateArticles_Delete() {
        BatchOperationRequest request = new BatchOperationRequest();
        request.setOperationType("delete");
        request.setIds(Arrays.asList(1L, 2L));

        when(articleMapper.update(any(), any(QueryWrapper.class))).thenReturn(2);

        assertDoesNotThrow(() -> {
            adminContentService.batchOperateArticles(request, 1L);
        });

        verify(articleMapper, times(1)).update(any(), argThat(wrapper -> {
            QueryWrapper<Article> queryWrapper = (QueryWrapper<Article>) wrapper;
            String sqlSegment = queryWrapper.getSqlSegment();
            return sqlSegment.contains("id IN") && sqlSegment.contains("status =");
        }));
        verify(adminLogService, times(1)).log(eq(1L), eq("BATCH_DELETE_ARTICLE"), anyString());
    }

    @Test
    void testBatchOperateArticles_Publish() {
        BatchOperationRequest request = new BatchOperationRequest();
        request.setOperationType("publish");
        request.setIds(Arrays.asList(1L, 2L));

        when(articleMapper.update(any(), any(QueryWrapper.class))).thenReturn(2);

        assertDoesNotThrow(() -> {
            adminContentService.batchOperateArticles(request, 1L);
        });

        verify(articleMapper, times(1)).update(any(), argThat(wrapper -> {
            QueryWrapper<Article> queryWrapper = (QueryWrapper<Article>) wrapper;
            String sqlSegment = queryWrapper.getSqlSegment();
            return sqlSegment.contains("id IN") && sqlSegment.contains("status =");
        }));
        verify(adminLogService, times(1)).log(eq(1L), eq("BATCH_PUBLISH_ARTICLE"), anyString());
    }

    @Test
    void testBatchOperateArticles_UnsupportedOperation() {
        BatchOperationRequest request = new BatchOperationRequest();
        request.setOperationType("unsupported");
        request.setIds(Arrays.asList(1L));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            adminContentService.batchOperateArticles(request, 1L);
        });

        assertEquals("不支持的操作类型", exception.getMessage());
        verify(articleMapper, never()).update(any(), any(QueryWrapper.class));
    }

    @Test
    void testBatchOperateArticles_EmptyIds() {
        BatchOperationRequest request = new BatchOperationRequest();
        request.setOperationType("delete");
        request.setIds(Arrays.asList());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            adminContentService.batchOperateArticles(request, 1L);
        });

        assertEquals("请选择要操作的文章", exception.getMessage());
        verify(articleMapper, never()).update(any(), any(QueryWrapper.class));
    }

    @Test
    void testBatchOperateComments() {
        BatchOperationRequest request = new BatchOperationRequest();
        request.setOperationType("delete");
        request.setIds(Arrays.asList(1L, 2L));

        when(commentMapper.update(any(), any(QueryWrapper.class))).thenReturn(2);

        assertDoesNotThrow(() -> {
            adminContentService.batchOperateComments(request, 1L);
        });

        verify(commentMapper, times(1)).update(any(), argThat(wrapper -> {
            QueryWrapper<Comment> queryWrapper = (QueryWrapper<Comment>) wrapper;
            String sqlSegment = queryWrapper.getSqlSegment();
            return sqlSegment.contains("id IN") && sqlSegment.contains("status =");
        }));
        verify(adminLogService, times(1)).log(eq(1L), eq("BATCH_DELETE_COMMENT"), anyString());
    }

    @Test
    void testExportArticles() {
        ContentQueryRequest filters = new ContentQueryRequest();
        filters.setStatus("PUBLISHED");

        List<Article> articles = Arrays.asList(mockArticle);
        when(articleMapper.selectList(any(QueryWrapper.class))).thenReturn(articles);

        ExportResponse result = adminContentService.exportArticles(filters, "json");

        assertNotNull(result);
        assertTrue(result.getFileName().endsWith(".json"));
        assertEquals("json", result.getFormat());
        assertEquals(1, result.getRecordCount());
        assertTrue(result.getFileSize() > 0);

        verify(articleMapper, times(1)).selectList(any(QueryWrapper.class));
    }

    @Test
    void testExportComments() {
        ContentQueryRequest filters = new ContentQueryRequest();
        filters.setStatus("NORMAL");

        List<Comment> comments = Arrays.asList(mockComment);
        when(commentMapper.selectList(any(QueryWrapper.class))).thenReturn(comments);

        ExportResponse result = adminContentService.exportComments(filters, "csv");

        assertNotNull(result);
        assertTrue(result.getFileName().endsWith(".csv"));
        assertEquals("csv", result.getFormat());
        assertEquals(1, result.getRecordCount());
        assertTrue(result.getFileSize() > 0);

        verify(commentMapper, times(1)).selectList(any(QueryWrapper.class));
    }

    @Test
    void testGetContentStats() {
        when(articleMapper.selectCount(any(QueryWrapper.class))).thenReturn(100L, 80L, 20L);
        when(commentMapper.selectCount(any(QueryWrapper.class))).thenReturn(200L, 190L, 10L);

        ContentStatsResponse result = adminContentService.getContentStats();

        assertNotNull(result);
        assertEquals(100L, result.getTotalArticles());
        assertEquals(80L, result.getPublishedArticles());
        assertEquals(20L, result.getDraftArticles());
        assertEquals(200L, result.getTotalComments());
        assertEquals(190L, result.getActiveComments());
        assertEquals(10L, result.getDeletedComments());

        verify(articleMapper, times(3)).selectCount(any(QueryWrapper.class));
        verify(commentMapper, times(3)).selectCount(any(QueryWrapper.class));
    }

    @Test
    void testCheckContent() {
        when(sensitiveWordService.containsSensitiveWord("包含敏感词的内容")).thenReturn(true);
        when(sensitiveWordService.containsSensitiveWord("正常内容")).thenReturn(false);

        assertTrue(adminContentService.checkContent("包含敏感词的内容"));
        assertFalse(adminContentService.checkContent("正常内容"));

        verify(sensitiveWordService, times(1)).containsSensitiveWord("包含敏感词的内容");
        verify(sensitiveWordService, times(1)).containsSensitiveWord("正常内容");
    }

    @Test
    void testFilterContent() {
        when(sensitiveWordService.filterSensitiveWords("包含敏感词的内容")).thenReturn("包含***的内容");
        when(sensitiveWordService.filterSensitiveWords("正常内容")).thenReturn("正常内容");

        assertEquals("包含***的内容", adminContentService.filterContent("包含敏感词的内容"));
        assertEquals("正常内容", adminContentService.filterContent("正常内容"));

        verify(sensitiveWordService, times(1)).filterSensitiveWords("包含敏感词的内容");
        verify(sensitiveWordService, times(1)).filterSensitiveWords("正常内容");
    }
}