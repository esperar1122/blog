package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_article")
public class Article {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("summary")
    private String summary;

    @TableField("cover_image")
    private String coverImage;

    @TableField("status")
    private String status;

    @TableField("view_count")
    private Long viewCount;

    @TableField("like_count")
    private Long likeCount;

    @TableField("comment_count")
    private Long commentCount;

    @TableField("is_top")
    private Boolean isTop;

    @TableField("author_id")
    private Long authorId;

    @TableField("category_id")
    private Long categoryId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField("publish_time")
    private LocalDateTime publishTime;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;

    @TableField("scheduled_publish_time")
    private LocalDateTime scheduledPublishTime;

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

    @TableField(exist = false)
    private String authorName;

    @TableField(exist = false)
    private String authorAvatar;

    @TableField(exist = false)
    private String categoryName;

    @TableField(exist = false)
    private java.util.List<Tag> tags;

    public enum Status {
        DRAFT("DRAFT"),
        PUBLISHED("PUBLISHED"),
        DELETED("DELETED");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public Article() {
        this.viewCount = 0L;
        this.likeCount = 0L;
        this.commentCount = 0L;
        this.isTop = false;
        this.status = Status.DRAFT.getValue();
        this.deleted = 0;
    }

    public boolean isPublished() {
        return Status.PUBLISHED.getValue().equals(this.status);
    }

    public boolean isDraft() {
        return Status.DRAFT.getValue().equals(this.status);
    }

    public boolean isDeleted() {
        return Status.DELETED.getValue().equals(this.status);
    }

    public boolean isScheduled() {
        return scheduledPublishTime != null && scheduledPublishTime.isAfter(java.time.LocalDateTime.now());
    }

    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null ? 0L : this.viewCount) + 1L;
    }

    public void incrementLikeCount() {
        this.likeCount = (this.likeCount == null ? 0L : this.likeCount) + 1L;
    }

    public void decrementLikeCount() {
        this.likeCount = Math.max(0L, (this.likeCount == null ? 0L : this.likeCount) - 1L);
    }

    public void incrementCommentCount() {
        this.commentCount = (this.commentCount == null ? 0L : this.commentCount) + 1L;
    }

    public void decrementCommentCount() {
        this.commentCount = Math.max(0L, (this.commentCount == null ? 0L : this.commentCount) - 1L);
    }
}