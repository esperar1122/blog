package com.example.blog.service;

import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.UserMapper;
import com.example.blog.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setNickname("测试用户");
        testUser.setRole(User.Role.USER.getValue());
        testUser.setStatus(User.Status.ACTIVE.getValue());
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());
    }

    @Test
    void testRegister_Success() {
        // 准备测试数据
        when(userMapper.existsByUsername("testuser")).thenReturn(false);
        when(userMapper.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // 执行测试
        User result = userService.register("testuser", "test@example.com", "password123", "测试用户");

        // 验证结果
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals("测试用户", result.getNickname());
        assertEquals(User.Role.USER.getValue(), result.getRole());
        assertEquals(User.Status.ACTIVE.getValue(), result.getStatus());

        // 验证方法调用
        verify(userMapper).existsByUsername("testuser");
        verify(userMapper).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void testRegister_UsernameExists() {
        // 准备测试数据
        when(userMapper.existsByUsername("testuser")).thenReturn(true);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register("testuser", "test@example.com", "password123", "测试用户");
        });

        assertEquals("用户名已存在", exception.getMessage());
        verify(userMapper).existsByUsername("testuser");
        verify(userMapper, never()).existsByEmail(anyString());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void testRegister_EmailExists() {
        // 准备测试数据
        when(userMapper.existsByUsername("testuser")).thenReturn(false);
        when(userMapper.existsByEmail("test@example.com")).thenReturn(true);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register("testuser", "test@example.com", "password123", "测试用户");
        });

        assertEquals("邮箱已被注册", exception.getMessage());
        verify(userMapper).existsByUsername("testuser");
        verify(userMapper).existsByEmail("test@example.com");
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void testLogin_Success() {
        // 准备测试数据
        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(userMapper.updateLastLoginTime(1L)).thenReturn(1);

        // 执行测试
        User result = userService.login("testuser", "password123");

        // 验证结果
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());

        // 验证方法调用
        verify(userMapper).selectOne(any());
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(userMapper).updateLastLoginTime(1L);
    }

    @Test
    void testLogin_UserNotFound() {
        // 准备测试数据
        when(userMapper.selectOne(any())).thenReturn(null);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login("nonexistent", "password123");
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(userMapper).selectOne(any());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testLogin_UserBanned() {
        // 准备测试数据
        testUser.setStatus(User.Status.BANNED.getValue());
        when(userMapper.selectOne(any())).thenReturn(testUser);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login("testuser", "password123");
        });

        assertEquals("账号已被禁用", exception.getMessage());
        verify(userMapper).selectOne(any());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testLogin_WrongPassword() {
        // 准备测试数据
        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login("testuser", "wrongpassword");
        });

        assertEquals("密码错误", exception.getMessage());
        verify(userMapper).selectOne(any());
        verify(passwordEncoder).matches("wrongpassword", "encodedPassword");
    }

    @Test
    void testGetUserById_Success() {
        // 准备测试数据
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // 执行测试
        User result = userService.getUserById(1L);

        // 验证结果
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userMapper).selectById(1L);
    }

    @Test
    void testGetUserById_NotFound() {
        // 准备测试数据
        when(userMapper.selectById(999L)).thenReturn(null);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.getUserById(999L);
        });

        assertEquals("用户不存在", exception.getMessage());
        verify(userMapper).selectById(999L);
    }

    @Test
    void testExistsByUsername_True() {
        // 准备测试数据
        when(userMapper.existsByUsername("testuser")).thenReturn(true);

        // 执行测试
        boolean result = userService.existsByUsername("testuser");

        // 验证结果
        assertTrue(result);
        verify(userMapper).existsByUsername("testuser");
    }

    @Test
    void testExistsByUsername_False() {
        // 准备测试数据
        when(userMapper.existsByUsername("nonexistent")).thenReturn(false);

        // 执行测试
        boolean result = userService.existsByUsername("nonexistent");

        // 验证结果
        assertFalse(result);
        verify(userMapper).existsByUsername("nonexistent");
    }

    @Test
    void testExistsByEmail_True() {
        // 准备测试数据
        when(userMapper.existsByEmail("test@example.com")).thenReturn(true);

        // 执行测试
        boolean result = userService.existsByEmail("test@example.com");

        // 验证结果
        assertTrue(result);
        verify(userMapper).existsByEmail("test@example.com");
    }

    @Test
    void testExistsByEmail_False() {
        // 准备测试数据
        when(userMapper.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // 执行测试
        boolean result = userService.existsByEmail("nonexistent@example.com");

        // 验证结果
        assertFalse(result);
        verify(userMapper).existsByEmail("nonexistent@example.com");
    }

    @Test
    void testUpdateLastLoginTime() {
        // 准备测试数据
        when(userMapper.updateLastLoginTime(1L)).thenReturn(1);

        // 执行测试
        userService.updateLastLoginTime(1L);

        // 验证方法调用
        verify(userMapper).updateLastLoginTime(1L);
    }
}