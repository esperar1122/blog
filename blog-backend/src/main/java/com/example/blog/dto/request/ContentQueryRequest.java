package com.example.blog.dto.request;

import lombok.Data;
import com.example.blog.entity.Article;

@Data
public class ContentQueryRequest {

    private Integer page = 0;

    private Integer size = 20;

    private String status;

    private String keyword;

    private Long categoryId;

    private String startTime;

    private String endTime;

    private Long articleId;

    private String contentType;

    public boolean isArticleQuery() {
        return "article".equals(contentType);
    }

    public boolean isCommentQuery() {
        return "comment".equals(contentType);
    }
}