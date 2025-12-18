package com.example.blog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "文章状态管理响应")
public class ArticleStatusManagementResponse {

    @Schema(description = "文章ID")
    private Long articleId;

    @Schema(description = "文章状态")
    private String status;

    @Schema(description = "文章状态描述")
    private String statusDescription;

    @Schema(description = "是否置顶")
    private Boolean isTop;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "删除时间")
    private LocalDateTime deletedAt;

    @Schema(description = "定时发布时间")
    private LocalDateTime scheduledPublishTime;

    @Schema(description = "操作结果")
    private String operationResult;

    @Schema(description = "操作消息")
    private String message;

    @Schema(description = "操作时间")
    private LocalDateTime operationTime;
}