package com.example.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class BlacklistRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String reason;

    private LocalDateTime expireTime;
}