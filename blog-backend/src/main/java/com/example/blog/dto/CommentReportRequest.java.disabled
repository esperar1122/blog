package com.example.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CommentReportRequest {

    @NotNull(message = "评论ID不能为空")
    private Long commentId;

    @NotBlank(message = "举报原因不能为空")
    private String reason;

    private String description;
}