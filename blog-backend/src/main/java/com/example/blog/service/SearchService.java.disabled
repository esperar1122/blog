package com.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.dto.SearchRequest;
import com.example.blog.dto.SearchResponse;
import com.example.blog.entity.HotKeywords;
import com.example.blog.entity.SearchHistory;
import com.example.blog.entity.SearchStats;

import java.util.List;

/**
 * 搜索服务接口
 */
public interface SearchService {

    /**
     * 全文搜索
     *
     * @param searchRequest 搜索请求
     * @param userId 用户ID（可为null）
     * @return 搜索响应
     */
    SearchResponse searchArticles(SearchRequest searchRequest, Long userId);

    /**
     * 获取搜索建议
     *
     * @param keyword 关键词
     * @param limit 返回数量限制
     * @return 搜索建议列表
     */
    List<String> getSearchSuggestions(String keyword, Integer limit);

    /**
     * 保存搜索历史
     *
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @param resultCount 结果数量
     */
    void saveSearchHistory(Long userId, String keyword, Integer resultCount);

    /**
     * 获取用户搜索历史
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 搜索历史分页数据
     */
    IPage<SearchHistory> getUserSearchHistory(Long userId, Integer page, Integer size);

    /**
     * 清空用户搜索历史
     *
     * @param userId 用户ID
     */
    void clearUserSearchHistory(Long userId);

    /**
     * 获取热门搜索关键词
     *
     * @param limit 返回数量限制
     * @return 热门关键词列表
     */
    List<HotKeywords> getHotKeywords(Integer limit);

    /**
     * 记录搜索统计
     *
     * @param keyword 搜索关键词
     * @param resultCount 结果数量
     */
    void recordSearchStats(String keyword, Integer resultCount);

    /**
     * 获取搜索统计数据
     *
     * @param page 页码
     * @param size 每页大小
     * @return 搜索统计分页数据
     */
    IPage<SearchStats> getSearchStats(Integer page, Integer size);

    /**
     * 获取相关文章推荐
     *
     * @param articleId 文章ID
     * @param limit 返回数量限制
     * @return 相关文章列表
     */
    List<SearchResponse.SearchResultItem> getRelatedArticles(Long articleId, Integer limit);

    /**
     * 更新热门关键词排名
     */
    void updateHotKeywordsRanking();
}