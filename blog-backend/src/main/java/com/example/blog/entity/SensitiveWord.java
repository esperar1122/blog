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

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public enum WordType {
        FILTER("FILTER"),
        BLOCK("BLOCK"),
        WARNING("WARNING");

        private final String value;

        WordType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public SensitiveWord() {
        this.type = WordType.FILTER.getValue();
    }

    public boolean isFilterType() {
        return WordType.FILTER.getValue().equals(this.type);
    }

    public boolean isBlockType() {
        return WordType.BLOCK.getValue().equals(this.type);
    }

    public boolean isWarningType() {
        return WordType.WARNING.getValue().equals(this.type);
    }
}