package com.example.blog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "备份响应")
public class BackupResponse {

    @Schema(description = "备份ID")
    private Long id;

    @Schema(description = "备份名称")
    private String name;

    @Schema(description = "备份描述")
    private String description;

    @Schema(description = "备份类型")
    private String backupType;

    @Schema(description = "备份文件路径")
    private String filePath;

    @Schema(description = "备份文件大小（字节）")
    private Long fileSize;

    @Schema(description = "备份状态")
    private String status; // IN_PROGRESS, SUCCESS, FAILED

    @Schema(description = "是否压缩")
    private Boolean compressed;

    @Schema(description = "创建者")
    private String createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "执行时长（毫秒）")
    private Long duration;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "备份统计信息")
    private BackupStatsResponse stats;

    @Schema(description = "是否可恢复")
    private Boolean restorable;
}