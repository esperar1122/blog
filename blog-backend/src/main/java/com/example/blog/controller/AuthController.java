package com.example.blog.controller;

import com.example.blog.common.Result;
import com.example.blog.entity.User;
import com.example.blog.service.UserService;
import com.example.blog.util.JwtUtil;
import com.example.blog.dto.LoginRequest;
import com.example.blog.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册请求 - username: {}, email: {}", request.getUsername(), request.getEmail());

        User user = userService.register(
            request.getUsername(),
            request.getEmail(),
            request.getPassword(),
            request.getNickname()
        );

        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());

        Map<String, Object> data = buildAuthResponse(user, token);

        log.info("用户注册成功 - username: {}, userId: {}", user.getUsername(), user.getId());
        return Result.success("注册成功", data);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录请求 - username: {}", request.getUsername());

        User user = userService.login(request.getUsername(), request.getPassword());

        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());

        Map<String, Object> data = buildAuthResponse(user, token);

        log.info("用户登录成功 - username: {}, userId: {}", user.getUsername(), user.getId());
        return Result.success("登录成功", data);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        String username = extractUsernameFromToken(authorization);
        log.info("用户登出 - username: {}", username);
        return Result.success("登出成功");
    }

    @GetMapping("/me")
    public Result<User> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        String token = extractTokenFromHeader(authorization);
        String username = jwtUtil.extractUsername(token);
        User user = userService.getUserByUsername(username);

        return Result.success(user);
    }

    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return Result.success(exists);
    }

    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return Result.success(exists);
    }

    private Map<String, Object> buildAuthResponse(User user, String token) {
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        return data;
    }

    private String extractTokenFromHeader(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid authorization header");
        }
        return authorization.substring(7);
    }

    private String extractUsernameFromToken(String authorization) {
        String token = extractTokenFromHeader(authorization);
        return jwtUtil.extractUsername(token);
    }
}