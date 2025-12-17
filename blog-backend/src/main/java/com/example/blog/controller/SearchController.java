package com.example.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.common.Result;
import com.example.blog.dto.SearchRequest;
import com.example.blog.dto.SearchResponse;
import com.example.blog.entity.HotKeywords;
import com.example.blog.entity.SearchHistory;
import com.example.blog.entity.SearchStats;
import com.example.blog.service.SearchService;
import com.example.blog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 搜索控制器
 */
@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final JwtUtil jwtUtil;

    /**
     * 全文搜索
     */
    @GetMapping
    public Result<SearchResponse> searchArticles(
            @RequestParam String q,
            @RequestParam(required = false) List<String> fields,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setQ(q);
        searchRequest.setFields(fields);
        searchRequest.setCategoryId(categoryId);
        searchRequest.setTagIds(tagIds);
        searchRequest.setStartDate(startDate);
        searchRequest.setEndDate(endDate);
        searchRequest.setSortBy(sortBy);
        searchRequest.setPage(page);
        searchRequest.setSize(size);

        // 获取当前用户ID（如果已登录）
        Long userId = getCurrentUserId(request);

        SearchResponse response = searchService.searchArticles(searchRequest, userId);
        return Result.success(response);
    }

    /**
     * 获取搜索建议
     */
    @GetMapping("/suggestions")
    public Result<List<String>> getSearchSuggestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") Integer limit) {

        List<String> suggestions = searchService.getSearchSuggestions(keyword, limit);
        return Result.success(suggestions);
    }

    /**
     * 获取用户搜索历史
     */
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public Result<IPage<SearchHistory>> getUserSearchHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request) {

        Long userId = getCurrentUserId(request);
        IPage<SearchHistory> history = searchService.getUserSearchHistory(userId, page, size);
        return Result.success(history);
    }

    /**
     * 保存搜索历史
     */
    @PostMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> saveSearchHistory(
            @Valid @RequestBody SaveHistoryRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getCurrentUserId(httpRequest);
        searchService.saveSearchHistory(userId, request.getKeyword(), request.getResultCount());
        return Result.success();
    }

    /**
     * 清空用户搜索历史
     */
    @DeleteMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> clearUserSearchHistory(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        searchService.clearUserSearchHistory(userId);
        return Result.success();
    }

    /**
     * 获取热门搜索关键词
     */
    @GetMapping("/hot-keywords")
    public Result<List<HotKeywords>> getHotKeywords(
            @RequestParam(defaultValue = "20") Integer limit) {

        List<HotKeywords> hotKeywords = searchService.getHotKeywords(limit);
        return Result.success(hotKeywords);
    }

    /**
     * 获取搜索统计数据
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<SearchStats>> getSearchStats(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        IPage<SearchStats> stats = searchService.getSearchStats(page, size);
        return Result.success(stats);
    }

    /**
     * 获取相关文章推荐
     */
    @GetMapping("/related/{articleId}")
    public Result<List<SearchResponse.SearchResultItem>> getRelatedArticles(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "5") Integer limit) {

        List<SearchResponse.SearchResultItem> relatedArticles =
            searchService.getRelatedArticles(articleId, limit);
        return Result.success(relatedArticles);
    }

    /**
     * 更新热门关键词排名（管理员专用）
     */
    @PostMapping("/update-hot-keywords")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateHotKeywordsRanking() {
        searchService.updateHotKeywordsRanking();
        return Result.success();
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                return jwtUtil.getUserIdFromToken(token);
            } catch (Exception e) {
                log.debug("Invalid token", e);
            }
        }
        return null;
    }

    /**
     * 保存搜索历史请求DTO
     */
    public static class SaveHistoryRequest {
        private String keyword;
        private Integer resultCount;

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public Integer getResultCount() {
            return resultCount;
        }

        public void setResultCount(Integer resultCount) {
            this.resultCount = resultCount;
        }
    }
}