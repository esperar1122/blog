package com.example.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.Result;
import com.example.blog.dto.request.ArticleQueryRequest;
import com.example.blog.dto.response.ArticleListResponse;
import com.example.blog.dto.response.ArticleDetailResponse;
import com.example.blog.entity.Article;
import com.example.blog.enums.UserRole;
import com.example.blog.security.RequireRole;
import com.example.blog.security.RequireAdmin;
import com.example.blog.service.ArticleService;
import com.example.blog.util.JwtUtil;
import com.example.blog.dto.CreateArticleRequest;
import com.example.blog.dto.UpdateArticleRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public Result<ArticleListResponse> getArticles(@Valid ArticleQueryRequest request) {
        ArticleListResponse result = articleService.getArticleList(request);
        return Result.success(result);
    }

    @GetMapping("/published")
    public Result<Page<Article>> getPublishedArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword) {

        Page<Article> pageParam = new Page<>(page, size);
        Page<Article> result = articleService.getPublishedArticlesWithPagination(pageParam, categoryId, tagId, keyword);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ArticleDetailResponse> getArticle(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        ArticleDetailResponse result = articleService.getArticleDetail(id, currentUserId);
        return Result.success(result);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @RequireRole(UserRole.USER)
    public Result<Article> createArticle(@Valid @RequestBody CreateArticleRequest request, HttpServletRequest httpRequest) {
        Long currentUserId = getCurrentUserIdOrThrow(httpRequest);

        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setCategoryId(request.getCategoryId());
        article.setStatus(request.getStatus());
        article.setAuthorId(currentUserId);

        Article createdArticle = articleService.createArticle(article, request.getTagIds());
        return Result.success("文章创建成功", createdArticle);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @RequireRole(UserRole.USER)
    public Result<Article> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateArticleRequest request,
            HttpServletRequest httpRequest) {

        Long currentUserId = getCurrentUserId(httpRequest);

        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setCategoryId(request.getCategoryId());

        Article updatedArticle = articleService.updateArticle(id, article, request.getTagIds(), currentUserId);
        return Result.success("文章更新成功", updatedArticle);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @RequireAdmin
    public Result<Void> deleteArticle(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        articleService.deleteArticle(id, currentUserId);
        return Result.success("文章删除成功");
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('USER')")
    @RequireRole(UserRole.USER)
    public Result<Void> publishArticle(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        articleService.publishArticle(id, currentUserId);
        return Result.success("文章发布成功");
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('USER')")
    @RequireRole(UserRole.USER)
    public Result<Void> unpublishArticle(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        articleService.unpublishArticle(id, currentUserId);
        return Result.success("文章取消发布成功");
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("hasRole('USER')")
    @RequireRole(UserRole.USER)
    public Result<Map<String, Object>> likeArticle(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        boolean liked = articleService.toggleLike(id, currentUserId);

        return Result.success(Map.of(
            "liked", liked,
            "message", liked ? "点赞成功" : "取消点赞成功"
        ));
    }

    @PostMapping("/{id}/top")
    public Result<Void> setArticleTop(@PathVariable Long id, @RequestParam boolean isTop, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        articleService.setArticleTop(id, isTop, currentUserId);
        return Result.success(isTop ? "文章置顶成功" : "文章取消置顶成功");
    }

    @GetMapping("/author/{authorId}")
    public Result<List<Article>> getArticlesByAuthor(
            @PathVariable Long authorId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "10") int limit) {

        List<Article> articles = articleService.getArticlesByAuthor(authorId, status, limit);
        return Result.success(articles);
    }

    @GetMapping("/popular")
    public Result<List<Article>> getPopularArticles(@RequestParam(defaultValue = "10") int limit) {
        List<Article> articles = articleService.getPopularArticles(limit);
        return Result.success(articles);
    }

    @GetMapping("/latest")
    public Result<List<Article>> getLatestArticles(@RequestParam(defaultValue = "10") int limit) {
        List<Article> articles = articleService.getLatestArticles(limit);
        return Result.success(articles);
    }

    @GetMapping("/{id}/related")
    public Result<List<Article>> getRelatedArticles(@PathVariable Long id, @RequestParam(defaultValue = "5") int limit) {
        List<Article> articles = articleService.getRelatedArticles(id, limit);
        return Result.success(articles);
    }

    @GetMapping("/search")
    public Result<ArticleListResponse> searchArticles(@Valid ArticleQueryRequest request) {
        ArticleListResponse result = articleService.searchArticles(request);
        return Result.success(result);
    }

    @GetMapping("/my-articles")
    @PreAuthorize("hasRole('USER')")
    public Result<ArticleListResponse> getMyArticles(@Valid ArticleQueryRequest request, HttpServletRequest httpRequest) {
        Long currentUserId = getCurrentUserIdOrThrow(httpRequest);
        request.setAuthorId(currentUserId);
        if (request.getStatus() == null || request.getStatus().equals("PUBLISHED")) {
            request.setStatus("ALL");
        }
        ArticleListResponse result = articleService.getArticleList(request);
        return Result.success(result);
    }

    @GetMapping("/drafts")
    public Result<List<Article>> getDraftArticles(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        List<Article> articles = articleService.getDraftArticles(currentUserId);
        return Result.success(articles);
    }

    @GetMapping("/published/me")
    public Result<List<Article>> getMyPublishedArticles(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        List<Article> articles = articleService.getPublishedArticles(currentUserId);
        return Result.success(articles);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getArticleStats(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);

        int totalArticles = articleService.countArticlesByAuthor(currentUserId, null);
        int publishedArticles = articleService.countArticlesByAuthor(currentUserId, Article.Status.PUBLISHED.getValue());
        int draftArticles = articleService.countArticlesByAuthor(currentUserId, Article.Status.DRAFT.getValue());

        return Result.success(Map.of(
            "totalArticles", totalArticles,
            "publishedArticles", publishedArticles,
            "draftArticles", draftArticles
        ));
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authorization.substring(7);
            return jwtUtil.extractUserId(token);
        } catch (Exception e) {
            return null;
        }
    }

    private Long getCurrentUserIdOrThrow(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        return userId;
    }
}