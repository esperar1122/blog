package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_system_settings")
public class SystemSettings {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("setting_key")
    private String settingKey;

    @TableField("setting_value")
    private String settingValue;

    @TableField("description")
    private String description;

    @TableField("type")
    private String type;

    @TableField("is_public")
    private Boolean isPublic;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public enum Type {
        STRING("STRING"),
        NUMBER("NUMBER"),
        BOOLEAN("BOOLEAN"),
        JSON("JSON");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public SystemSettings() {
        this.type = Type.STRING.getValue();
        this.isPublic = false;
    }

    public boolean isPublic() {
        return Boolean.TRUE.equals(this.isPublic);
    }
}