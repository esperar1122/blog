package com.example.blog.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponse {

    private Long id;
    private String content;
    private Long articleId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long parentId;
    private Integer level;
    private Integer likeCount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<CommentResponse> replies;

    public CommentResponse() {
    }

    public CommentResponse(Long id, String content, Long articleId, Long userId, String userName, String userAvatar) {
        this.id = id;
        this.content = content;
        this.articleId = articleId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
    }
}