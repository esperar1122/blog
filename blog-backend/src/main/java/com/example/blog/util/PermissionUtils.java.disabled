package com.example.blog.util;

import com.example.blog.enums.UserRole;
import com.example.blog.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 权限检查工具类
 * 提供常用的权限验证方法
 */
public class PermissionUtils {

    /**
     * 获取当前认证用户
     */
    public static CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        CustomUserDetails currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static UserRole getCurrentUserRole() {
        CustomUserDetails currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getRole() : null;
    }

    /**
     * 检查当前用户是否为管理员
     */
    public static boolean isCurrentUserAdmin() {
        CustomUserDetails currentUser = getCurrentUser();
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * 检查当前用户是否为普通用户
     */
    public static boolean isCurrentUserUser() {
        CustomUserDetails currentUser = getCurrentUser();
        return currentUser != null && currentUser.isUser();
    }

    /**
     * 检查当前用户是否有指定角色
     */
    public static boolean hasRole(UserRole requiredRole) {
        UserRole currentRole = getCurrentUserRole();
        return currentRole != null && currentRole == requiredRole;
    }

    /**
     * 检查当前用户是否可以访问指定用户资源
     * 管理员可以访问所有用户资源，普通用户只能访问自己的资源
     */
    public static boolean canAccessUserResource(Long targetUserId) {
        CustomUserDetails currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        // 管理员可以访问所有用户资源
        if (currentUser.isAdmin()) {
            return true;
        }

        // 普通用户只能访问自己的资源
        return currentUser.getId().equals(targetUserId);
    }

    /**
     * 检查当前用户是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 验证用户是否有管理员权限，如果没有则抛出异常
     */
    public static void requireAdmin() {
        if (!isCurrentUserAdmin()) {
            throw new SecurityException("需要管理员权限");
        }
    }

    /**
     * 验证用户是否有指定角色权限，如果没有则抛出异常
     */
    public static void requireRole(UserRole requiredRole) {
        if (!hasRole(requiredRole)) {
            throw new SecurityException("需要 " + requiredRole.getDescription() + " 权限");
        }
    }

    /**
     * 验证用户是否可以访问指定用户资源，如果不能则抛出异常
     */
    public static void requireUserResourceAccess(Long targetUserId) {
        if (!canAccessUserResource(targetUserId)) {
            throw new SecurityException("没有权限访问该用户资源");
        }
    }
}