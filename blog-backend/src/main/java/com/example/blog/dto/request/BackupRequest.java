package com.example.blog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@Schema(description = "备份请求")
public class BackupRequest {

    @NotBlank(message = "备份名称不能为空")
    @Size(max = 100, message = "备份名称长度不能超过100个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_\\-\\u4e00-\\u9fa5]+$", message = "备份名称只能包含字母、数字、下划线、中划线和中文")
    @Schema(description = "备份名称", example = "daily_backup_20231216")
    private String name;

    @Size(max = 500, message = "备份描述长度不能超过500个字符")
    @Schema(description = "备份描述", example = "每日定时备份")
    private String description;

    @Schema(description = "是否压缩备份", example = "true")
    private Boolean compressed = true;

    @Schema(description = "备份类型", example = "FULL", allowableValues = {"FULL", "INCREMENTAL", "DIFFERENTIAL"})
    private String backupType = "FULL";

    @Schema(description = "是否包含结构", example = "true")
    private Boolean includeStructure = true;

    @Schema(description = "是否包含数据", example = "true")
    private Boolean includeData = true;

    @Schema(description = "是否包含触发器", example = "true")
    private Boolean includeTriggers = false;

    @Schema(description = "是否包含存储过程", example = "true")
    private Boolean includeRoutines = false;

    @Schema(description = "是否包含事件", example = "false")
    private Boolean includeEvents = false;

    @Schema(description = "排除的表", example = "t_log,t_temp")
    private String excludeTables;

    @Schema(description = "包含的表", example = "t_user,t_article")
    private String includeTables;
}