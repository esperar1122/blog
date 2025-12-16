package com.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.User;
import com.example.blog.enums.UserRole;

import java.util.List;
import java.util.Map;

public interface UserService {

    User getUserById(Long id);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    User register(String username, String email, String password, String nickname);

    User login(String username, String password);

    User loginByUsernameOrEmail(String loginIdentifier, String password);

    User updateUser(Long id, User user);

    User updateUserProfile(Long id, String nickname, String bio, String avatar);

    boolean changePassword(Long id, String oldPassword, String newPassword);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    IPage<User> getUsersWithPagination(Page<User> page, String role);

    List<User> getActiveUsers();

    boolean banUser(Long id);

    boolean unbanUser(Long id);

    boolean deleteUser(Long id);

    User assignAdminRole(Long id);

    User removeAdminRole(Long id);

    boolean resetPassword(Long id, String newPassword);

    void updateLastLoginTime(Long id);

    // 权限管理相关方法

    /**
     * 获取所有用户（管理员）
     */
    Page<User> getAllUsers(Page<User> page, String keyword, UserRole role);

    /**
     * 更新用户角色（管理员）
     */
    void updateUserRole(Long userId, UserRole role);

    /**
     * 更新用户状态（管理员）
     */
    void updateUserStatus(Long userId, boolean enabled);

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

    /**
     * 批量更新用户角色（管理员）
     */
    void batchUpdateUserRole(List<Long> userIds, UserRole role);

    /**
     * 获取系统统计信息（管理员）
     */
    Map<String, Object> getSystemStats();
}