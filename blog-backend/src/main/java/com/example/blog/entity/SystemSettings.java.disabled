package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_system_setting")
@Schema(description = "系统设置实体")
public class SystemSettings {

    @TableId(type = IdType.AUTO)
    @Schema(description = "设置ID")
    private Long id;

    @Schema(description = "设置键")
    private String settingKey;

    @Schema(description = "设置值")
    private String settingValue;

    @Schema(description = "数据类型")
    private String settingType;

    @Schema(description = "设置描述")
    private String description;

    @Schema(description = "是否系统设置")
    private Boolean isSystem;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}