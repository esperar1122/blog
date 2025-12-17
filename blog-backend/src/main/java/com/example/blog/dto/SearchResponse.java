package com.example.blog.dto;

import com.example.blog.entity.Article;
import lombok.Data;

import java.util.List;

/**
 * 搜索响应DTO
 */
@Data
public class SearchResponse {

    /**
     * 搜索结果列表
     */
    private List<SearchResultItem> results;

    /**
     * 分页信息
     */
    private PaginationInfo pagination;

    /**
     * 搜索元信息
     */
    private SearchMeta searchMeta;

    @Data
    public static class SearchResultItem {
        /**
         * 文章ID
         */
        private Long id;

        /**
         * 标题
         */
        private String title;

        /**
         * 摘要
         */
        private String summary;

        /**
         * 作者信息
         */
        private AuthorInfo author;

        /**
         * 分类信息
         */
        private CategoryInfo category;

        /**
         * 标签列表
         */
        private List<TagInfo> tags;

        /**
         * 浏览量
         */
        private Integer viewCount;

        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 高亮信息
         */
        private HighlightInfo highlights;
    }

    @Data
    public static class AuthorInfo {
        private Long id;
        private String nickname;
        private String avatar;
    }

    @Data
    public static class CategoryInfo {
        private Long id;
        private String name;
    }

    @Data
    public static class TagInfo {
        private Long id;
        private String name;
        private String color;
    }

    @Data
    public static class HighlightInfo {
        private List<String> title;
        private List<String> content;
    }

    @Data
    public static class PaginationInfo {
        private Integer page;
        private Integer size;
        private Long total;
        private Integer totalPages;
    }

    @Data
    public static class SearchMeta {
        private String keyword;
        private Double searchTime;
        private Integer resultCount;
    }

    /**
     * 从Article创建SearchResultItem
     */
    public static SearchResultItem fromArticle(Article article) {
        SearchResultItem item = new SearchResultItem();
        item.setId(article.getId());
        item.setTitle(article.getTitle());
        item.setSummary(article.getSummary());
        item.setViewCount(article.getViewCount());
        item.setCreateTime(article.getCreateTime().toString());

        // 设置作者信息
        AuthorInfo author = new AuthorInfo();
        author.setId(article.getAuthorId());
        // 需要通过查询获取作者详细信息
        item.setAuthor(author);

        // 设置分类信息
        if (article.getCategoryId() != null) {
            CategoryInfo category = new CategoryInfo();
            category.setId(article.getCategoryId());
            // 需要通过查询获取分类名称
            item.setCategory(category);
        }

        return item;
    }
}