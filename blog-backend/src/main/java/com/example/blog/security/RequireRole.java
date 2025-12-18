package com.example.blog.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限验证注解
 * 用于方法级权限控制，要求用户具有指定角色
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * 需要的角色
     */
    String value() default "USER";

    /**
     * 权限验证失败时的错误信息
     */
    String message() default "权限不足";
}