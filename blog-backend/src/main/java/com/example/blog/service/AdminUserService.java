package com.example.blog.service;

import com.example.blog.dto.request.UserUpdateRequest;
import com.example.blog.dto.request.UserQueryRequest;
import com.example.blog.dto.response.UserListResponse;
import com.example.blog.dto.response.UserStatsResponse;
import com.example.blog.entity.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface AdminUserService {

    /**
     * 获取用户列表
     */
    Page<User> getUserList(UserQueryRequest request);

    /**
     * 根据ID获取用户
     */
    User getUserById(Long id);

    /**
     * 更新用户信息
     */
    User updateUser(Long id, UserUpdateRequest request, Long adminId);

    /**
     * 禁用/启用用户
     */
    User toggleUserStatus(Long id, Long adminId);

    /**
     * 删除用户（逻辑删除）
     */
    void deleteUser(Long id, Long adminId);

    /**
     * 永久删除用户
     */
    void permanentDeleteUser(Long id, Long adminId);

    /**
     * 获取用户统计信息
     */
    UserStatsResponse getUserStats();

    /**
     * 记录管理员操作日志
     */
    void logAdminOperation(Long adminId, String operation, String details);
}