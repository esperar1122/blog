package com.example.blog.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

@Slf4j
public class SensitiveDataUtil {

    private static final Pattern JDBC_URL_PATTERN = Pattern.compile("jdbc:mysql://([^:]+):([^@]+)@([^:]+):([^/]+)/([^\\?]+)");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("\"password\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b1[3-9]\\d{9}\\b");

    /**
     * 脱敏数据库连接URL
     */
    public static String maskDatabaseUrl(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        try {
            return JDBC_URL_PATTERN.matcher(url)
                .replaceAll("jdbc:mysql://****:****@$3:$4/$5");
        } catch (Exception e) {
            log.warn("数据库URL脱敏失败", e);
            return url.replaceAll("(password=)[^&;]+", "$1****");
        }
    }

    /**
     * 脱敏JSON中的密码字段
     */
    public static String maskPasswordInJson(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }

        try {
            return PASSWORD_PATTERN.matcher(json)
                .replaceAll("\"password\":\"****\"");
        } catch (Exception e) {
            log.warn("JSON密码脱敏失败", e);
            return json;
        }
    }

    /**
     * 脱敏邮箱地址
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }

        try {
            return EMAIL_PATTERN.matcher(email)
                .replaceAll(match -> {
                    String emailStr = match.group();
                    int atIndex = emailStr.indexOf('@');
                    if (atIndex > 2) {
                        return emailStr.substring(0, 2) + "****" + emailStr.substring(atIndex - 1);
                    }
                    return emailStr.substring(0, 1) + "****" + emailStr.substring(atIndex);
                });
        } catch (Exception e) {
            log.warn("邮箱脱敏失败", e);
            return email;
        }
    }

    /**
     * 脱敏手机号码
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return phone;
        }

        try {
            return PHONE_PATTERN.matcher(phone)
                .replaceAll(match -> {
                    String phoneStr = match.group();
                    return phoneStr.substring(0, 3) + "****" + phoneStr.substring(7);
                });
        } catch (Exception e) {
            log.warn("手机号脱敏失败", e);
            return phone;
        }
    }

    /**
     * 通用脱敏方法 - 对字符串进行部分隐藏
     */
    public static String maskString(String str, int keepStart, int keepEnd) {
        if (str == null || str.length() <= keepStart + keepEnd) {
            return str;
        }

        String start = str.substring(0, keepStart);
        String end = str.substring(str.length() - keepEnd);
        String mask = "****";

        return start + mask + end;
    }

    /**
     * 检查字符串是否包含敏感信息
     */
    public static boolean containsSensitiveData(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }

        String lowerData = data.toLowerCase();
        return lowerData.contains("password") ||
               lowerData.contains("token") ||
               lowerData.contains("secret") ||
               lowerData.contains("key") ||
               JDBC_URL_PATTERN.matcher(data).find() ||
               PASSWORD_PATTERN.matcher(data).find();
    }

    /**
     * 对请求参数进行脱敏处理
     */
    public static String sanitizeParams(String params) {
        if (params == null || params.isEmpty()) {
            return params;
        }

        String sanitized = params;

        // 脱敏密码相关字段
        sanitized = sanitized.replaceAll("(?i)(\"password\"\\s*:\\s*\")([^\"]+)(\")", "$1****$3");
        sanitized = sanitized.replaceAll("(?i)(\"pwd\"\\s*:\\s*\")([^\"]+)(\")", "$1****$3");

        // 脱敏token相关字段
        sanitized = sanitized.replaceAll("(?i)(\"token\"\\s*:\\s*\")([^\"]{10,})(\")", "$1****$3");
        sanitized = sanitized.replaceAll("(?i)(\"authorization\"\\s*:\\s*\")([^\"]{10,})(\")", "$1****$3");

        // 脱敏数据库连接信息
        sanitized = maskDatabaseUrl(sanitized);

        // 如果参数过长，进行截断
        if (sanitized.length() > 500) {
            sanitized = sanitized.substring(0, 500) + "...[参数过长已截断]";
        }

        return sanitized;
    }

    /**
     * 对响应结果进行脱敏处理
     */
    public static String sanitizeResult(String result) {
        if (result == null || result.isEmpty()) {
            return result;
        }

        String sanitized = result;

        // 脱敏可能的敏感信息
        sanitized = sanitized.replaceAll("(?i)(\"password\"\\s*:\\s*\")([^\"]+)(\")", "$1****$3");
        sanitized = sanitized.replaceAll("(?i)(\"token\"\\s*:\\s*\")([^\"]{10,})(\")", "$1****$3");
        sanitized = sanitized.replaceAll("(?i)(\"secret\"\\s*:\\s*\")([^\"]+)(\")", "$1****$3");

        // 脱敏数据库连接信息
        sanitized = maskDatabaseUrl(sanitized);

        // 脱敏邮箱和电话
        sanitized = maskEmail(sanitized);
        sanitized = maskPhone(sanitized);

        // 如果结果过长，进行截断
        if (sanitized.length() > 500) {
            sanitized = sanitized.substring(0, 500) + "...[响应过长已截断]";
        }

        return sanitized;
    }
}