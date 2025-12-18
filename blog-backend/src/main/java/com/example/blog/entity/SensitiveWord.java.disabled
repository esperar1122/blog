package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_sensitive_word")
public class SensitiveWord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("word")
    private String word;

    @TableField("replacement")
    private String replacement;

    @TableField("pattern")
    private String pattern;

    @TableField("type")
    private String type;

    @TableField("level")
    private String level;

    @TableField("status")
    private String status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public enum Type {
        WORD("WORD"),
        REGEX("REGEX");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum Level {
        LOW("LOW"),
        MEDIUM("MEDIUM"),
        HIGH("HIGH");

        private final String value;

        Level(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum Status {
        ACTIVE("ACTIVE"),
        INACTIVE("INACTIVE");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public SensitiveWord() {
        this.type = Type.WORD.getValue();
        this.level = Level.MEDIUM.getValue();
        this.status = Status.ACTIVE.getValue();
    }

    public boolean isActive() {
        return Status.ACTIVE.getValue().equals(this.status);
    }

    public boolean isRegex() {
        return Type.REGEX.getValue().equals(this.type);
    }
}