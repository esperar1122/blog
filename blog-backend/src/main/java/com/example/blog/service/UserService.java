package com.example.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.User;

import java.util.List;

public interface UserService {

    User getUserById(Long id);

    User getUserByUsername(String username);

    User getUserByEmail(String email);

    User register(String username, String email, String password, String nickname);

    User login(String username, String password);

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
}