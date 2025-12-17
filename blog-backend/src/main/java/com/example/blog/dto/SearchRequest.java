package com.example.blog.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索请求DTO
 */
@Data
public class SearchRequest {

    /**
     * 搜索关键词
     */
    private String q;

    /**
     * 搜索字段：title, content, author, tags
     */
    private List<String> fields;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 标签ID列表
     */
    private List<Long> tagIds;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 排序方式：relevance, createTime, viewCount
     */
    private String sortBy = "relevance";

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 验证请求参数
     */
    public boolean isValid() {
        if (q == null || q.trim().isEmpty()) {
            return false;
        }

        if (q.length() < 2 || q.length() > 100) {
            return false;
        }

        if (page != null && page < 1) {
            return false;
        }

        if (size != null && (size < 1 || size > 100)) {
            return false;
        }

        return true;
    }
}