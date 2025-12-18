package com.example.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class SensitiveWordRequest {

    @NotBlank(message = "敏感词不能为空")
    private String word;

    private String replacement;

    @NotBlank(message = "匹配模式不能为空")
    private String pattern;

    @NotBlank(message = "敏感词类型不能为空")
    private String type;
}