package com.example.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新分类请求
 */
@Data
public class UpdateCategoryRequest {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    private String name;

    @Size(max = 200, message = "分类描述长度不能超过200个字符")
    private String description;

    @Size(max = 50, message = "分类图标长度不能超过50个字符")
    private String icon;

    private Long parentId;

    private Integer sortOrder;
}