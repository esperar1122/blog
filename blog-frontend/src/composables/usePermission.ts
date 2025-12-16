import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import type { UserRole, RouteMeta, PermissionResult } from '@/types/permission'
import { ROLE_INFO } from '@/types/permission'

/**
 * 权限管理 composable
 * 提供权限检查和验证功能
 */
export function usePermission() {
  const authStore = useAuthStore()

  /**
   * 当前用户角色
   */
  const currentUserRole = computed<UserRole>(() => {
    return authStore.user?.role as UserRole || 'USER'
  })

  /**
   * 当前用户角色信息
   */
  const currentUserRoleInfo = computed(() => {
    return ROLE_INFO[currentUserRole.value]
  })

  /**
   * 检查当前用户是否有指定角色
   */
  const hasRole = (role: UserRole): boolean => {
    return currentUserRole.value === role
  }

  /**
   * 检查当前用户是否为管理员
   */
  const isAdmin = (): boolean => {
    return hasRole('ADMIN')
  }

  /**
   * 检查当前用户是否为普通用户
   */
  const isUser = (): boolean => {
    return hasRole('USER')
  }

  /**
   * 检查当前用户是否具有所需角色之一
   */
  const hasAnyRole = (roles: UserRole[]): boolean => {
    return roles.includes(currentUserRole.value)
  }

  /**
   * 检查当前用户是否具有所有指定角色
   */
  const hasAllRoles = (roles: UserRole[]): boolean => {
    return roles.every(role => hasRole(role))
  }

  /**
   * 检查路由访问权限
   */
  const canAccessRoute = (route: RouteMeta): PermissionResult => {
    // 如果不需要认证，允许访问
    if (!route.requiresAuth) {
      return { hasPermission: true }
    }

    // 如果用户未登录，拒绝访问
    if (!authStore.isAuthenticated) {
      return {
        hasPermission: false,
        reason: '需要登录'
      }
    }

    // 如果路由有角色要求
    if (route.requiredRoles && route.requiredRoles.length > 0) {
      const hasRequiredRole = hasAnyRole(route.requiredRoles)
      if (!hasRequiredRole) {
        return {
          hasPermission: false,
          reason: `需要以下角色之一: ${route.requiredRoles.join(', ')}`
        }
      }
    }

    return { hasPermission: true }
  }

  /**
   * 检查是否可以访问管理员功能
   */
  const canAccessAdmin = (): boolean => {
    return isAdmin()
  }

  /**
   * 检查是否可以访问用户功能
   */
  const canAccessUser = (): boolean => {
    return authStore.isAuthenticated
  }

  /**
   * 根据角色过滤菜单项
   */
  const filterMenusByRole = <T extends { roles?: UserRole[] }>(menus: T[]): T[] => {
    return menus.filter(menu => {
      if (!menu.roles || menu.roles.length === 0) {
        return true
      }
      return hasAnyRole(menu.roles)
    })
  }

  /**
   * 获取角色描述
   */
  const getRoleDescription = (role: UserRole): string => {
    return ROLE_INFO[role]?.description || '未知角色'
  }

  /**
   * 获取当前用户角色描述
   */
  const getCurrentRoleDescription = (): string => {
    return getRoleDescription(currentUserRole.value)
  }

  /**
   * 比较角色权限级别
   */
  const compareRoleLevel = (role1: UserRole, role2: UserRole): number => {
    return ROLE_INFO[role1]?.level - ROLE_INFO[role2]?.level
  }

  /**
   * 检查角色1是否比角色2权限更高
   */
  const hasHigherRole = (role1: UserRole, role2: UserRole): boolean => {
    return compareRoleLevel(role1, role2) > 0
  }

  /**
   * 检查当前用户是否比指定用户权限更高
   */
  const hasHigherRoleThan = (targetRole: UserRole): boolean => {
    return hasHigherRole(currentUserRole.value, targetRole)
  }

  return {
    // 响应式数据
    currentUserRole,
    currentUserRoleInfo,

    // 权限检查方法
    hasRole,
    isAdmin,
    isUser,
    hasAnyRole,
    hasAllRoles,
    canAccessRoute,
    canAccessAdmin,
    canAccessUser,

    // 工具方法
    filterMenusByRole,
    getRoleDescription,
    getCurrentRoleDescription,
    compareRoleLevel,
    hasHigherRole,
    hasHigherRoleThan
  }
}