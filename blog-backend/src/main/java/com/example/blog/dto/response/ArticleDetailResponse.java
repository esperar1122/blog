package com.example.blog.dto.response;

import com.example.blog.entity.Tag;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleDetailResponse {

    private Long id;

    private String title;

    private String content;

    private String summary;

    private Long authorId;

    private String authorName;

    private String authorAvatar;

    private Long categoryId;

    private String categoryName;

    private String status;

    private Long viewCount;

    private Long likeCount;

    private Long commentCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<Tag> tags;

    public ArticleDetailResponse() {
        this.viewCount = 0L;
        this.likeCount = 0L;
        this.commentCount = 0L;
    }
}