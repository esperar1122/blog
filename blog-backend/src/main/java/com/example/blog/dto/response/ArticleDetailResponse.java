package com.example.blog.dto.response;

import com.example.blog.entity.Article;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ArticleDetailResponse {

    private Article article;

    private UserSummary author;

    private CategorySummary category;

    private List<TagSummary> tags;

    private boolean isLiked;

    private boolean canEdit;

    @Data
    public static class UserSummary {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
        private String bio;
    }

    @Data
    public static class CategorySummary {
        private Long id;
        private String name;
        private String description;
    }

    @Data
    public static class TagSummary {
        private Long id;
        private String name;
        private String color;
    }
}