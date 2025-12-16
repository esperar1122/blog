package com.example.blog.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class ArticleListResponse {

    private List<ArticleSummary> content;

    private int totalPages;

    private long totalElements;

    private int size;

    private int number;

    private boolean first;

    private boolean last;

    private boolean empty;

    @Data
    public static class ArticleSummary {
        private Long id;
        private String title;
        private String summary;
        private String coverImage;
        private String status;
        private Integer viewCount;
        private Integer likeCount;
        private Integer commentCount;
        private Boolean isTop;
        private Long authorId;
        private String authorName;
        private String authorAvatar;
        private Long categoryId;
        private String categoryName;
        private List<String> tags;
        private java.time.LocalDateTime createTime;
        private java.time.LocalDateTime updateTime;
        private java.time.LocalDateTime publishTime;
    }
}