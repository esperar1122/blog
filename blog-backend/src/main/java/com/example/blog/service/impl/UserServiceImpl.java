package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.UserService;
import com.example.blog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
    @Transactional
    public User register(String username, String email, String password, String nickname) {
        if (existsByUsername(username)) {
            throw new BusinessException("用户名已存在");
        }
        if (existsByEmail(email)) {
            throw new BusinessException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setRole(User.Role.USER.getValue());
        user.setStatus(User.Status.ACTIVE.getValue());

        userMapper.insert(user);
        return user;
    }

    @Override
    public User login(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username)
                   .or()
                   .eq("email", username);

        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!User.Status.ACTIVE.getValue().equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        updateLastLoginTime(user.getId());
        return user;
    }

    @Override
    @Transactional
    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);

        if (user.getNickname() != null) {
            existingUser.setNickname(user.getNickname());
        }
        if (user.getBio() != null) {
            existingUser.setBio(user.getBio());
        }
        if (user.getAvatar() != null) {
            existingUser.setAvatar(user.getAvatar());
        }

        userMapper.updateById(existingUser);
        return existingUser;
    }

    @Override
    @Transactional
    public User updateUserProfile(Long id, String nickname, String bio, String avatar) {
        User user = getUserById(id);

        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }

        userMapper.updateById(user);
        return user;
    }

    @Override
    @Transactional
    public boolean changePassword(Long id, String oldPassword, String newPassword) {
        User user = getUserById(id);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return userMapper.updateById(user) > 0;
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
    public IPage<User> getUsersWithPagination(Page<User> page, String role) {
        return userMapper.selectUsersWithRole(page, role);
    }

    @Override
    public List<User> getActiveUsers() {
        return userMapper.selectActiveUsers();
    }

    @Override
    @Transactional
    public boolean banUser(Long id) {
        User user = getUserById(id);
        if (user.isAdmin()) {
            throw new BusinessException("不能禁用管理员账户");
        }

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                    .set("status", User.Status.BANNED.getValue());
        return userMapper.update(null, updateWrapper) > 0;
    }

    @Override
    @Transactional
    public boolean unbanUser(Long id) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                    .set("status", User.Status.ACTIVE.getValue());
        return userMapper.update(null, updateWrapper) > 0;
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        User user = getUserById(id);
        if (user.isAdmin()) {
            throw new BusinessException("不能删除管理员账户");
        }
        return userMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public User assignAdminRole(Long id) {
        User user = getUserById(id);
        user.setRole(User.Role.ADMIN.getValue());
        userMapper.updateById(user);
        return user;
    }

    @Override
    @Transactional
    public User removeAdminRole(Long id) {
        User user = getUserById(id);
        user.setRole(User.Role.USER.getValue());
        userMapper.updateById(user);
        return user;
    }

    @Override
    @Transactional
    public boolean resetPassword(Long id, String newPassword) {
        User user = getUserById(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional
    public void updateLastLoginTime(Long id) {
        userMapper.updateLastLoginTime(id);
    }
}