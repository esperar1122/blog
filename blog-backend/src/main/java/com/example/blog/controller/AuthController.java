package com.example.blog.controller;

import com.example.blog.dto.LoginRequest;
import com.example.blog.dto.RegisterRequest;
import com.example.blog.entity.User;
import com.example.blog.security.JwtTokenProvider;
import com.example.blog.security.PermissionInterceptor;
import com.example.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request,
                                           HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        log.info("用户注册请求 - username: {}, email: {}, ip: {}",
                request.getUsername(), request.getEmail(), ipAddress);

        try {
            User user = userService.register(request);

            // 生成JWT令牌
            String token = jwtTokenProvider.generateToken(
                    user.getUsername(),
                    user.getId(),
                    user.getRole()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("token", token);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", jwtTokenProvider.getAccessTokenExpirationMinutes() * 60);
            response.put("message", "注册成功");

            return response;
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage());
            return createErrorResponse("注册失败: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest request,
                                         HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        log.info("用户登录请求 - username: {}, ip: {}", request.getUsername(), ipAddress);

        try {
            User user = userService.login(request);

            // 生成JWT令牌
            String token = jwtTokenProvider.generateToken(
                    user.getUsername(),
                    user.getId(),
                    user.getRole()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("token", token);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", jwtTokenProvider.getAccessTokenExpirationMinutes() * 60);
            response.put("message", "登录成功");

            log.info("用户登录成功 - username: {}", user.getUsername());
            return response;
        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage());
            return createErrorResponse("登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestHeader(value = "Authorization", required = false) String authorization,
                                        HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        log.info("用户登出 - ip: {}", ipAddress);

        // 清除当前用户角色
        PermissionInterceptor.clearCurrentUserRole();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "登出成功");
        return response;
    }

    @GetMapping("/validate")
    public Map<String, Object> validateToken(@RequestHeader("Authorization") String authorization) {
        try {
            if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
                return createErrorResponse("无效的授权头");
            }

            String token = authorization.substring(7); // 移除 "Bearer " 前缀

            if (!jwtTokenProvider.validateToken(token)) {
                return createErrorResponse("令牌无效或已过期");
            }

            // 从令牌中提取用户信息
            String username = jwtTokenProvider.extractUsername(token);
            Long userId = jwtTokenProvider.extractUserId(token);
            String role = jwtTokenProvider.extractRole(token);

            // 设置当前用户角色到线程本地变量
            PermissionInterceptor.setCurrentUserRole(role);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("username", username);
            response.put("userId", userId);
            response.put("role", role);

            return response;
        } catch (Exception e) {
            log.error("令牌验证失败: {}", e.getMessage());
            return createErrorResponse("令牌验证失败");
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}