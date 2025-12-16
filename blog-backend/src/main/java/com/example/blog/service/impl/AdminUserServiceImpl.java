package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.dto.request.UserUpdateRequest;
import com.example.blog.dto.request.UserQueryRequest;
import com.example.blog.dto.response.UserListResponse;
import com.example.blog.dto.response.UserStatsResponse;
import com.example.blog.entity.AdminLog;
import com.example.blog.entity.User;
import com.example.blog.mapper.AdminLogMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;
    private final AdminLogMapper adminLogMapper;

    @Override
    public Page<User> getUserList(UserQueryRequest request) {
        Page<User> page = new Page<>(request.getPage(), request.getSize());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // 角色筛选
        if (StringUtils.hasText(request.getRole())) {
            queryWrapper.eq("role", request.getRole());
        }

        // 状态筛选
        if (StringUtils.hasText(request.getStatus())) {
            queryWrapper.eq("status", request.getStatus());
        }

        // 关键词搜索（用户名、邮箱、昵称）
        if (StringUtils.hasText(request.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like("username", request.getKeyword())
                    .or()
                    .like("email", request.getKeyword())
                    .or()
                    .like("nickname", request.getKeyword())
            );
        }

        // 排除已删除的用户
        queryWrapper.eq("is_deleted", false);

        // 按创建时间降序排列
        queryWrapper.orderByDesc("create_time");

        return userMapper.selectPage(page, queryWrapper);
    }

    @Override
    public User getUserById(Long id) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).eq("is_deleted", false);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User updateUser(Long id, UserUpdateRequest request, Long adminId) {
        User targetUser = getUserById(id);
        if (targetUser == null) {
            throw new RuntimeException("用户不存在");
        }

        User admin = userMapper.selectById(adminId);
        if (admin == null || !admin.isAdmin()) {
            throw new RuntimeException("无权限执行此操作");
        }

        // 记录更新前的状态
        String oldStatus = targetUser.getStatus();
        String oldRole = targetUser.getRole();

        // 更新用户信息
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);

        if (StringUtils.hasText(request.getNickname())) {
            updateWrapper.set("nickname", request.getNickname());
        }
        if (StringUtils.hasText(request.getEmail())) {
            updateWrapper.set("email", request.getEmail());
        }
        if (StringUtils.hasText(request.getBio())) {
            updateWrapper.set("bio", request.getBio());
        }
        if (StringUtils.hasText(request.getRole())) {
            updateWrapper.set("role", request.getRole());
        }
        if (StringUtils.hasText(request.getStatus())) {
            updateWrapper.set("status", request.getStatus());
        }

        updateWrapper.set("update_time", LocalDateTime.now());

        int rows = userMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new RuntimeException("更新用户失败");
        }

        // 记录操作日志
        String details = String.format("更新用户信息 - 昵称: %s -> %s, 邮箱: %s -> %s, 角色: %s -> %s, 状态: %s -> %s",
                targetUser.getNickname(), request.getNickname(),
                targetUser.getEmail(), request.getEmail(),
                oldRole, request.getRole(),
                oldStatus, request.getStatus()
        );

        logAdminOperation(adminId, AdminLog.Operation.USER_UPDATE.getDescription(), details);

        // 返回更新后的用户信息
        return getUserById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User toggleUserStatus(Long id, Long adminId) {
        User targetUser = getUserById(id);
        if (targetUser == null) {
            throw new RuntimeException("用户不存在");
        }

        User admin = userMapper.selectById(adminId);
        if (admin == null || !admin.isAdmin()) {
            throw new RuntimeException("无权限执行此操作");
        }

        // 防止管理员禁用自己
        if (id.equals(adminId)) {
            throw new RuntimeException("不能禁用自己的账户");
        }

        String newStatus = targetUser.getStatus().equals(User.Status.ACTIVE.getValue())
                ? User.Status.BANNED.getValue()
                : User.Status.ACTIVE.getValue();

        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .set("status", newStatus)
                .set("update_time", LocalDateTime.now());

        int rows = userMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new RuntimeException("更新用户状态失败");
        }

        // 记录操作日志
        String details = String.format("用户状态切换 - %s: %s -> %s",
                targetUser.getUsername(), targetUser.getStatus(), newStatus);

        logAdminOperation(adminId, AdminLog.Operation.USER_STATUS_TOGGLE.getDescription(), details);

        return getUserById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id, Long adminId) {
        User targetUser = getUserById(id);
        if (targetUser == null) {
            throw new RuntimeException("用户不存在");
        }

        User admin = userMapper.selectById(adminId);
        if (admin == null || !admin.isAdmin()) {
            throw new RuntimeException("无权限执行此操作");
        }

        // 防止管理员删除自己
        if (id.equals(adminId)) {
            throw new RuntimeException("不能删除自己的账户");
        }

        // 逻辑删除
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id)
                .set("is_deleted", true)
                .set("update_time", LocalDateTime.now());

        int rows = userMapper.update(null, updateWrapper);
        if (rows == 0) {
            throw new RuntimeException("删除用户失败");
        }

        // 记录操作日志
        String details = String.format("逻辑删除用户 - %s", targetUser.getUsername());
        logAdminOperation(adminId, AdminLog.Operation.USER_DELETE.getDescription(), details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void permanentDeleteUser(Long id, Long adminId) {
        User targetUser = getUserById(id);
        if (targetUser == null) {
            throw new RuntimeException("用户不存在");
        }

        User admin = userMapper.selectById(adminId);
        if (admin == null || !admin.isAdmin()) {
            throw new RuntimeException("无权限执行此操作");
        }

        // 防止管理员永久删除自己
        if (id.equals(adminId)) {
            throw new RuntimeException("不能永久删除自己的账户");
        }

        // 永久删除
        int rows = userMapper.deleteById(id);
        if (rows == 0) {
            throw new RuntimeException("永久删除用户失败");
        }

        // 记录操作日志
        String details = String.format("永久删除用户 - %s (邮箱: %s)", targetUser.getUsername(), targetUser.getEmail());
        logAdminOperation(adminId, AdminLog.Operation.USER_PERMANENT_DELETE.getDescription(), details);
    }

    @Override
    public UserStatsResponse getUserStats() {
        UserStatsResponse response = new UserStatsResponse();

        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDateTime monthStart = today.withDayOfMonth(1);

        // 总用户数
        QueryWrapper<User> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("is_deleted", false);
        response.setTotalUsers(userMapper.selectCount(totalWrapper));

        // 活跃用户数
        QueryWrapper<User> activeWrapper = new QueryWrapper<>();
        activeWrapper.eq("is_deleted", false).eq("status", User.Status.ACTIVE.getValue());
        response.setActiveUsers(userMapper.selectCount(activeWrapper));

        // 禁用用户数
        QueryWrapper<User> bannedWrapper = new QueryWrapper<>();
        bannedWrapper.eq("is_deleted", false).eq("status", User.Status.BANNED.getValue());
        response.setBannedUsers(userMapper.selectCount(bannedWrapper));

        // 未激活用户数
        QueryWrapper<User> inactiveWrapper = new QueryWrapper<>();
        inactiveWrapper.eq("is_deleted", false).eq("status", User.Status.INACTIVE.getValue());
        response.setInactiveUsers(userMapper.selectCount(inactiveWrapper));

        // 管理员数量
        QueryWrapper<User> adminWrapper = new QueryWrapper<>();
        adminWrapper.eq("is_deleted", false).eq("role", User.Role.ADMIN.getValue());
        response.setAdminUsers(userMapper.selectCount(adminWrapper));

        // 普通用户数量
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("is_deleted", false).eq("role", User.Role.USER.getValue());
        response.setNormalUsers(userMapper.selectCount(userWrapper));

        // 今日注册用户数
        QueryWrapper<User> todayWrapper = new QueryWrapper<>();
        todayWrapper.eq("is_deleted", false).ge("create_time", today);
        response.setTodayRegistrations(userMapper.selectCount(todayWrapper));

        // 本周注册用户数
        QueryWrapper<User> weekWrapper = new QueryWrapper<>();
        weekWrapper.eq("is_deleted", false).ge("create_time", weekStart);
        response.setWeekRegistrations(userMapper.selectCount(weekWrapper));

        // 本月注册用户数
        QueryWrapper<User> monthWrapper = new QueryWrapper<>();
        monthWrapper.eq("is_deleted", false).ge("create_time", monthStart);
        response.setMonthRegistrations(userMapper.selectCount(monthWrapper));

        return response;
    }

    @Override
    public void logAdminOperation(Long adminId, String operation, String details) {
        User admin = userMapper.selectById(adminId);
        if (admin == null) {
            return;
        }

        AdminLog adminLog = new AdminLog();
        adminLog.setAdminId(adminId);
        adminLog.setAdminUsername(admin.getUsername());
        adminLog.setOperation(operation);
        adminLog.setDetails(details);
        adminLog.setCreateTime(LocalDateTime.now());

        adminLogMapper.insert(adminLog);
        log.info("管理员操作日志: {} - {} - {}", admin.getUsername(), operation, details);
    }
}