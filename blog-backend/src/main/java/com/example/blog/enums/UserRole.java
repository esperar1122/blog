package com.example.blog.enums;

/**
 * 用户角色枚举
 * 定义系统中的用户角色类型
 */
public enum UserRole {
    USER("USER", "普通用户"),
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取角色枚举
     */
    public static UserRole fromCode(String code) {
        for (UserRole role : UserRole.values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role code: " + code);
    }

    /**
     * 检查是否为管理员角色
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * 检查是否为普通用户角色
     */
    public boolean isUser() {
        return this == USER;
    }
}