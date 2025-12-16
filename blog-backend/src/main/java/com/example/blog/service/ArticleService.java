package com.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.request.ArticleQueryRequest;
import com.example.blog.dto.response.ArticleListResponse;
import com.example.blog.dto.response.ArticleDetailResponse;
import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleVersion;
import com.example.blog.entity.ArticleOperationLog;

import java.time.LocalDateTime;
import java.util.List;

public interface ArticleService {

    Article getArticleById(Long id);

    Article getArticleWithDetails(Long id);

    IPage<Article> getArticlesWithPagination(Page<Article> page, Long categoryId, Long tagId, String keyword, String status);

    IPage<Article> getPublishedArticlesWithPagination(Page<Article> page, Long categoryId, Long tagId, String keyword);

    Article createArticle(Article article, List<Long> tagIds);

    Article updateArticle(Long id, Article article, List<Long> tagIds, Long currentUserId);

    boolean deleteArticle(Long id, Long currentUserId);

    boolean publishArticle(Long id, Long currentUserId);

    boolean unpublishArticle(Long id, Long currentUserId);

    boolean incrementViewCount(Long id);

    boolean likeArticle(Long id, Long userId);

    boolean unlikeArticle(Long id, Long userId);

    boolean toggleLike(Long id, Long userId);

    List<Article> getArticlesByAuthor(Long authorId, String status, Integer limit);

    List<Article> getPopularArticles(Integer limit);

    List<Article> getLatestArticles(Integer limit);

    List<Article> getRelatedArticles(Long id, Integer limit);

    List<Article> getDraftArticles(Long authorId);

    List<Article> getPublishedArticles(Long authorId);

    boolean setArticleTop(Long id, boolean isTop, Long currentUserId);

    List<Article> searchArticles(String keyword, Integer limit);

    int countArticlesByAuthor(Long authorId, String status);

    int countAllArticles(String status);

    /**
     * 获取总文章数
     */
    int getTotalArticleCount();

    /**
     * 获取已发布文章数
     */
    int getPublishedArticleCount();

    /**
     * 分页查询文章列表（带筛选和排序）
     */
    ArticleListResponse getArticleList(ArticleQueryRequest request);

    /**
     * 获取文章详情（带阅读量统计）
     */
    ArticleDetailResponse getArticleDetail(Long id, Long currentUserId);

    /**
     * 搜索文章
     */
    ArticleListResponse searchArticles(ArticleQueryRequest request);

    // 文章生命周期管理方法

    /**
     * 发布文章
     */
    boolean publishArticle(Long articleId);

    /**
     * 下线文章
     */
    boolean unpublishArticle(Long articleId);

    /**
     * 置顶文章
     */
    boolean pinArticle(Long articleId);

    /**
     * 取消置顶文章
     */
    boolean unpinArticle(Long articleId);

    /**
     * 定时发布文章
     */
    boolean schedulePublish(Long articleId, LocalDateTime scheduledTime);

    /**
     * 软删除文章
     */
    boolean softDeleteArticle(Long articleId);

    /**
     * 恢复文章
     */
    boolean restoreArticle(Long articleId);

    /**
     * 获取文章版本历史
     */
    List<ArticleVersion> getArticleVersions(Long articleId);

    /**
     * 获取文章操作日志
     */
    List<ArticleOperationLog> getArticleOperationLogs(Long articleId);

    /**
     * 获取已删除的文章
     */
    List<Article> getDeletedArticles(Long authorId);

    /**
     * 获取定时发布的文章
     */
    List<Article> getScheduledArticles(Long authorId);

    /**
     * 获取置顶的文章
     */
    List<Article> getPinnedArticles(Long authorId);
}