package com.example.blog.service;

import com.example.blog.dto.request.ArticleQueryRequest;
import com.example.blog.dto.response.ArticleListResponse;
import com.example.blog.dto.response.ArticleDetailResponse;
import com.example.blog.dto.CreateArticleRequest;
import com.example.blog.dto.UpdateArticleRequest;
import com.example.blog.entity.Article;

import java.util.Map;

public interface ArticleService {

    Article getArticleById(Long id);

    ArticleListResponse getArticleList(ArticleQueryRequest request);

    ArticleDetailResponse getArticleDetail(Long id);

    Article createArticle(CreateArticleRequest request);

    Article updateArticle(Long id, UpdateArticleRequest request);

    boolean deleteArticle(Long id);

    boolean publishArticle(Long id);

    boolean unpublishArticle(Long id);

    boolean likeArticle(Long articleId, Long userId);

    Map<String, Object> getArticleStatistics();
}