package com.example.blog.controller;

import com.example.blog.common.Result;
import com.example.blog.dto.LoginRequest;
import com.example.blog.dto.RefreshTokenRequest;
import com.example.blog.dto.RegisterRequest;
import com.example.blog.entity.User;
import com.example.blog.security.JwtTokenProvider;
import com.example.blog.service.LoginAttemptService;
import com.example.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request,
                                               HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        log.info("用户注册请求 - username: {}, email: {}, ip: {}",
                request.getUsername(), request.getEmail(), ipAddress);

        User user = userService.register(
            request.getUsername(),
            request.getEmail(),
            request.getPassword(),
            request.getNickname()
        );

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getUsername(), user.getId(), user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getId());

        Map<String, Object> data = buildAuthResponse(user, accessToken, refreshToken, httpRequest);

        log.info("用户注册成功 - username: {}, userId: {}", user.getUsername(), user.getId());
        return Result.success("注册成功", data);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request,
                                           HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        String loginIdentifier = request.getUsername();

        log.info("用户登录请求 - identifier: {}, ip: {}", loginIdentifier, ipAddress);

        // 检查登录失败次数限制
        if (loginAttemptService.isLocked(loginIdentifier, ipAddress)) {
            long remainingTime = loginAttemptService.getRemainingLockTime(loginIdentifier, ipAddress);
            log.warn("登录被拒绝 - 账户已锁定: identifier: {}, ip: {}, 剩余锁定时间: {} 分钟",
                    loginIdentifier, ipAddress, remainingTime);

            return Result.error(423, "账户已锁定，请 " + remainingTime + " 分钟后重试");
        }

        try {
            // 尝试登录（支持用户名或邮箱）
            User user = userService.loginByUsernameOrEmail(loginIdentifier, request.getPassword());

            // 登录成功，清除失败记录
            loginAttemptService.clearFailedAttempts(loginIdentifier, ipAddress);

            // 生成令牌
            String accessToken = jwtTokenProvider.generateAccessToken(
                    user.getUsername(), user.getId(), user.getRole());
            String refreshToken = jwtTokenProvider.generateRefreshToken(
                    user.getUsername(), user.getId());

            Map<String, Object> data = buildAuthResponse(user, accessToken, refreshToken, httpRequest);

            log.info("用户登录成功 - username: {}, userId: {}, ip: {}",
                    user.getUsername(), user.getId(), ipAddress);
            return Result.success("登录成功", data);

        } catch (Exception e) {
            // 登录失败，记录失败尝试
            loginAttemptService.recordFailedAttempt(loginIdentifier, ipAddress);
            int attemptCount = loginAttemptService.getAttemptCount(loginIdentifier, ipAddress);

            log.warn("用户登录失败 - identifier: {}, ip: {}, 失败次数: {}, 错误: {}",
                    loginIdentifier, ipAddress, attemptCount, e.getMessage());

            // 检查是否因此次失败而被锁定
            if (loginAttemptService.isLocked(loginIdentifier, ipAddress)) {
                long remainingTime = loginAttemptService.getRemainingLockTime(loginIdentifier, ipAddress);
                return Result.error(423, "登录失败次数过多，账户已锁定 " + remainingTime + " 分钟");
            }

            return Result.error("用户名或密码错误");
        }
    }

    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken(@Valid @RequestBody RefreshTokenRequest request,
                                                   HttpServletRequest httpRequest) {
        String refreshToken = request.getRefreshToken();
        String ipAddress = getClientIpAddress(httpRequest);

        log.debug("刷新令牌请求 - ip: {}", ipAddress);

        if (!StringUtils.hasText(refreshToken)) {
            return Result.error("刷新令牌不能为空");
        }

        try {
            // 验证刷新令牌并提取用户信息
            String username = jwtTokenProvider.extractUsername(refreshToken);
            Long userId = jwtTokenProvider.extractUserId(refreshToken);

            // 验证刷新令牌的有效性
            if (!jwtTokenProvider.validateRefreshToken(refreshToken, username)) {
                log.warn("无效的刷新令牌 - username: {}, ip: {}", username, ipAddress);
                return Result.error("无效的刷新令牌");
            }

            // 获取用户信息
            User user = userService.getUserByUsername(username);

            // 撤销旧的刷新令牌
            jwtTokenProvider.revokeRefreshToken(username);

            // 生成新的令牌
            String newAccessToken = jwtTokenProvider.generateAccessToken(
                    user.getUsername(), user.getId(), user.getRole());

            String newRefreshToken = jwtTokenProvider.generateRefreshToken(
                    user.getUsername(), user.getId());

            Map<String, Object> data = buildAuthResponse(user, newAccessToken, newRefreshToken, httpRequest);

            log.info("令牌刷新成功 - username: {}, ip: {}", username, ipAddress);
            return Result.success("令牌刷新成功", data);

        } catch (Exception e) {
            log.error("刷新令牌失败 - ip: {}, 错误: {}", ipAddress, e.getMessage());
            return Result.error("刷新令牌失败");
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization,
                             HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        String token = extractTokenFromHeader(authorization);
        String username = null;

        try {
            if (StringUtils.hasText(token)) {
                username = jwtTokenProvider.extractUsername(token);

                // 将访问令牌加入黑名单
                jwtTokenProvider.blacklistToken(token);

                // 撤销用户的刷新令牌
                jwtTokenProvider.revokeRefreshToken(username);
            }

            log.info("用户登出 - username: {}, ip: {}", username, ipAddress);
            return Result.success("登出成功");

        } catch (Exception e) {
            log.warn("登出处理出现异常 - ip: {}, 错误: {}", ipAddress, e.getMessage());
            return Result.success("登出成功");
        }
    }

    @GetMapping("/me")
    public Result<User> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        String token = extractTokenFromHeader(authorization);

        if (!StringUtils.hasText(token)) {
            return Result.error("未提供认证令牌");
        }

        if (!jwtTokenProvider.validateAccessToken(token)) {
            return Result.error("无效或已过期的认证令牌");
        }

        String username = jwtTokenProvider.extractUsername(token);
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

    private Map<String, Object> buildAuthResponse(User user, String accessToken, String refreshToken,
                                                 HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();

        // 访问令牌放在响应体中
        data.put("accessToken", accessToken);
        data.put("tokenType", "Bearer");
        data.put("expiresIn", jwtTokenProvider.getAccessTokenExpirationMinutes() * 60);
        data.put("user", user);

        // 刷新令牌通过HttpOnly Cookie返回，提高安全性
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(jwtTokenProvider.getRefreshTokenExpirationDays() * 24 * 60 * 60)
                .sameSite("Strict")
                .build();

        // 注意：在Spring Boot中，你需要通过HttpServletResponse来设置Cookie
        // 这里只是为了演示Cookie的创建逻辑

        return data;
    }

    private String extractTokenFromHeader(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring(7);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // 如果是多个IP，取第一个
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }
}