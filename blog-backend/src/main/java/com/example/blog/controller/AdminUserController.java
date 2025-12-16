package com.example.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.Result;
import com.example.blog.dto.request.UserQueryRequest;
import com.example.blog.dto.request.UserUpdateRequest;
import com.example.blog.dto.response.UserListResponse;
import com.example.blog.dto.response.UserStatsResponse;
import com.example.blog.entity.User;
import com.example.blog.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "管理员用户管理", description = "管理员用户管理相关API")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    private Long getCurrentAdminId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getId();
        }
        throw new RuntimeException("未认证的管理员");
    }

    @GetMapping
    @Operation(summary = "获取用户列表", description = "分页获取用户列表，支持按角色、状态筛选和关键词搜索")
    public Result<Page<User>> getUserList(
            @Parameter(description = "页码，默认0") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量，默认20") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "角色筛选") @RequestParam(required = false) String role,
            @Parameter(description = "状态筛选") @RequestParam(required = false) String status,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {

        UserQueryRequest request = new UserQueryRequest();
        request.setPage(page);
        request.setSize(size);
        request.setRole(role);
        request.setStatus(status);
        request.setKeyword(keyword);

        Page<User> userPage = adminUserService.getUserList(request);
        return Result.success(userPage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
    public Result<User> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long id) {

        User user = adminUserService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 记录查看操作
        Long adminId = getCurrentAdminId();
        adminUserService.logAdminOperation(adminId, "查看用户", "查看用户详情 - " + user.getUsername());

        return Result.success(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息", description = "更新指定用户的信息")
    public Result<User> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {

        try {
            Long adminId = getCurrentAdminId();
            User updatedUser = adminUserService.updateUser(id, request, adminId);
            return Result.success(updatedUser);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/toggle-status")
    @Operation(summary = "切换用户状态", description = "启用或禁用用户账户")
    public Result<User> toggleUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long id) {

        try {
            Long adminId = getCurrentAdminId();
            User user = adminUserService.toggleUserStatus(id, adminId);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "逻辑删除用户（可恢复）")
    public Result<String> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "是否永久删除") @RequestParam(defaultValue = "false") Boolean permanent) {

        try {
            Long adminId = getCurrentAdminId();

            if (permanent) {
                adminUserService.permanentDeleteUser(id, adminId);
                return Result.success("用户已永久删除");
            } else {
                adminUserService.deleteUser(id, adminId);
                return Result.success("用户已删除");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "获取用户统计", description = "获取用户相关的统计数据")
    public Result<UserStatsResponse> getUserStats() {
        UserStatsResponse stats = adminUserService.getUserStats();
        return Result.success(stats);
    }
}