package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_tag")
public class Tag {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("color")
    private String color;

    @TableField("article_count")
    private Integer articleCount;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("deleted")
    private Integer deleted;

    // 兼容性方法
    public LocalDateTime getCreatedAt() {
        return createTime;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createTime = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updateTime;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updateTime = updatedAt;
    }

    public Tag() {
        this.color = "#1890ff";
        this.articleCount = 0;
        this.deleted = 0;
    }
}