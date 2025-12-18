package com.example.blog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "文章版本响应")
public class ArticleVersionResponse {

    @Schema(description = "版本ID")
    private Long id;

    @Schema(description = "文章ID")
    private Long articleId;

    @Schema(description = "版本号")
    private Integer versionNumber;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章内容")
    private String content;

    @Schema(description = "文章摘要")
    private String summary;

    @Schema(description = "封面图片URL")
    private String coverImage;

    @Schema(description = "变更原因")
    private String changeReason;

    @Schema(description = "编辑者ID")
    private Long editorId;

    @Schema(description = "编辑者姓名")
    private String editorName;

    @Schema(description = "编辑者头像")
    private String editorAvatar;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}