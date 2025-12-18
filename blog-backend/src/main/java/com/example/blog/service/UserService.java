package com.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.dto.LoginRequest;
import com.example.blog.dto.RegisterRequest;
import com.example.blog.entity.User;
import com.example.blog.enums.UserRole;

import java.util.List;

/**
 * 简化的用户服务接口
 * 专注于大学生个人博客的核心用户功能
 */
public interface UserService {

    // === 核心用户功能 ===

    User getUserById(Long id);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    User register(RegisterRequest request);

    User login(LoginRequest request);

    User loginByUsernameOrEmail(String loginIdentifier, String password);

    boolean updateUserProfile(Long userId, User updateUser);

    boolean changePassword(Long userId, String oldPassword, String newPassword);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void updateLastLoginTime(Long id);

    // === 基础管理功能 ===

    /**
     * 获取用户列表（分页，管理员功能）
     */
    IPage<User> getUserList(int page, int size, String keyword);

    /**
     * 获取活跃用户列表
     */
    List<User> getActiveUsers();

    /**
     * 删除用户（软删除，管理员功能）
     */
    boolean deleteUser(Long userId);

    /**
     * 分配管理员角色
     */
    User assignAdminRole(Long id);

    /**
     * 移除管理员角色
     */
    User removeAdminRole(Long id);

    /**
     * 重置用户密码（管理员功能）
     */
    boolean resetPassword(Long id, String newPassword);

    // === 统计信息 ===

    /**
     * 获取总用户数
     */
    int getTotalUserCount();

    /**
     * 获取活跃用户数
     */
    int getActiveUserCount();

    /**
     * 根据角色获取用户数
     */
    int getUserCountByRole(UserRole role);
}