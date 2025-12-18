package com.example.blog.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 简化的权限检查工具类
 * 提供基础的用户角色检查功能
 */
@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<String> currentUserRole = new ThreadLocal<>();

    /**
     * 设置当前用户角色
     */
    public static void setCurrentUserRole(String role) {
        currentUserRole.set(role);
        log.debug("设置当前用户角色: {}", role);
    }

    /**
     * 获取当前用户角色
     */
    public static String getCurrentUserRole() {
        return currentUserRole.get();
    }

    /**
     * 清除当前用户角色
     */
    public static void clearCurrentUserRole() {
        currentUserRole.remove();
        log.debug("清除当前用户角色");
    }

    /**
     * 检查当前用户是否为管理员
     */
    public static boolean isCurrentUserAdmin() {
        String role = getCurrentUserRole();
        return "ADMIN".equals(role);
    }

    /**
     * 检查当前用户是否为普通用户
     */
    public static boolean isCurrentUser() {
        String role = getCurrentUserRole();
        return "USER".equals(role);
    }

    /**
     * 检查当前用户是否具有指定角色
     */
    public static boolean hasRole(String requiredRole) {
        String currentRole = getCurrentUserRole();
        if (currentRole == null) {
            return false;
        }
        return requiredRole.equals(currentRole);
    }

    /**
     * 验证管理员权限
     */
    public static void validateAdmin() {
        if (!isCurrentUserAdmin()) {
            throw new SecurityException("需要管理员权限");
        }
    }

    /**
     * 验证指定角色权限
     */
    public static void validateRole(String requiredRole) {
        if (!hasRole(requiredRole)) {
            throw new SecurityException("需要 " + requiredRole + " 权限");
        }
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 在请求开始时清除可能存在的用户角色信息
        clearCurrentUserRole();
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) {
        // 在请求完成后清除用户角色信息
        clearCurrentUserRole();
    }
}