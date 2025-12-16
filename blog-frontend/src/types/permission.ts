/**
 * 权限相关类型定义
 */

/**
 * 用户角色枚举
 */
export enum UserRole {
  USER = 'USER',
  ADMIN = 'ADMIN'
}

/**
 * 角色信息映射
 */
export const ROLE_INFO = {
  [UserRole.USER]: {
    code: UserRole.USER,
    description: '普通用户',
    level: 1
  },
  [UserRole.ADMIN]: {
    code: UserRole.ADMIN,
    description: '管理员',
    level: 2
  }
} as const;

/**
 * 权限检查函数类型
 */
export type PermissionChecker = (userRole: UserRole) => boolean;

/**
 * 路由元信息接口
 */
export interface RouteMeta {
  requiresAuth?: boolean;
  requiredRoles?: UserRole[];
  layout?: string;
  title?: string;
  icon?: string;
  hidden?: boolean;
}

/**
 * 菜单项接口
 */
export interface MenuItem {
  path: string;
  name: string;
  icon?: string;
  children?: MenuItem[];
  roles?: UserRole[];
  hidden?: boolean;
}

/**
 * 用户权限信息接口
 */
export interface UserPermission {
  id: number;
  username: string;
  role: UserRole;
  permissions: string[];
}

/**
 * 权限验证结果接口
 */
export interface PermissionResult {
  hasPermission: boolean;
  reason?: string;
}