package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_comment_report")
public class CommentReport {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("comment_id")
    private Long commentId;

    @TableField("reporter_id")
    private Long reporterId;

    @TableField("reason")
    private String reason;

    @TableField("description")
    private String description;

    @TableField("status")
    private String status;

    @TableField("reviewer_id")
    private Long reviewerId;

    @TableField("review_time")
    private LocalDateTime reviewTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public enum ReportReason {
        SPAM("SPAM"),
        INAPPROPRIATE("INAPPROPRIATE"),
        OFFENSIVE("OFFENSIVE"),
        OTHER("OTHER");

        private final String value;

        ReportReason(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum ReportStatus {
        PENDING("PENDING"),
        APPROVED("APPROVED"),
        REJECTED("REJECTED");

        private final String value;

        ReportStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public CommentReport() {
        this.status = ReportStatus.PENDING.getValue();
    }

    public boolean isPending() {
        return ReportStatus.PENDING.getValue().equals(this.status);
    }

    public boolean isApproved() {
        return ReportStatus.APPROVED.getValue().equals(this.status);
    }

    public boolean isRejected() {
        return ReportStatus.REJECTED.getValue().equals(this.status);
    }
}