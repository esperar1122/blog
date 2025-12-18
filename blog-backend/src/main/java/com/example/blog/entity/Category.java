package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_category")
public class Category {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("icon")
    private String icon;

    @TableField("parent_id")
    private Long parentId;

    @TableField("sort_order")
    private Integer sortOrder;

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

    public Category() {
        this.sortOrder = 0;
        this.articleCount = 0;
        this.deleted = 0;
    }

    public boolean isRootCategory() {
        return this.parentId == null || this.parentId == 0;
    }
}