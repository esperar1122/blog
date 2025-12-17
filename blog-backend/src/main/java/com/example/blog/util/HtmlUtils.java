package com.example.blog.util;

import org.apache.commons.text.StringEscapeUtils;

/**
 * HTML工具类，用于处理HTML转义和XSS防护
 */
public class HtmlUtils {

    /**
     * 转义HTML特殊字符，防止XSS攻击
     *
     * @param input 需要转义的字符串
     * @return 转义后的字符串
     */
    public static String escape(String input) {
        if (input == null) {
            return null;
        }

        // 使用Apache Commons Text进行HTML转义
        return StringEscapeUtils.escapeHtml4(input);
    }

    /**
     * 转义HTML特殊字符，但保留某些安全标签（可选功能）
     * 目前实现完全转义，确保最大安全性
     *
     * @param input 需要转义的字符串
     * @return 转义后的字符串
     */
    public static String escapeSelective(String input) {
        // 为了安全起见，当前实现与escape相同
        // 如果需要允许某些标签，可以在这里实现白名单逻辑
        return escape(input);
    }

    /**
     * 验证字符串是否包含潜在的恶意HTML
     *
     * @param input 需要验证的字符串
     * @return true 如果包含潜在恶意内容，false 否则
     */
    public static boolean containsMaliciousHtml(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        String lowerInput = input.toLowerCase();

        // 检查常见的恶意HTML标签和属性
        String[] maliciousPatterns = {
            "<script",
            "javascript:",
            "onload=",
            "onerror=",
            "onclick=",
            "onmouseover=",
            "<iframe",
            "<object",
            "<embed",
            "<form",
            "<input",
            "<textarea",
            "vbscript:",
            "data:",
            "<link",
            "<meta",
            "<style",
            "<base",
            "<frameset",
            "<frame"
        };

        for (String pattern : maliciousPatterns) {
            if (lowerInput.contains(pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 清理标签名称，移除或替换不安全字符
     *
     * @param tagName 原始标签名称
     * @return 清理后的安全标签名称
     */
    public static String sanitizeTagName(String tagName) {
        if (tagName == null) {
            return null;
        }

        // 移除HTML标签
        String cleaned = tagName.replaceAll("<[^>]*>", "");

        // 转义特殊字符
        cleaned = escape(cleaned);

        // 移除多余的空格
        cleaned = cleaned.trim().replaceAll("\\s+", " ");

        // 限制长度
        if (cleaned.length() > 30) {
            cleaned = cleaned.substring(0, 30);
        }

        return cleaned;
    }
}