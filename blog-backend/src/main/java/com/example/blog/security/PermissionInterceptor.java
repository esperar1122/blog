package com.example.blog.security;

import com.example.blog.enums.UserRole;
import com.example.blog.util.PermissionUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * 权限拦截器
 * 处理自定义权限注解的验证逻辑
 */
@Aspect
@Component
@Slf4j
public class PermissionInterceptor {

    /**
     * 处理@RequireRole注解
     */
    @Before("@annotation(requireRole)")
    public void checkRequireRole(JoinPoint joinPoint, RequireRole requireRole) {
        log.debug("检查@RequireRole权限: {}", requireRole.value());

        UserRole currentRole = PermissionUtils.getCurrentUserRole();
        if (currentRole == null) {
            log.warn("用户未认证，无法访问方法: {}", joinPoint.getSignature().toShortString());
            throw new AccessDeniedException(requireRole.message());
        }

        if (!requireRole.value().equals(currentRole)) {
            log.warn("用户角色 {} 不满足所需角色 {}", currentRole, requireRole.value());
            throw new AccessDeniedException(requireRole.message());
        }

        log.debug("权限验证通过: 用户角色 {}", currentRole);
    }

    /**
     * 处理@RequireAdmin注解
     */
    @Before("@annotation(requireAdmin)")
    public void checkRequireAdmin(JoinPoint joinPoint, RequireAdmin requireAdmin) {
        log.debug("检查@RequireAdmin权限");

        if (!PermissionUtils.isCurrentUserAdmin()) {
            log.warn("用户非管理员，无法访问方法: {}", joinPoint.getSignature().toShortString());
            throw new AccessDeniedException(requireAdmin.message());
        }

        log.debug("管理员权限验证通过");
    }
}