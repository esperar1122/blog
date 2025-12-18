package com.example.blog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "文章操作日志响应")
public class ArticleOperationLogResponse {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "文章ID")
    private Long articleId;

    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "操作类型描述")
    private String operationTypeDescription;

    @Schema(description = "操作前状态")
    private String oldStatus;

    @Schema(description = "操作后状态")
    private String newStatus;

    @Schema(description = "操作者ID")
    private Long operatorId;

    @Schema(description = "操作者姓名")
    private String operatorName;

    @Schema(description = "操作者头像")
    private String operatorAvatar;

    @Schema(description = "操作者IP地址")
    private String operatorIp;

    @Schema(description = "操作详情")
    private String operationDetail;

    @Schema(description = "操作时间")
    private LocalDateTime createTime;
}