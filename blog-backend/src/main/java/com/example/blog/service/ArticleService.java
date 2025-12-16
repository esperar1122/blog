package com.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.Article;

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
}