package com.example.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.Result;
import com.example.blog.dto.request.UserUpdateRequest;
import com.example.blog.dto.response.UserStatsResponse;
import com.example.blog.entity.User;
import com.example.blog.service.AdminUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@ActiveProfiles("test")
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminUserService adminUserService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private UserStatsResponse testStats;

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

        testStats = new UserStatsResponse();
        testStats.setTotalUsers(100L);
        testStats.setActiveUsers(80L);
        testStats.setBannedUsers(10L);
        testStats.setAdminUsers(5L);
        testStats.setTodayRegistrations(5L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserList() throws Exception {
        // Given
        Page<User> userPage = new Page<>(0, 20);
        userPage.setRecords(Arrays.asList(testUser));
        userPage.setTotal(1);

        when(adminUserService.getUserList(any())).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/admin/users")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].username").value("testuser"))
                .andExpect(jsonPath("$.data.total").value(1));

        verify(adminUserService).getUserList(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserList_WithFilters() throws Exception {
        // Given
        Page<User> userPage = new Page<>(0, 20);
        userPage.setRecords(Arrays.asList(testUser));
        userPage.setTotal(1);

        when(adminUserService.getUserList(any())).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/admin/users")
                .param("page", "0")
                .param("size", "20")
                .param("role", "USER")
                .param("status", "ACTIVE")
                .param("keyword", "test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(adminUserService).getUserList(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserById() throws Exception {
        // Given
        when(adminUserService.getUserById(1L)).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/admin/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(adminUserService).getUserById(1L);
        verify(adminUserService).logAdminOperation(anyLong(), eq("查看用户"), anyString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserById_NotFound() throws Exception {
        // Given
        when(adminUserService.getUserById(1L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/admin/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("用户不存在"));

        verify(adminUserService).getUserById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUser() throws Exception {
        // Given
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setNickname("Updated User");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setRole("USER");
        updateRequest.setStatus("ACTIVE");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setNickname("Updated User");
        updatedUser.setEmail("updated@example.com");

        when(adminUserService.updateUser(eq(1L), any(UserUpdateRequest.class), anyLong()))
                .thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/admin/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("Updated User"))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"));

        verify(adminUserService).updateUser(eq(1L), any(UserUpdateRequest.class), anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUser_Error() throws Exception {
        // Given
        UserUpdateRequest updateRequest = new UserUpdateRequest();

        when(adminUserService.updateUser(eq(1L), any(UserUpdateRequest.class), anyLong()))
                .thenThrow(new RuntimeException("用户不存在"));

        // When & Then
        mockMvc.perform(put("/admin/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("用户不存在"));

        verify(adminUserService).updateUser(eq(1L), any(UserUpdateRequest.class), anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testToggleUserStatus() throws Exception {
        // Given
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setStatus(User.Status.BANNED.getValue());

        when(adminUserService.toggleUserStatus(1L, anyLong())).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/admin/users/1/toggle-status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("BANNED"));

        verify(adminUserService).toggleUserStatus(1L, anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser_Logical() throws Exception {
        // Given
        doNothing().when(adminUserService).deleteUser(1L, anyLong());

        // When & Then
        mockMvc.perform(delete("/admin/users/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("用户已删除"));

        verify(adminUserService).deleteUser(1L, anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser_Permanent() throws Exception {
        // Given
        doNothing().when(adminUserService).permanentDeleteUser(1L, anyLong());

        // When & Then
        mockMvc.perform(delete("/admin/users/1")
                .with(csrf())
                .param("permanent", "true")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("用户已永久删除"));

        verify(adminUserService).permanentDeleteUser(1L, anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUserStats() throws Exception {
        // Given
        when(adminUserService.getUserStats()).thenReturn(testStats);

        // When & Then
        mockMvc.perform(get("/admin/users/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(100))
                .andExpect(jsonPath("$.data.activeUsers").value(80))
                .andExpect(jsonPath("$.data.bannedUsers").value(10))
                .andExpect(jsonPath("$.data.adminUsers").value(5))
                .andExpect(jsonPath("$.data.todayRegistrations").value(5));

        verify(adminUserService).getUserStats();
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAccessDenied_ForNonAdmin() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(adminUserService, never()).getUserList(any());
    }

    @Test
    void testAccessDenied_Unauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(adminUserService, never()).getUserList(any());
    }
}