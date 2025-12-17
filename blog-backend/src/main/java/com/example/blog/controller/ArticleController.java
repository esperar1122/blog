package com.example.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.Result;
import com.example.blog.entity.Article;
import com.example.blog.service.ArticleService;
import com.example.blog.util.JwtUtil;
import com.example.blog.dto.CreateArticleRequest;
import com.example.blog.dto.UpdateArticleRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public Result<Page<Article>> getArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {

        Page<Article> pageParam = new Page<>(page, size);
        Page<Article> result = articleService.getArticlesWithPagination(pageParam, categoryId, tagId, keyword, status);
        return Result.success(result);
    }

    @GetMapping("/published")
    public Result<Page<Article>> getPublishedArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "OR") String tagLogic) {

        Page<Article> pageParam = new Page<>(page, size);

        // 如果提供了多个标签ID，使用多标签筛选逻辑
        if (tagIds != null && !tagIds.isEmpty()) {
            // 暂时使用第一个标签ID，完整的多标签逻辑需要在Service层实现
            Page<Article> result = articleService.getPublishedArticlesWithPagination(pageParam, categoryId, tagIds.get(0), keyword);
            return Result.success(result);
        } else {
            Page<Article> result = articleService.getPublishedArticlesWithPagination(pageParam, categoryId, tagId, keyword);
            return Result.success(result);
        }
    }

    @GetMapping("/{id}")
    public Result<Article> getArticle(@PathVariable Long id, HttpServletRequest request) {
        Article article = articleService.getArticleWithDetails(id);
        articleService.incrementViewCount(id);
        return Result.success(article);
    }

    @PostMapping
    public Result<Article> createArticle(@Valid @RequestBody CreateArticleRequest request, HttpServletRequest httpRequest) {
        Long currentUserId = getCurrentUserId(httpRequest);

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
    public Result<Void> deleteArticle(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        articleService.deleteArticle(id, currentUserId);
        return Result.success("文章删除成功");
    }

    @PostMapping("/{id}/publish")
    public Result<Void> publishArticle(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        articleService.publishArticle(id, currentUserId);
        return Result.success("文章发布成功");
    }

    @PostMapping("/{id}/unpublish")
    public Result<Void> unpublishArticle(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        articleService.unpublishArticle(id, currentUserId);
        return Result.success("文章取消发布成功");
    }

    @PostMapping("/{id}/like")
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
    public Result<List<Article>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "20") int limit) {

        List<Article> articles = articleService.searchArticles(keyword, limit);
        return Result.success(articles);
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
            throw new RuntimeException("未登录");
        }
        String token = authorization.substring(7);
        return jwtUtil.extractUserId(token);
    }
}