package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.LoginRequest;
import com.example.blog.dto.RegisterRequest;
import com.example.blog.entity.User;
import com.example.blog.enums.UserRole;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public User getUserByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    @Override
    public User loginByUsernameOrEmail(String loginIdentifier, String password) {
        // 尝试通过用户名查找
        User user = getUserByUsername(loginIdentifier);
        if (user == null) {
            // 如果用户名不存在，尝试通过邮箱查找
            user = getUserByEmail(loginIdentifier);
        }

        if (user == null) {
            throw new BusinessException("用户名或邮箱不存在");
        }

        // 简化版本：直接比较密码
        if (!password.equals(user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        log.info("用户登录成功 - identifier: {}", loginIdentifier);

        // 返回用户信息（不包含密码）
        user.setPassword(null);
        return user;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userMapper.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }

    @Override
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (getUserByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (getUserByEmail(request.getEmail()) != null) {
            throw new BusinessException("邮箱已被使用");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // 不加密，简化版本
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole(UserRole.USER.toString());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);
        log.info("用户注册成功 - username: {}", user.getUsername());

        // 返回用户信息（不包含密码）
        user.setPassword(null);
        return user;
    }

    @Override
    public User login(LoginRequest request) {
        User user = getUserByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 简化版本：直接比较密码（不加密）
        if (!request.getPassword().equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        log.info("用户登录成功 - username: {}", user.getUsername());

        // 返回用户信息（不包含密码）
        user.setPassword(null);
        return user;
    }

    @Override
    public boolean updateUserProfile(Long userId, User updateUser) {
        User existingUser = getUserById(userId);

        if (updateUser.getUsername() != null && !updateUser.getUsername().equals(existingUser.getUsername())) {
            // 检查新用户名是否已存在
            if (getUserByUsername(updateUser.getUsername()) != null) {
                throw new BusinessException("用户名已存在");
            }
            existingUser.setUsername(updateUser.getUsername());
        }

        if (updateUser.getEmail() != null && !updateUser.getEmail().equals(existingUser.getEmail())) {
            // 检查新邮箱是否已存在
            if (getUserByEmail(updateUser.getEmail()) != null) {
                throw new BusinessException("邮箱已被使用");
            }
            existingUser.setEmail(updateUser.getEmail());
        }

        if (updateUser.getNickname() != null) {
            existingUser.setNickname(updateUser.getNickname());
        }

        if (updateUser.getAvatar() != null) {
            existingUser.setAvatar(updateUser.getAvatar());
        }

        existingUser.setUpdatedAt(LocalDateTime.now());

        int result = userMapper.updateById(existingUser);
        return result > 0;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        // 简化版本：直接比较密码
        if (!oldPassword.equals(user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(newPassword);
        user.setUpdateTime(LocalDateTime.now());

        int result = userMapper.updateById(user);
        return result > 0;
    }

    @Override
    public IPage<User> getUserList(int page, int size, String keyword) {
        Page<User> pageParam = new Page<>(page, size);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("username", keyword)
                .or()
                .like("nickname", keyword)
                .or()
                .like("email", keyword)
            );
        }

        queryWrapper.orderByDesc("create_time");

        return userMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public List<User> getActiveUsers() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0).eq("status", "ACTIVE");
        return userMapper.selectList(queryWrapper);
    }

  
    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        User user = getUserById(userId);
        if (user == null) {
            log.warn("用户不存在 - userId: {}", userId);
            return false;
        }

        // 软删除：设置为已删除状态
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId)
                    .set("deleted", 1)
                    .set("update_time", LocalDateTime.now());

        int result = userMapper.update(null, updateWrapper);

        if (result > 0) {
            log.info("用户删除成功 - userId: {}, username: {}", userId, user.getUsername());
        }

        return result > 0;
    }

    
    @Override
    public User assignAdminRole(Long id) {
        User user = getUserById(id);
        user.setRole(UserRole.ADMIN.toString());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("分配管理员权限成功 - userId: {}", id);
        return user;
    }

    @Override
    public User removeAdminRole(Long id) {
        User user = getUserById(id);
        user.setRole(UserRole.USER.toString());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("移除管理员权限成功 - userId: {}", id);
        return user;
    }

    @Override
    public boolean resetPassword(Long id, String newPassword) {
        User user = getUserById(id);
        user.setPassword(newPassword);
        user.setUpdateTime(LocalDateTime.now());

        int result = userMapper.updateById(user);
        if (result > 0) {
            log.info("密码重置成功 - userId: {}", id);
        }
        return result > 0;
    }

    @Override
    public void updateLastLoginTime(Long id) {
        User user = getUserById(id);
        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    // === 统计信息方法 ===

    @Override
    public int getTotalUserCount() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0);
        return Math.toIntExact(userMapper.selectCount(queryWrapper));
    }

    @Override
    public int getActiveUserCount() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0).eq("status", "ACTIVE");
        return Math.toIntExact(userMapper.selectCount(queryWrapper));
    }

    @Override
    public int getUserCountByRole(UserRole role) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0).eq("role", role.toString());
        return Math.toIntExact(userMapper.selectCount(queryWrapper));
    }
}