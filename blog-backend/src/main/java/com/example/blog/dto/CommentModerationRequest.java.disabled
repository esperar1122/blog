package com.example.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class CommentModerationRequest {

    @NotBlank(message = "操作状态不能为空")
    private String status;

    private String reviewNote;
}