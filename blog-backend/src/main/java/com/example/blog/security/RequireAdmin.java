package com.example.blog.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 管理员权限验证注解
 * 用于方法级权限控制，要求用户具有管理员角色
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAdmin {

    /**
     * 权限验证失败时的错误信息
     */
    String message() default "需要管理员权限";
}