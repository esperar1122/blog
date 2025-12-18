package com.example.blog.util;

/**
 * HTML工具类，用于处理HTML转义和XSS防护
 * 使用Java标准库实现，无需外部依赖
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

        // 使用Java标准库进行HTML转义
        StringBuilder escaped = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#39;");
                    break;
                case '&':
                    escaped.append("&amp;");
                    break;
                default:
                    escaped.append(c);
                    break;
            }
        }
        return escaped.toString();
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
     * 清理HTML内容，移除所有HTML标签，只保留纯文本
     *
     * @param html 需要清理的HTML内容
     * @return 清理后的纯文本内容
     */
    public static String cleanHtml(String html) {
        if (html == null) {
            return null;
        }

        // 移除所有HTML标签
        String cleaned = html.replaceAll("<[^>]*>", "");

        // 处理HTML实体
        cleaned = cleaned.replaceAll("&lt;", "<");
        cleaned = cleaned.replaceAll("&gt;", ">");
        cleaned = cleaned.replaceAll("&quot;", "\"");
        cleaned = cleaned.replaceAll("&#39;", "'");
        cleaned = cleaned.replaceAll("&amp;", "&");

        // 标准化空白字符
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        return cleaned;
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