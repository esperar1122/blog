package com.example.blog.enums;

/**
 * 文章状态枚举
 */
public enum ArticleStatus {
    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    DELETED("DELETED", "已删除");

    private final String code;
    private final String description;

    ArticleStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return code;
    }

    /**
     * 根据代码获取状态枚举
     */
    public static ArticleStatus fromCode(String code) {
        for (ArticleStatus status : ArticleStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return DRAFT; // 默认返回草稿状态
    }
}