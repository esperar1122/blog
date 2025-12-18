package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_backup_record")
@Schema(description = "备份记录实体")
public class BackupRecord {

    @TableId(type = IdType.AUTO)
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

    @Schema(description = "备份文件大小")
    private Long fileSize;

    @Schema(description = "备份状态")
    private String status; // IN_PROGRESS, SUCCESS, FAILED

    @Schema(description = "是否压缩")
    private Boolean compressed;

    @Schema(description = "是否包含结构")
    private Boolean includeStructure;

    @Schema(description = "是否包含数据")
    private Boolean includeData;

    @Schema(description = "是否包含触发器")
    private Boolean includeTriggers;

    @Schema(description = "是否包含存储过程")
    private Boolean includeRoutines;

    @Schema(description = "是否包含事件")
    private Boolean includeEvents;

    @Schema(description = "排除的表")
    private String excludeTables;

    @Schema(description = "包含的表")
    private String includeTables;

    @Schema(description = "创建者ID")
    private Long createdBy;

    @Schema(description = "创建者名称")
    private String createdByName;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "执行时长（毫秒）")
    private Long duration;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "数据库版本")
    private String databaseVersion;

    @Schema(description = "备份数据库")
    private String databaseName;

    @Schema(description = "备份校验和")
    private String checksum;
}