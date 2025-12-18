package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.SearchRequest;
import com.example.blog.dto.SearchResponse;
import com.example.blog.entity.*;
import com.example.blog.mapper.*;
import com.example.blog.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 搜索服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ArticleMapper articleMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final SearchStatsMapper searchStatsMapper;
    private final HotKeywordsMapper hotKeywordsMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    // HTML标签正则表达式
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    @Override
    @Transactional
    public SearchResponse searchArticles(SearchRequest searchRequest, Long userId) {
        long startTime = System.currentTimeMillis();

        // 验证请求参数
        if (!searchRequest.isValid()) {
            throw new IllegalArgumentException("Invalid search request");
        }

        // 构建搜索查询
        String keyword = searchRequest.getQ().trim();

        // 执行搜索
        Page<Article> page = new Page<>(searchRequest.getPage(), searchRequest.getSize());
        IPage<Article> articlePage = performSearch(page, searchRequest);

        // 构建响应
        SearchResponse response = new SearchResponse();
        response.setResults(convertToSearchResultItems(articlePage.getRecords(), keyword));

        // 设置分页信息
        SearchResponse.PaginationInfo pagination = new SearchResponse.PaginationInfo();
        pagination.setPage(searchRequest.getPage());
        pagination.setSize(searchRequest.getSize());
        pagination.setTotal(articlePage.getTotal());
        pagination.setTotalPages((int) Math.ceil((double) articlePage.getTotal() / searchRequest.getSize()));
        response.setPagination(pagination);

        // 设置搜索元信息
        SearchResponse.SearchMeta searchMeta = new SearchResponse.SearchMeta();
        searchMeta.setKeyword(keyword);
        searchMeta.setSearchTime((System.currentTimeMillis() - startTime) / 1000.0);
        searchMeta.setResultCount((int) articlePage.getTotal());
        response.setSearchMeta(searchMeta);

        // 保存搜索历史和统计（异步）
        if (userId != null) {
            saveSearchHistory(userId, keyword, (int) articlePage.getTotal());
        }
        recordSearchStats(keyword, (int) articlePage.getTotal());

        return response;
    }

    /**
     * 执行实际搜索
     */
    private IPage<Article> performSearch(Page<Article> page, SearchRequest request) {
        String keyword = request.getQ();
        List<String> fields = request.getFields();

        // 默认搜索所有字段
        if (fields == null || fields.isEmpty()) {
            fields = Arrays.asList("title", "content");
        }

        // 构建查询条件
        QueryWrapper<Article> queryWrapper = new QueryWrapper<>();

        // 只搜索已发布的文章
        queryWrapper.eq("status", "published");

        // 全文搜索条件
        if (fields.contains("title") && fields.contains("content")) {
            // 使用全文索引搜索标题和内容
            queryWrapper.and(wrapper ->
                wrapper.apply("MATCH(title, content) AGAINST({0} IN NATURAL LANGUAGE MODE)", keyword)
            );
        } else {
            // 单独搜索字段
            if (fields.contains("title")) {
                queryWrapper.like("title", keyword);
            }
            if (fields.contains("content")) {
                queryWrapper.or().like("content", keyword);
            }
        }

        // 按分类筛选
        if (request.getCategoryId() != null) {
            queryWrapper.eq("category_id", request.getCategoryId());
        }

        // 按日期范围筛选
        if (StringUtils.hasText(request.getStartDate())) {
            queryWrapper.ge("create_time", request.getStartDate() + " 00:00:00");
        }
        if (StringUtils.hasText(request.getEndDate())) {
            queryWrapper.le("create_time", request.getEndDate() + " 23:59:59");
        }

        // 排序
        switch (request.getSortBy()) {
            case "createTime":
                queryWrapper.orderByDesc("create_time");
                break;
            case "viewCount":
                queryWrapper.orderByDesc("view_count");
                break;
            case "relevance":
            default:
                // 相关性排序（使用全文搜索的相关性分数）
                if (fields.contains("title") && fields.contains("content")) {
                    queryWrapper.last("ORDER BY MATCH(title, content) AGAINST('" + keyword + "' IN NATURAL LANGUAGE MODE) DESC, create_time DESC");
                } else {
                    queryWrapper.orderByDesc("create_time");
                }
                break;
        }

        return articleMapper.selectPage(page, queryWrapper);
    }

    /**
     * 转换为搜索结果项
     */
    private List<SearchResponse.SearchResultItem> convertToSearchResultItems(List<Article> articles, String keyword) {
        return articles.stream().map(article -> {
            SearchResponse.SearchResultItem item = SearchResponse.fromArticle(article);

            // 设置作者信息
            User author = userMapper.selectById(article.getAuthorId());
            if (author != null) {
                SearchResponse.AuthorInfo authorInfo = new SearchResponse.AuthorInfo();
                authorInfo.setId(author.getId());
                authorInfo.setNickname(author.getNickname());
                authorInfo.setAvatar(author.getAvatar());
                item.setAuthor(authorInfo);
            }

            // 设置分类信息
            if (article.getCategoryId() != null) {
                Category category = categoryMapper.selectById(article.getCategoryId());
                if (category != null) {
                    SearchResponse.CategoryInfo categoryInfo = new SearchResponse.CategoryInfo();
                    categoryInfo.setId(category.getId());
                    categoryInfo.setName(category.getName());
                    item.setCategory(categoryInfo);
                }
            }

            // 设置标签信息
            List<Long> tagIds = articleTagMapper.selectTagIdsByArticleId(article.getId());
            if (!tagIds.isEmpty()) {
                List<Tag> tags = tagMapper.selectBatchIds(tagIds);
                List<SearchResponse.TagInfo> tagInfos = tags.stream().map(tag -> {
                    SearchResponse.TagInfo tagInfo = new SearchResponse.TagInfo();
                    tagInfo.setId(tag.getId());
                    tagInfo.setName(tag.getName());
                    tagInfo.setColor(tag.getColor());
                    return tagInfo;
                }).collect(Collectors.toList());
                item.setTags(tagInfos);
            }

            // 设置高亮信息
            SearchResponse.HighlightInfo highlights = new SearchResponse.HighlightInfo();
            highlights.setTitle(highlightText(article.getTitle(), keyword));
            highlights.setContent(highlightText(clearHtmlTags(article.getContent()), keyword));
            item.setHighlights(highlights);

            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 高亮文本中的关键词
     */
    private List<String> highlightText(String text, String keyword) {
        if (!StringUtils.hasText(text) || !StringUtils.hasText(keyword)) {
            return Collections.emptyList();
        }

        String highlighted = text.replaceAll(
            "(?i)(" + Pattern.quote(keyword) + ")",
            "<mark>$1</mark>"
        );

        // 截取前200字符
        if (highlighted.length() > 200) {
            int keywordIndex = highlighted.toLowerCase().indexOf(keyword.toLowerCase());
            if (keywordIndex > 100) {
                highlighted = "..." + highlighted.substring(keywordIndex - 50);
            }
            highlighted = highlighted.substring(0, 200) + "...";
        }

        return Collections.singletonList(highlighted);
    }

    /**
     * 清除HTML标签
     */
    private String clearHtmlTags(String html) {
        if (!StringUtils.hasText(html)) {
            return "";
        }
        return HTML_TAG_PATTERN.matcher(html).replaceAll("").trim();
    }

    @Override
    public List<String> getSearchSuggestions(String keyword, Integer limit) {
        if (!StringUtils.hasText(keyword) || keyword.length() < 2) {
            return Collections.emptyList();
        }

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // 从搜索统计中获取热门关键词建议
        QueryWrapper<SearchStats> wrapper = new QueryWrapper<>();
        wrapper.like("keyword", keyword)
               .orderByDesc("search_count")
               .last("LIMIT " + limit);

        List<SearchStats> stats = searchStatsMapper.selectList(wrapper);
        return stats.stream()
                   .map(SearchStats::getKeyword)
                   .collect(Collectors.toList());
    }

    @Override
    public void saveSearchHistory(Long userId, String keyword, Integer resultCount) {
        if (userId == null || !StringUtils.hasText(keyword)) {
            return;
        }

        SearchHistory history = new SearchHistory();
        history.setUserId(userId);
        history.setKeyword(keyword);
        history.setResultCount(resultCount != null ? resultCount : 0);

        searchHistoryMapper.insert(history);

        // 限制用户搜索历史记录数量（最多保留100条）
        List<SearchHistory> histories = searchHistoryMapper.selectList(
            new QueryWrapper<SearchHistory>()
                .eq("user_id", userId)
                .orderByDesc("search_time")
                .last("LIMIT 101")
        );

        if (histories.size() > 100) {
            SearchHistory oldest = histories.get(histories.size() - 1);
            searchHistoryMapper.deleteById(oldest.getId());
        }
    }

    @Override
    public IPage<SearchHistory> getUserSearchHistory(Long userId, Integer page, Integer size) {
        if (userId == null) {
            return new Page<>();
        }

        Page<SearchHistory> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 20);
        return searchHistoryMapper.selectByUserId(pageParam, userId);
    }

    @Override
    @Transactional
    public void clearUserSearchHistory(Long userId) {
        if (userId != null) {
            searchHistoryMapper.deleteByUserId(userId);
        }
    }

    @Override
    public List<HotKeywords> getHotKeywords(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 20;
        }

        return hotKeywordsMapper.selectTopKeywords(limit);
    }

    @Override
    @Transactional
    public void recordSearchStats(String keyword, Integer resultCount) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }

        // 检查关键词是否已存在
        QueryWrapper<SearchStats> wrapper = new QueryWrapper<>();
        wrapper.eq("keyword", keyword);
        SearchStats existingStats = searchStatsMapper.selectOne(wrapper);

        if (existingStats != null) {
            // 更新现有统计
            searchStatsMapper.incrementSearchCount(keyword, resultCount != null ? resultCount : 0);
        } else {
            // 创建新统计记录
            SearchStats stats = new SearchStats();
            stats.setKeyword(keyword);
            stats.setSearchCount(1);
            stats.setAvgResultCount(resultCount != null ? resultCount : 0.0);
            searchStatsMapper.insert(stats);
        }
    }

    @Override
    public IPage<SearchStats> getSearchStats(Integer page, Integer size) {
        Page<SearchStats> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 20);
        return searchStatsMapper.selectPage(pageParam, new QueryWrapper<SearchStats>().orderByDesc("search_count"));
    }

    @Override
    public List<SearchResponse.SearchResultItem> getRelatedArticles(Long articleId, Integer limit) {
        if (articleId == null) {
            return Collections.emptyList();
        }

        if (limit == null || limit <= 0) {
            limit = 5;
        }

        // 获取当前文章
        Article currentArticle = articleMapper.selectById(articleId);
        if (currentArticle == null) {
            return Collections.emptyList();
        }

        // 获取当前文章的标签
        List<Long> currentTagIds = articleTagMapper.selectTagIdsByArticleId(articleId);

        // 基于标签推荐相关文章
        if (!currentTagIds.isEmpty()) {
            QueryWrapper<Article> wrapper = new QueryWrapper<>();
            wrapper.eq("status", "published")
                   .ne("id", articleId);

            // 如果有分类，优先推荐同分类文章
            if (currentArticle.getCategoryId() != null) {
                wrapper.and(w -> w.eq("category_id", currentArticle.getCategoryId())
                                 .or()
                                 .in("id", articleTagMapper.selectArticleIdsByTagIds(currentTagIds)));
            } else {
                wrapper.in("id", articleTagMapper.selectArticleIdsByTagIds(currentTagIds));
            }

            wrapper.orderByDesc("create_time")
                   .last("LIMIT " + limit);

            List<Article> relatedArticles = articleMapper.selectList(wrapper);
            return convertToSearchResultItems(relatedArticles, null);
        }

        return Collections.emptyList();
    }

    @Override
    @Transactional
    public void updateHotKeywordsRanking() {
        // 重置所有排名
        hotKeywordsMapper.resetAllPositions();

        // 获取搜索统计前20名
        List<SearchStats> topStats = searchStatsMapper.selectTopSearchKeywords(20);

        List<HotKeywords> hotKeywords = new ArrayList<>();
        for (int i = 0; i < topStats.size(); i++) {
            SearchStats stat = topStats.get(i);

            // 检查是否已存在
            HotKeywords existing = hotKeywordsMapper.selectByKeyword(stat.getKeyword());
            if (existing != null) {
                existing.setSearchCount(stat.getSearchCount());
                existing.setPosition(i + 1);
                hotKeywords.add(existing);
            } else {
                HotKeywords hot = new HotKeywords();
                hot.setKeyword(stat.getKeyword());
                hot.setSearchCount(stat.getSearchCount());
                hot.setPosition(i + 1);
                hotKeywords.add(hot);
            }
        }

        // 批量更新
        for (HotKeywords hot : hotKeywords) {
            if (hot.getId() != null) {
                hotKeywordsMapper.updateById(hot);
            } else {
                hotKeywordsMapper.insert(hot);
            }
        }
    }
}