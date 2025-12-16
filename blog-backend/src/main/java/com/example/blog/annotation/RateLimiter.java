package com.example.blog.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API频率限制注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {

    /**
     * 限制时间窗口（秒）
     */
    int timeWindow() default 60;

    /**
     * 时间窗口内允许的最大请求次数
     */
    int limit() default 100;

    /**
     * 限制键的前缀
     */
    String prefix() default "rate_limit:";

    /**
     * 错误消息
     */
    String message() default "请求过于频繁，请稍后再试";
}