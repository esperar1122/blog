package com.example.blog.config;

import com.example.blog.security.JwtTokenProvider;
import com.example.blog.security.PermissionInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 简化的安全配置
 * 配置JWT认证和权限拦截器
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new PermissionInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/api/auth/**") // 排除登录相关接口
                .excludePathPatterns("/api/public/**") // 排除公共接口
                .excludePathPatterns("/error", "/favicon.ico"); // 排除系统路径
    }

    /**
     * 生成JWT令牌
     */
    public String generateToken(String username, Long userId, String role) {
        return jwtTokenProvider.generateToken(username, userId, role);
    }

    /**
     * 验证JWT令牌
     */
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    /**
     * 从令牌中提取用户名
     */
    public String extractUsername(String token) {
        return jwtTokenProvider.extractUsername(token);
    }

    /**
     * 从令牌中提取用户ID
     */
    public Long extractUserId(String token) {
        return jwtTokenProvider.extractUserId(token);
    }

    /**
     * 从令牌中提取用户角色
     */
    public String extractRole(String token) {
        return jwtTokenProvider.extractRole(token);
    }
}