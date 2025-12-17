package com.example.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.Result;
import com.example.blog.dto.request.ArticleQueryRequest;
import com.example.blog.dto.request.PublishArticleRequest;
import com.example.blog.dto.request.SchedulePublishRequest;
import com.example.blog.dto.response.ArticleListResponse;
import com.example.blog.dto.response.ArticleDetailResponse;
import com.example.blog.dto.response.ArticleVersionResponse;
import com.example.blog.dto.response.ArticleOperationLogResponse;
import com.example.blog.dto.response.ArticleStatusManagementResponse;
import com.example.blog.entity.Article;
import com.example.blog.entity.ArticleVersion;
import com.example.blog.entity.ArticleOperationLog;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // 文章生命周期管理API

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('USER')")
    public Result<ArticleStatusManagementResponse> publishArticle(@PathVariable Long id) {
        boolean success = articleService.publishArticle(id);
        ArticleStatusManagementResponse response = new ArticleStatusManagementResponse();
        response.setArticleId(id);
        response.setStatus("PUBLISHED");
        response.setStatusDescription("已发布");
        response.setOperationResult(success ? "success" : "failed");
        response.setMessage(success ? "文章发布成功" : "文章发布失败");
        response.setOperationTime(LocalDateTime.now());

        return success ? Result.success(response) : Result.error("文章发布失败");
    }

    @PostMapping("/{id}/unpublish")
    @PreAuthorize("hasRole('USER')")
    public Result<ArticleStatusManagementResponse> unpublishArticle(@PathVariable Long id) {
        boolean success = articleService.unpublishArticle(id);
        ArticleStatusManagementResponse response = new ArticleStatusManagementResponse();
        response.setArticleId(id);
        response.setStatus("DRAFT");
        response.setStatusDescription("草稿");
        response.setOperationResult(success ? "success" : "failed");
        response.setMessage(success ? "文章下线成功" : "文章下线失败");
        response.setOperationTime(LocalDateTime.now());

        return success ? Result.success(response) : Result.error("文章下线失败");
    }

    @PostMapping("/{id}/pin")
    @PreAuthorize("hasRole('USER')")
    public Result<ArticleStatusManagementResponse> pinArticle(@PathVariable Long id) {
        boolean success = articleService.pinArticle(id);
        ArticleStatusManagementResponse response = new ArticleStatusManagementResponse();
        response.setArticleId(id);
        response.setIsTop(true);
        response.setOperationResult(success ? "success" : "failed");
        response.setMessage(success ? "文章置顶成功" : "文章置顶失败");
        response.setOperationTime(LocalDateTime.now());

        return success ? Result.success(response) : Result.error("文章置顶失败");
    }

    @PostMapping("/{id}/unpin")
    @PreAuthorize("hasRole('USER')")
    public Result<ArticleStatusManagementResponse> unpinArticle(@PathVariable Long id) {
        boolean success = articleService.unpinArticle(id);
        ArticleStatusManagementResponse response = new ArticleStatusManagementResponse();
        response.setArticleId(id);
        response.setIsTop(false);
        response.setOperationResult(success ? "success" : "failed");
        response.setMessage(success ? "取消置顶成功" : "取消置顶失败");
        response.setOperationTime(LocalDateTime.now());

        return success ? Result.success(response) : Result.error("取消置顶失败");
    }

    @PostMapping("/{id}/schedule-publish")
    @PreAuthorize("hasRole('USER')")
    public Result<ArticleStatusManagementResponse> schedulePublishArticle(
            @PathVariable Long id,
            @Valid @RequestBody SchedulePublishRequest request) {
        boolean success = articleService.schedulePublish(id, request.getScheduledPublishTime());
        ArticleStatusManagementResponse response = new ArticleStatusManagementResponse();
        response.setArticleId(id);
        response.setScheduledPublishTime(request.getScheduledPublishTime());
        response.setOperationResult(success ? "success" : "failed");
        response.setMessage(success ? "定时发布设置成功" : "定时发布设置失败");
        response.setOperationTime(LocalDateTime.now());

        return success ? Result.success(response) : Result.error("定时发布设置失败");
    }

    @DeleteMapping("/{id}/soft-delete")
    @PreAuthorize("hasRole('USER')")
    public Result<ArticleStatusManagementResponse> softDeleteArticle(@PathVariable Long id) {
        boolean success = articleService.softDeleteArticle(id);
        ArticleStatusManagementResponse response = new ArticleStatusManagementResponse();
        response.setArticleId(id);
        response.setStatus("DELETED");
        response.setStatusDescription("已删除");
        response.setDeletedAt(LocalDateTime.now());
        response.setOperationResult(success ? "success" : "failed");
        response.setMessage(success ? "文章删除成功" : "文章删除失败");
        response.setOperationTime(LocalDateTime.now());

        return success ? Result.success(response) : Result.error("文章删除失败");
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('USER')")
    public Result<ArticleStatusManagementResponse> restoreArticle(@PathVariable Long id) {
        boolean success = articleService.restoreArticle(id);
        ArticleStatusManagementResponse response = new ArticleStatusManagementResponse();
        response.setArticleId(id);
        response.setStatus("DRAFT");
        response.setStatusDescription("草稿");
        response.setOperationResult(success ? "success" : "failed");
        response.setMessage(success ? "文章恢复成功" : "文章恢复失败");
        response.setOperationTime(LocalDateTime.now());

        return success ? Result.success(response) : Result.error("文章恢复失败");
    }

    @GetMapping("/{id}/versions")
    @PreAuthorize("hasRole('USER')")
    public Result<List<ArticleVersionResponse>> getArticleVersions(@PathVariable Long id) {
        List<ArticleVersion> versions = articleService.getArticleVersions(id);
        List<ArticleVersionResponse> response = versions.stream()
                .map(this::convertToArticleVersionResponse)
                .collect(Collectors.toList());

        return Result.success(response);
    }

    @GetMapping("/{id}/operation-logs")
    @PreAuthorize("hasRole('USER')")
    public Result<List<ArticleOperationLogResponse>> getArticleOperationLogs(@PathVariable Long id) {
        List<ArticleOperationLog> logs = articleService.getArticleOperationLogs(id);
        List<ArticleOperationLogResponse> response = logs.stream()
                .map(this::convertToArticleOperationLogResponse)
                .collect(Collectors.toList());

        return Result.success(response);
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('USER')")
    public Result<List<Article>> getDeletedArticles(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        List<Article> articles = articleService.getDeletedArticles(currentUserId);
        return Result.success(articles);
    }

    @GetMapping("/scheduled")
    @PreAuthorize("hasRole('USER')")
    public Result<List<Article>> getScheduledArticles(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        List<Article> articles = articleService.getScheduledArticles(currentUserId);
        return Result.success(articles);
    }

    @GetMapping("/pinned")
    @PreAuthorize("hasRole('USER')")
    public Result<List<Article>> getPinnedArticles(HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        List<Article> articles = articleService.getPinnedArticles(currentUserId);
        return Result.success(articles);
    }

    // 辅助方法

    private ArticleVersionResponse convertToArticleVersionResponse(ArticleVersion version) {
        ArticleVersionResponse response = new ArticleVersionResponse();
        response.setId(version.getId());
        response.setArticleId(version.getArticleId());
        response.setVersionNumber(version.getVersionNumber());
        response.setTitle(version.getTitle());
        response.setContent(version.getContent());
        response.setSummary(version.getSummary());
        response.setCoverImage(version.getCoverImage());
        response.setChangeReason(version.getChangeReason());
        response.setEditorId(version.getEditorId());
        response.setEditorName(version.getEditorName());
        response.setEditorAvatar(version.getEditorAvatar());
        response.setCreateTime(version.getCreateTime());
        return response;
    }

    private ArticleOperationLogResponse convertToArticleOperationLogResponse(ArticleOperationLog log) {
        ArticleOperationLogResponse response = new ArticleOperationLogResponse();
        response.setId(log.getId());
        response.setArticleId(log.getArticleId());
        response.setOperationType(log.getOperationType());
        response.setOperationTypeDescription(ArticleOperationLog.OperationType.fromValue(log.getOperationType()).getDescription());
        response.setOldStatus(log.getOldStatus());
        response.setNewStatus(log.getNewStatus());
        response.setOperatorId(log.getOperatorId());
        response.setOperatorName(log.getOperatorName());
        response.setOperatorAvatar(log.getOperatorAvatar());
        response.setOperatorIp(log.getOperatorIp());
        response.setOperationDetail(log.getOperationDetail());
        response.setCreateTime(log.getCreateTime());
        return response;
    }
}