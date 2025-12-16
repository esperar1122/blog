package com.example.blog.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Schema(description = "用户更新请求")
public class UserUpdateRequest {

    @Schema(description = "昵称", example = "张三")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    @Schema(description = "邮箱", example = "user@example.com")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @Schema(description = "个人简介", example = "这是我的个人简介")
    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    private String bio;

    @Schema(description = "用户角色", example = "USER", allowableValues = {"USER", "ADMIN"})
    @Pattern(regexp = "^(USER|ADMIN)$", message = "用户角色只能是USER或ADMIN")
    private String role;

    @Schema(description = "用户状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "BANNED"})
    @Pattern(regexp = "^(ACTIVE|INACTIVE|BANNED)$", message = "用户状态只能是ACTIVE、INACTIVE或BANNED")
    private String status;
}