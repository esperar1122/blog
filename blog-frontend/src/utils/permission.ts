import type { UserRole } from '@/types/permission'
import {
  ROLE_LEVELS,
  ROLE_DESCRIPTIONS,
  ROLE_COLORS,
  ROLE_TAG_TYPES,
  PERMISSION_CHECKERS,
  ACTION_PERMISSIONS,
  PERMISSION_MESSAGES,
  ROUTE_PERMISSIONS
} from '@/constants/permission'

/**
 * 权限工具函数
 */

/**
 * 检查用户是否有指定角色
 */
export function hasRole(userRole: UserRole, requiredRole: UserRole): boolean {
  return userRole === requiredRole
}

/**
 * 检查用户是否有任一指定角色
 */
export function hasAnyRole(userRole: UserRole, roles: UserRole[]): boolean {
  return roles.includes(userRole)
}

/**
 * 检查用户是否有所有指定角色
 */
export function hasAllRoles(userRole: UserRole, roles: UserRole[]): boolean {
  return roles.every(role => userRole === role)
}

/**
 * 检查用户是否为管理员
 */
export function isAdmin(userRole: UserRole): boolean {
  return PERMISSION_CHECKERS.isAdmin(userRole)
}

/**
 * 检查用户是否为普通用户
 */
export function isUser(userRole: UserRole): boolean {
  return PERMISSION_CHECKERS.isUser(userRole)
}

/**
 * 比较角色权限级别
 */
export function compareRoleLevel(role1: UserRole, role2: UserRole): number {
  return ROLE_LEVELS[role1] - ROLE_LEVELS[role2]
}

/**
 * 检查角色1是否比角色2权限更高
 */
export function hasHigherRole(role1: UserRole, role2: UserRole): boolean {
  return compareRoleLevel(role1, role2) > 0
}

/**
 * 检查当前用户是否比目标用户权限更高
 */
export function canManageUser(currentUserRole: UserRole, targetUserRole: UserRole): boolean {
  // 不能管理自己或权限更高的用户
  return hasHigherRole(currentUserRole, targetUserRole)
}

/**
 * 获取角色描述
 */
export function getRoleDescription(role: UserRole): string {
  return ROLE_DESCRIPTIONS[role] || '未知角色'
}

/**
 * 获取角色颜色
 */
export function getRoleColor(role: UserRole): string {
  return ROLE_COLORS[role] || '#909399'
}

/**
 * 获取角色标签类型
 */
export function getRoleTagType(role: UserRole): string {
  return ROLE_TAG_TYPES[role] || 'info'
}

/**
 * 检查是否有操作权限
 */
export function hasActionPermission(
  userRole: UserRole,
  action: keyof typeof ACTION_PERMISSIONS
): boolean {
  return PERMISSION_CHECKERS.hasActionPermission(userRole, action)
}

/**
 * 检查是否可以访问路由
 */
export function canAccessRoute(userRole: UserRole, routePath: string): boolean {
  // 查找匹配的路由权限配置
  for (const [routeType, requiredRoles] of Object.entries(ROUTE_PERMISSIONS)) {
    if (requiredRoles.length === 0 || requiredRoles.includes(userRole)) {
      return true
    }
  }
  return false
}

/**
 * 根据角色过滤菜单项
 */
export function filterMenusByRole<T extends { roles?: UserRole[] }>(
  userRole: UserRole,
  menus: T[]
): T[] {
  return menus.filter(menu => {
    if (!menu.roles || menu.roles.length === 0) {
      return true
    }
    return hasAnyRole(userRole, menu.roles)
  })
}

/**
 * 获取权限错误消息
 */
export function getPermissionMessage(type: keyof typeof PERMISSION_MESSAGES): string {
  return PERMISSION_MESSAGES[type] || '权限错误'
}

/**
 * 验证角色有效性
 */
export function isValidRole(role: string): role is UserRole {
  return Object.values(UserRole).includes(role as UserRole)
}

/**
 * 获取最高角色
 */
export function getHighestRole(roles: UserRole[]): UserRole {
  if (roles.length === 0) {
    return UserRole.USER
  }

  return roles.reduce((highest, current) => {
    return hasHigherRole(current, highest) ? current : highest
  })
}

/**
 * 检查权限范围
 */
export function checkPermissionScope(
  currentUserRole: UserRole,
  targetUserId: number,
  resourceUserId: number,
  adminOverride: boolean = true
): boolean {
  // 管理员可以操作所有资源（如果允许管理员覆盖）
  if (adminOverride && isAdmin(currentUserRole)) {
    return true
  }

  // 普通用户只能操作自己的资源
  return currentUserRole === UserRole.USER && targetUserId === resourceUserId
}

/**
 * 生成权限标识符
 */
export function generatePermissionId(module: string, action: string): string {
  return `${module}:${action}`
}

/**
 * 解析权限标识符
 */
export function parsePermissionId(permissionId: string): { module: string; action: string } {
  const [module, action] = permissionId.split(':')
  return { module, action: action || '' }
}

/**
 * 权限检查结果接口
 */
export interface PermissionCheckResult {
  hasPermission: boolean
  reason?: string
  requiredRole?: UserRole
  currentRole?: UserRole
}

/**
 * 详细权限检查
 */
export function detailedPermissionCheck(
  userRole: UserRole,
  requiredRoles?: UserRole[],
  action?: keyof typeof ACTION_PERMISSIONS
): PermissionCheckResult {
  const result: PermissionCheckResult = {
    hasPermission: true,
    currentRole: userRole
  }

  // 检查操作权限
  if (action && !hasActionPermission(userRole, action)) {
    result.hasPermission = false
    result.reason = getPermissionMessage('PERMISSION_DENIED')
    result.requiredRole = getHighestRole(ACTION_PERMISSIONS[action])
    return result
  }

  // 检查角色权限
  if (requiredRoles && requiredRoles.length > 0) {
    if (!hasAnyRole(userRole, requiredRoles)) {
      result.hasPermission = false
      result.reason = getPermissionMessage('PERMISSION_DENIED')
      result.requiredRole = getHighestRole(requiredRoles)
      return result
    }
  }

  return result
}

/**
 * 权限缓存接口
 */
interface PermissionCache {
  [key: string]: {
    result: boolean
    timestamp: number
  }
}

const permissionCache: PermissionCache = {}

/**
 * 带缓存的权限检查
 */
export function cachedPermissionCheck(
  key: string,
  checker: () => boolean,
  cacheTime: number = 5 * 60 * 1000 // 5分钟
): boolean {
  const cached = permissionCache[key]
  const now = Date.now()

  if (cached && (now - cached.timestamp) < cacheTime) {
    return cached.result
  }

  const result = checker()
  permissionCache[key] = {
    result,
    timestamp: now
  }

  return result
}

/**
 * 清除权限缓存
 */
export function clearPermissionCache(): void {
  Object.keys(permissionCache).forEach(key => {
    delete permissionCache[key]
  })
}

/**
 * 清除过期的权限缓存
 */
export function clearExpiredPermissionCache(maxAge: number = 5 * 60 * 1000): void {
  const now = Date.now()
  Object.keys(permissionCache).forEach(key => {
    if (now - permissionCache[key].timestamp > maxAge) {
      delete permissionCache[key]
    }
  })
}