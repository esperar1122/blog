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
    private Integer viewCount;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("comment_count")
    private Integer commentCount;

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
        this.viewCount = 0;
        this.likeCount = 0;
        this.commentCount = 0;
        this.isTop = false;
        this.status = Status.DRAFT.getValue();
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
        this.viewCount = (this.viewCount == null ? 0 : this.viewCount) + 1;
    }

    public void incrementLikeCount() {
        this.likeCount = (this.likeCount == null ? 0 : this.likeCount) + 1;
    }

    public void decrementLikeCount() {
        this.likeCount = Math.max(0, (this.likeCount == null ? 0 : this.likeCount) - 1);
    }

    public void incrementCommentCount() {
        this.commentCount = (this.commentCount == null ? 0 : this.commentCount) + 1;
    }

    public void decrementCommentCount() {
        this.commentCount = Math.max(0, (this.commentCount == null ? 0 : this.commentCount) - 1);
    }
}