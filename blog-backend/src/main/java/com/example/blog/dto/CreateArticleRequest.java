package com.example.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
public class CreateArticleRequest {

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题不能超过200个字符")
    private String title;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    @Size(max = 500, message = "文章摘要不能超过500个字符")
    private String summary;

    private String coverImage;

    private Long categoryId;

    private List<Long> tagIds;

    private String status = com.example.blog.entity.Article.Status.DRAFT.getValue();
}