package com.example.blog.dto.response;


import lombok.Data;
import java.util.List;

@Data
public class ArticleListResponse {

    private List<ArticleDetailResponse> articles;

    private Long total;

    private Long current;

    private Long size;

    private Long pages;

    public ArticleListResponse() {
        this.articles = List.of();
        this.total = 0L;
        this.current = 1L;
        this.size = 10L;
        this.pages = 0L;
    }
}