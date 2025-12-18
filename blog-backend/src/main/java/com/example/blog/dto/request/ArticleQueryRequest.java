package com.example.blog.dto.request;

import lombok.Data;

@Data
public class ArticleQueryRequest {

    private Integer page = 1;

    private Integer size = 10;

    private String sort = "createTime";

    private Long categoryId;

    private Long tagId;

    private String keyword;

    private String status;

    private Long authorId;

    private Boolean isTop;
}