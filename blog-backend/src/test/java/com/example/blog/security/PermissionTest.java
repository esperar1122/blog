package com.example.blog.security;

import com.example.blog.enums.UserRole;
import com.example.blog.security.CustomUserDetails;
import com.example.blog.util.PermissionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 权限测试类
 */
@ExtendWith(MockitoExtension.class)
public class PermissionTest {

    private CustomUserDetails adminUser;
    private CustomUserDetails regularUser;
    private Authentication adminAuth;
    private Authentication userAuth;

    @BeforeEach
    void setUp() {
        // 创建管理员用户
        adminUser = new CustomUserDetails() {
            @Override
            public Long getId() { return 1L; }

            @Override
            public String getUsername() { return "admin"; }

            @Override
            public String getPassword() { return "password"; }

            @Override
            public UserRole getRole() { return UserRole.ADMIN; }

            @Override
            public boolean isAdmin() { return true; }

            @Override
            public boolean isUser() { return false; }

            @Override
            public boolean isEnabled() { return true; }

            @Override
            public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
        };

        // 创建普通用户
        regularUser = new CustomUserDetails() {
            @Override
            public Long getId() { return 2L; }

            @Override
            public String getUsername() { return "user"; }

            @Override
            public String getPassword() { return "password"; }

            @Override
            public UserRole getRole() { return UserRole.USER; }

            @Override
            public boolean isAdmin() { return false; }

            @Override
            public boolean isUser() { return true; }

            @Override
            public boolean isEnabled() { return true; }

            @Override
            public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
                return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            }
        };

        // 创建认证对象
        adminAuth = new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities());
        userAuth = new UsernamePasswordAuthenticationToken(regularUser, null, regularUser.getAuthorities());
    }

    @Test
    void testAdminHasAdminRole() {
        assertTrue(adminUser.isAdmin());
        assertFalse(adminUser.isUser());
        assertEquals(UserRole.ADMIN, adminUser.getRole());
    }

    @Test
    void testRegularUserHasUserRole() {
        assertTrue(regularUser.isUser());
        assertFalse(regularUser.isAdmin());
        assertEquals(UserRole.USER, regularUser.getRole());
    }

    @Test
    void testPermissionUtilsWithAdminUser() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(adminAuth);
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(context);

            // 测试管理员权限检查
            assertTrue(PermissionUtils.isCurrentUserAdmin());
            assertFalse(PermissionUtils.isCurrentUserUser());
            assertTrue(PermissionUtils.hasRole(UserRole.ADMIN));
            assertFalse(PermissionUtils.hasRole(UserRole.USER));
            assertTrue(PermissionUtils.isAuthenticated());
            assertEquals(1L, PermissionUtils.getCurrentUserId());
            assertEquals(UserRole.ADMIN, PermissionUtils.getCurrentUserRole());
        }
    }

    @Test
    void testPermissionUtilsWithRegularUser() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(userAuth);
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(context);

            // 测试普通用户权限检查
            assertFalse(PermissionUtils.isCurrentUserAdmin());
            assertTrue(PermissionUtils.isCurrentUserUser());
            assertFalse(PermissionUtils.hasRole(UserRole.ADMIN));
            assertTrue(PermissionUtils.hasRole(UserRole.USER));
            assertTrue(PermissionUtils.isAuthenticated());
            assertEquals(2L, PermissionUtils.getCurrentUserId());
            assertEquals(UserRole.USER, PermissionUtils.getCurrentUserRole());
        }
    }

    @Test
    void testPermissionUtilsWithoutAuthentication() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(null);
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(context);

            // 测试未认证状态
            assertFalse(PermissionUtils.isCurrentUserAdmin());
            assertFalse(PermissionUtils.isCurrentUserUser());
            assertFalse(PermissionUtils.hasRole(UserRole.ADMIN));
            assertFalse(PermissionUtils.hasRole(UserRole.USER));
            assertFalse(PermissionUtils.isAuthenticated());
            assertNull(PermissionUtils.getCurrentUserId());
            assertNull(PermissionUtils.getCurrentUserRole());
        }
    }

    @Test
    void testCanAccessUserResource_AdminCanAccessAllResources() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(adminAuth);
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(context);

            // 管理员可以访问所有用户资源
            assertTrue(PermissionUtils.canAccessUserResource(1L)); // 自己的资源
            assertTrue(PermissionUtils.canAccessUserResource(2L)); // 其他用户的资源
            assertTrue(PermissionUtils.canAccessUserResource(999L)); // 不存在的用户资源
        }
    }

    @Test
    void testCanAccessUserResource_RegularUserCanAccessOwnResources() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(userAuth);
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(context);

            // 普通用户只能访问自己的资源
            assertTrue(PermissionUtils.canAccessUserResource(2L)); // 自己的资源
            assertFalse(PermissionUtils.canAccessUserResource(1L)); // 其他用户的资源
            assertFalse(PermissionUtils.canAccessUserResource(999L)); // 不存在的用户资源
        }
    }

    @Test
    void testRequireRole_AdminSuccess() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(adminAuth);
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(context);

            // 管理员要求管理员角色应该成功
            assertDoesNotThrow(() -> PermissionUtils.requireRole(UserRole.ADMIN));
        }
    }

    @Test
    void testRequireRole_RegularUserFails() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(userAuth);
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(context);

            // 普通用户要求管理员角色应该失败
            SecurityException exception = assertThrows(
                SecurityException.class,
                () -> PermissionUtils.requireRole(UserRole.ADMIN)
            );
            assertTrue(exception.getMessage().contains("需要 管理员 权限"));
        }
    }

    @Test
    void testRequireAdmin_AdminSuccess() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(adminAuth);
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(context);

            // 管理员调用requireAdmin应该成功
            assertDoesNotThrow(() -> PermissionUtils.requireAdmin());
        }
    }

    @Test
    void testRequireAdmin_RegularUserFails() {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContext = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            when(context.getAuthentication()).thenReturn(userAuth);
            mockedSecurityContext.when(SecurityContextHolder::getContext).thenReturn(context);

            // 普通用户调用requireAdmin应该失败
            SecurityException exception = assertThrows(
                SecurityException.class,
                () -> PermissionUtils.requireAdmin()
            );
            assertEquals("需要管理员权限", exception.getMessage());
        }
    }

    @Test
    void testUserRoleEnum() {
        // 测试角色枚举
        assertEquals("USER", UserRole.USER.getCode());
        assertEquals("ADMIN", UserRole.ADMIN.getCode());
        assertEquals("普通用户", UserRole.USER.getDescription());
        assertEquals("管理员", UserRole.ADMIN.getDescription());

        // 测试fromCode方法
        assertEquals(UserRole.USER, UserRole.fromCode("USER"));
        assertEquals(UserRole.ADMIN, UserRole.fromCode("ADMIN"));

        // 测试fromCode异常
        assertThrows(IllegalArgumentException.class, () -> UserRole.fromCode("INVALID"));

        // 测试角色检查方法
        assertTrue(UserRole.USER.isUser());
        assertFalse(UserRole.USER.isAdmin());
        assertFalse(UserRole.ADMIN.isUser());
        assertTrue(UserRole.ADMIN.isAdmin());
    }

    @Test
    void testCustomUserDetailsAuthorities() {
        // 测试用户权限
        assertTrue(adminUser.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));

        assertTrue(regularUser.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));

        assertFalse(adminUser.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));

        assertFalse(regularUser.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void testCustomUserDetailsSecurityMethods() {
        // 测试安全相关方法
        assertTrue(adminUser.isAccountNonExpired());
        assertTrue(adminUser.isAccountNonLocked());
        assertTrue(adminUser.isCredentialsNonExpired());
        assertTrue(adminUser.isEnabled());

        assertTrue(regularUser.isAccountNonExpired());
        assertTrue(regularUser.isAccountNonLocked());
        assertTrue(regularUser.isCredentialsNonExpired());
        assertTrue(regularUser.isEnabled());
    }
}