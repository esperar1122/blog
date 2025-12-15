package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_notification")
public class Notification {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("type")
    private String type;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("related_id")
    private Long relatedId;

    @TableField("related_type")
    private String relatedType;

    @TableField("is_read")
    private Boolean isRead;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String userName;

    public enum Type {
        COMMENT("COMMENT", "新评论"),
        LIKE("LIKE", "点赞"),
        REPLY("REPLY", "回复"),
        SYSTEM("SYSTEM", "系统通知");

        private final String value;
        private final String description;

        Type(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum RelatedType {
        ARTICLE("ARTICLE", "文章"),
        COMMENT("COMMENT", "评论");

        private final String value;
        private final String description;

        RelatedType(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }
    }

    public Notification() {
        this.isRead = false;
    }

    public boolean isRead() {
        return Boolean.TRUE.equals(this.isRead);
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void markAsUnread() {
        this.isRead = false;
    }

    public static Notification createCommentNotification(Long userId, String title, String content, Long articleId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(Type.COMMENT.getValue());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(articleId);
        notification.setRelatedType(RelatedType.ARTICLE.getValue());
        return notification;
    }

    public static Notification createLikeNotification(Long userId, String title, String content, Long articleId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(Type.LIKE.getValue());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(articleId);
        notification.setRelatedType(RelatedType.ARTICLE.getValue());
        return notification;
    }

    public static Notification createReplyNotification(Long userId, String title, String content, Long commentId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(Type.REPLY.getValue());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedId(commentId);
        notification.setRelatedType(RelatedType.COMMENT.getValue());
        return notification;
    }

    public static Notification createSystemNotification(Long userId, String title, String content) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(Type.SYSTEM.getValue());
        notification.setTitle(title);
        notification.setContent(content);
        return notification;
    }
}