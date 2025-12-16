package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("login_attempts")
public class LoginAttempt {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String ipAddress;

    private Integer attemptCount;

    private LocalDateTime lastAttemptTime;

    private LocalDateTime lockedUntil;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}