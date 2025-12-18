package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("t_admin_log")
public class AdminLog {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("admin_id")
    private Long adminId;

    @TableField("admin_username")
    private String adminUsername;

    @TableField("operation")
    private String operation;

    @TableField("target_user_id")
    private Long targetUserId;

    @TableField("target_username")
    private String targetUsername;

    @TableField("details")
    private String details;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    public enum Operation {
        USER_UPDATE("用户更新"),
        USER_STATUS_TOGGLE("用户状态切换"),
        USER_DELETE("用户删除"),
        USER_PERMANENT_DELETE("用户永久删除"),
        USER_VIEW("查看用户");

        private final String description;

        Operation(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}