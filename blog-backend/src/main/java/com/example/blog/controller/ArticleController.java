package com.example.blog.controller;

import com.example.blog.common.Result;
import com.example.blog.dto.request.ArticleQueryRequest;
import com.example.blog.dto.response.ArticleListResponse;
import com.example.blog.dto.response.ArticleDetailResponse;
import com.example.blog.entity.Article;
import com.example.blog.service.ArticleService;
import com.example.blog.dto.CreateArticleRequest;
import com.example.blog.dto.UpdateArticleRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public Result<ArticleListResponse> getArticles(@Valid ArticleQueryRequest request) {
        ArticleListResponse result = articleService.getArticleList(request);
        return Result.success(result);
    }

    @GetMapping("/published")
    public Result<ArticleListResponse> getPublishedArticles(@Valid ArticleQueryRequest request) {
        request.setStatus("PUBLISHED");
        ArticleListResponse result = articleService.getArticleList(request);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<ArticleDetailResponse> getArticle(@PathVariable Long id) {
        ArticleDetailResponse result = articleService.getArticleDetail(id);
        return Result.success(result);
    }

    @PostMapping
    public Result<Article> createArticle(@Valid @RequestBody CreateArticleRequest request,
                                       HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        log.info("创建文章请求 - title: {}, ip: {}", request.getTitle(), ipAddress);

        try {
            Article article = articleService.createArticle(request);
            return Result.success(article);
        } catch (Exception e) {
            log.error("创建文章失败: {}", e.getMessage());
            return Result.error("创建文章失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Article> updateArticle(@PathVariable Long id,
                                       @Valid @RequestBody UpdateArticleRequest request,
                                       HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        log.info("更新文章请求 - id: {}, title: {}, ip: {}", id, request.getTitle(), ipAddress);

        try {
            Article article = articleService.updateArticle(id, request);
            return Result.success(article);
        } catch (Exception e) {
            log.error("更新文章失败: {}", e.getMessage());
            return Result.error("更新文章失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteArticle(@PathVariable Long id, HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        log.info("删除文章请求 - id: {}, ip: {}", id, ipAddress);

        try {
            articleService.deleteArticle(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除文章失败: {}", e.getMessage());
            return Result.error("删除文章失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/publish")
    public Result<String> publishArticle(@PathVariable Long id, HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        log.info("发布文章请求 - id: {}, ip: {}", id, ipAddress);

        try {
            articleService.publishArticle(id);
            return Result.success("发布成功");
        } catch (Exception e) {
            log.error("发布文章失败: {}", e.getMessage());
            return Result.error("发布文章失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/unpublish")
    public Result<String> unpublishArticle(@PathVariable Long id, HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        log.info("取消发布文章请求 - id: {}, ip: {}", id, ipAddress);

        try {
            articleService.unpublishArticle(id);
            return Result.success("取消发布成功");
        } catch (Exception e) {
            log.error("取消发布文章失败: {}", e.getMessage());
            return Result.error("取消发布文章失败: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public Result<ArticleListResponse> searchArticles(@RequestParam String keyword,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        ArticleQueryRequest request = new ArticleQueryRequest();
        request.setKeyword(keyword);
        request.setPage(page);
        request.setSize(size);

        ArticleListResponse result = articleService.getArticleList(request);
        return Result.success(result);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}