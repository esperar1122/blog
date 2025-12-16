package com.example.blog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Data
@Schema(description = "发布文章请求")
public class PublishArticleRequest {

    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID", required = true)
    private Long articleId;

    @Schema(description = "发布备注")
    private String publishNote;
}