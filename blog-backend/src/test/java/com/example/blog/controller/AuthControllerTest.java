package com.example.blog.controller;

import com.example.blog.dto.LoginRequest;
import com.example.blog.dto.RefreshTokenRequest;
import com.example.blog.dto.RegisterRequest;
import com.example.blog.entity.User;
import com.example.blog.security.JwtTokenProvider;
import com.example.blog.service.LoginAttemptService;
import com.example.blog.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController 安全测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private LoginAttemptService loginAttemptService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setNickname("测试用户");
        testUser.setRole("user");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setEmail("new@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setNickname("新用户");

        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("refresh-token");
    }

    @Test
    @DisplayName("用户登录成功 - 正常流程")
    void login_Success() throws Exception {
        // 准备测试数据
        when(loginAttemptService.isLocked(anyString(), anyString())).thenReturn(false);
        when(userService.loginByUsernameOrEmail(anyString(), anyString())).thenReturn(testUser);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyLong(), anyString()))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(anyString(), anyLong()))
                .thenReturn("refresh-token");

        // 执行请求并验证结果
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.username").value("testuser"));

        // 验证服务调用
        verify(loginAttemptService).isLocked("testuser", "127.0.0.1");
        verify(userService).loginByUsernameOrEmail("testuser", "password123");
        verify(jwtTokenProvider).generateAccessToken("testuser", 1L, "user");
        verify(jwtTokenProvider).generateRefreshToken("testuser", 1L);
        verify(loginAttemptService).clearFailedAttempts("testuser", "127.0.0.1");
    }

    @Test
    @DisplayName("用户登录失败 - 账户已锁定")
    void login_AccountLocked() throws Exception {
        // 准备测试数据
        when(loginAttemptService.isLocked(anyString(), anyString())).thenReturn(true);
        when(loginAttemptService.getRemainingLockTime(anyString(), anyString())).thenReturn(30L);

        // 执行请求并验证结果
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value(423))
                .andExpect(jsonPath("$.message").value("账户已锁定，请 30 分钟后重试"));

        // 验证服务调用
        verify(loginAttemptService).isLocked("testuser", "127.0.0.1");
        verify(loginAttemptService).getRemainingLockTime("testuser", "127.0.0.1");
        verify(userService, never()).loginByUsernameOrEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("用户登录失败 - 用户名或密码错误")
    void login_InvalidCredentials() throws Exception {
        // 准备测试数据
        when(loginAttemptService.isLocked(anyString(), anyString())).thenReturn(false);
        when(userService.loginByUsernameOrEmail(anyString(), anyString()))
                .thenThrow(new RuntimeException("用户名或密码错误"));
        when(loginAttemptService.getAttemptCount(anyString(), anyString())).thenReturn(1);

        // 执行请求并验证结果
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));

        // 验证服务调用
        verify(loginAttemptService).recordFailedAttempt("testuser", "127.0.0.1");
    }

    @Test
    @DisplayName("用户登录失败 - 次数过多导致锁定")
    void login_TooManyAttempts_ThenLocked() throws Exception {
        // 准备测试数据
        when(loginAttemptService.isLocked(anyString(), anyString()))
                .thenReturn(false)  // 第一次检查未锁定
                .thenReturn(true); // 第二次检查已锁定
        when(userService.loginByUsernameOrEmail(anyString(), anyString()))
                .thenThrow(new RuntimeException("用户名或密码错误"));
        when(loginAttemptService.getAttemptCount(anyString(), anyString())).thenReturn(5);
        when(loginAttemptService.getRemainingLockTime(anyString(), anyString())).thenReturn(30L);

        // 执行请求并验证结果
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value(423))
                .andExpect(jsonPath("$.message").value("登录失败次数过多，账户已锁定 30 分钟"));

        // 验证服务调用
        verify(loginAttemptService).recordFailedAttempt("testuser", "127.0.0.1");
        verify(loginAttemptService).getRemainingLockTime("testuser", "127.0.0.1");
    }

    @Test
    @DisplayName("刷新令牌成功")
    void refreshToken_Success() throws Exception {
        // 准备测试数据
        when(jwtTokenProvider.extractUsername(anyString())).thenReturn("testuser");
        when(jwtTokenProvider.extractUserId(anyString())).thenReturn(1L);
        when(jwtTokenProvider.validateRefreshToken(anyString(), anyString())).thenReturn(true);
        when(userService.getUserByUsername(anyString())).thenReturn(testUser);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyLong(), anyString()))
                .thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(anyString(), anyLong()))
                .thenReturn("new-refresh-token");

        // 执行请求并验证结果
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("令牌刷新成功"))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));

        // 验证服务调用
        verify(jwtTokenProvider).extractUsername("refresh-token");
        verify(jwtTokenProvider).validateRefreshToken("refresh-token", "testuser");
        verify(jwtTokenProvider).revokeRefreshToken("testuser");
        verify(jwtTokenProvider).generateAccessToken("testuser", 1L, "user");
    }

    @Test
    @DisplayName("刷新令牌失败 - 无效的刷新令牌")
    void refreshToken_InvalidToken() throws Exception {
        // 准备测试数据
        when(jwtTokenProvider.extractUsername(anyString())).thenReturn("testuser");
        when(jwtTokenProvider.extractUserId(anyString())).thenReturn(1L);
        when(jwtTokenProvider.validateRefreshToken(anyString(), anyString())).thenReturn(false);

        // 执行请求并验证结果
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("无效的刷新令牌"));

        // 验证服务调用
        verify(jwtTokenProvider).validateRefreshToken("refresh-token", "testuser");
        verify(jwtTokenProvider, never()).generateAccessToken(anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("用户登出成功")
    void logout_Success() throws Exception {
        // 准备测试数据
        when(jwtTokenProvider.extractUsername(anyString())).thenReturn("testuser");

        // 执行请求并验证结果
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登出成功"));

        // 验证服务调用
        verify(jwtTokenProvider).extractUsername("access-token");
        verify(jwtTokenProvider).blacklistToken("access-token");
        verify(jwtTokenProvider).revokeRefreshToken("testuser");
    }

    @Test
    @DisplayName("获取当前用户信息成功")
    void getCurrentUser_Success() throws Exception {
        // 准备测试数据
        when(jwtTokenProvider.validateAccessToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.extractUsername(anyString())).thenReturn("testuser");
        when(userService.getUserByUsername(anyString())).thenReturn(testUser);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        // 验证服务调用
        verify(jwtTokenProvider).validateAccessToken("access-token");
        verify(jwtTokenProvider).extractUsername("access-token");
        verify(userService).getUserByUsername("testuser");
    }

    @Test
    @DisplayName("获取当前用户信息失败 - 无效令牌")
    void getCurrentUser_InvalidToken() throws Exception {
        // 准备测试数据
        when(jwtTokenProvider.validateAccessToken(anyString())).thenReturn(false);

        // 执行请求并验证结果
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("无效或已过期的认证令牌"));

        // 验证服务调用
        verify(jwtTokenProvider).validateAccessToken("invalid-token");
        verify(jwtTokenProvider, never()).extractUsername(anyString());
        verify(userService, never()).getUserByUsername(anyString());
    }
}