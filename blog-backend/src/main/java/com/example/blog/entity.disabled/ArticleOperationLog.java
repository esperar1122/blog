package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_article_operation_log")
public class ArticleOperationLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("article_id")
    private Long articleId;

    @TableField("operation_type")
    private String operationType;

    @TableField("old_status")
    private String oldStatus;

    @TableField("new_status")
    private String newStatus;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("operator_ip")
    private String operatorIp;

    @TableField("operation_detail")
    private String operationDetail;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String operatorName;

    @TableField(exist = false)
    private String operatorAvatar;

    public enum OperationType {
        PUBLISH("PUBLISH", "发布文章"),
        UNPUBLISH("UNPUBLISH", "下线文章"),
        PIN("PIN", "置顶文章"),
        UNPIN("UNPIN", "取消置顶"),
        SOFT_DELETE("SOFT_DELETE", "删除文章"),
        RESTORE("RESTORE", "恢复文章"),
        SCHEDULE_PUBLISH("SCHEDULE_PUBLISH", "定时发布"),
        UPDATE("UPDATE", "更新文章");

        private final String value;
        private final String description;

        OperationType(String value, String description) {
            this.value = value;
            this.description = description;
        }

        public String getValue() {
            return value;
        }

        public String getDescription() {
            return description;
        }

        public static OperationType fromValue(String value) {
            for (OperationType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown operation type: " + value);
        }
    }
}