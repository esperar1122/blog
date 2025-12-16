package com.example.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.request.UserUpdateRequest;
import com.example.blog.dto.request.UserQueryRequest;
import com.example.blog.dto.response.UserStatsResponse;
import com.example.blog.entity.AdminLog;
import com.example.blog.entity.User;
import com.example.blog.mapper.AdminLogMapper;
import com.example.blog.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private AdminLogMapper adminLogMapper;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setNickname("Test User");
        testUser.setRole(User.Role.USER.getValue());
        testUser.setStatus(User.Status.ACTIVE.getValue());
        testUser.setCreateTime(LocalDateTime.now());

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRole(User.Role.ADMIN.getValue());
    }

    @Test
    void testGetUserList() {
        // Given
        UserQueryRequest request = new UserQueryRequest();
        request.setPage(0);
        request.setSize(20);
        request.setKeyword("test");

        Page<User> expectedPage = new Page<>(0, 20);
        expectedPage.setRecords(Arrays.asList(testUser));
        expectedPage.setTotal(1);

        when(userMapper.selectPage(any(Page.class), any(QueryWrapper.class)))
                .thenReturn(expectedPage);

        // When
        Page<User> result = adminUserService.getUserList(request);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("testuser", result.getRecords().get(0).getUsername());
        verify(userMapper).selectPage(any(Page.class), any(QueryWrapper.class));
    }

    @Test
    void testGetUserById() {
        // Given
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUser);

        // When
        User result = adminUserService.getUserById(1L);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userMapper).selectOne(any(QueryWrapper.class));
    }

    @Test
    void testUpdateUser() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.selectById(2L)).thenReturn(adminUser);
        when(userMapper.update(null, any())).thenReturn(1);
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUser);

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setNickname("Updated User");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setRole(User.Role.USER.getValue());
        updateRequest.setStatus(User.Status.ACTIVE.getValue());

        // When
        User result = adminUserService.updateUser(1L, updateRequest, 2L);

        // Then
        assertNotNull(result);
        verify(userMapper).update(null, any());
        verify(adminLogMapper).insert(any(AdminLog.class));
    }

    @Test
    void testToggleUserStatus() {
        // Given
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUser);
        when(userMapper.selectById(2L)).thenReturn(adminUser);
        when(userMapper.update(null, any())).thenReturn(1);

        // When
        User result = adminUserService.toggleUserStatus(1L, 2L);

        // Then
        assertNotNull(result);
        verify(userMapper).update(null, any());
        verify(adminLogMapper).insert(any(AdminLog.class));
    }

    @Test
    void testToggleUserStatus_SelfToggle() {
        // Given
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUser);
        when(userMapper.selectById(1L)).thenReturn(testUser);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminUserService.toggleUserStatus(1L, 1L);
        });

        assertEquals("不能禁用自己的账户", exception.getMessage());
    }

    @Test
    void testDeleteUser() {
        // Given
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUser);
        when(userMapper.selectById(2L)).thenReturn(adminUser);
        when(userMapper.update(null, any())).thenReturn(1);

        // When
        adminUserService.deleteUser(1L, 2L);

        // Then
        verify(userMapper).update(null, any());
        verify(adminLogMapper).insert(any(AdminLog.class));
    }

    @Test
    void testPermanentDeleteUser() {
        // Given
        when(userMapper.selectOne(any(QueryWrapper.class))).thenReturn(testUser);
        when(userMapper.selectById(2L)).thenReturn(adminUser);
        when(userMapper.deleteById(1L)).thenReturn(1);

        // When
        adminUserService.permanentDeleteUser(1L, 2L);

        // Then
        verify(userMapper).deleteById(1L);
        verify(adminLogMapper).insert(any(AdminLog.class));
    }

    @Test
    void testGetUserStats() {
        // Given
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(100L, 80L, 10L, 10L, 5L, 95L, 5L, 20L, 50L);

        // When
        UserStatsResponse result = adminUserService.getUserStats();

        // Then
        assertNotNull(result);
        assertEquals(100L, result.getTotalUsers());
        assertEquals(80L, result.getActiveUsers());
        assertEquals(10L, result.getBannedUsers());
        assertEquals(5L, result.getAdminUsers());
        verify(userMapper, times(9)).selectCount(any(QueryWrapper.class));
    }

    @Test
    void testLogAdminOperation() {
        // Given
        when(userMapper.selectById(2L)).thenReturn(adminUser);
        when(adminLogMapper.insert(any(AdminLog.class))).thenReturn(1);

        // When
        adminUserService.logAdminOperation(2L, "TEST_OPERATION", "Test details");

        // Then
        verify(userMapper).selectById(2L);
        verify(adminLogMapper).insert(any(AdminLog.class));
    }

    @Test
    void testUpdateUser_UserNotFound() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(null);
        when(userMapper.selectById(2L)).thenReturn(adminUser);

        UserUpdateRequest updateRequest = new UserUpdateRequest();

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminUserService.updateUser(1L, updateRequest, 2L);
        });

        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    void testUpdateUser_AdminNotFound() {
        // Given
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.selectById(2L)).thenReturn(null);

        UserUpdateRequest updateRequest = new UserUpdateRequest();

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminUserService.updateUser(1L, updateRequest, 2L);
        });

        assertEquals("无权限执行此操作", exception.getMessage());
    }
}