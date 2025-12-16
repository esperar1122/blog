import type { UserRole, MenuItem } from '@/types/permission'

/**
 * 权限相关常量定义
 */

// 角色级别映射
export const ROLE_LEVELS = {
  [UserRole.USER]: 1,
  [UserRole.ADMIN]: 2
} as const

// 角色颜色映射
export const ROLE_COLORS = {
  [UserRole.USER]: '#409EFF', // 蓝色
  [UserRole.ADMIN]: '#F56C6C'  // 红色
} as const

// 角色标签类型映射
export const ROLE_TAG_TYPES = {
  [UserRole.USER]: 'primary',
  [UserRole.ADMIN]: 'danger'
} as const

// 权限描述
export const ROLE_DESCRIPTIONS = {
  [UserRole.USER]: '普通用户',
  [UserRole.ADMIN]: '管理员'
} as const

// 权限提示消息
export const PERMISSION_MESSAGES = {
  LOGIN_REQUIRED: '请先登录',
  ADMIN_REQUIRED: '需要管理员权限',
  USER_REQUIRED: '需要用户权限',
  PERMISSION_DENIED: '权限不足',
  ACCESS_DENIED: '访问被拒绝',
  ACTION_DENIED: '操作被拒绝'
} as const

// 路由权限映射
export const ROUTE_PERMISSIONS = {
  // 公共路由
  PUBLIC: [],

  // 用户路由
  USER: [UserRole.USER, UserRole.ADMIN],

  // 管理员路由
  ADMIN: [UserRole.ADMIN]
} as const

// 操作权限映射
export const ACTION_PERMISSIONS = {
  // 文章操作
  CREATE_ARTICLE: [UserRole.USER, UserRole.ADMIN],
  EDIT_OWN_ARTICLE: [UserRole.USER, UserRole.ADMIN],
  EDIT_ANY_ARTICLE: [UserRole.ADMIN],
  DELETE_OWN_ARTICLE: [UserRole.USER, UserRole.ADMIN],
  DELETE_ANY_ARTICLE: [UserRole.ADMIN],

  // 用户操作
  EDIT_PROFILE: [UserRole.USER, UserRole.ADMIN],
  VIEW_PROFILE: [UserRole.USER, UserRole.ADMIN],

  // 管理员操作
  MANAGE_USERS: [UserRole.ADMIN],
  MANAGE_ARTICLES: [UserRole.ADMIN],
  MANAGE_COMMENTS: [UserRole.ADMIN],
  MANAGE_CATEGORIES: [UserRole.ADMIN],
  MANAGE_TAGS: [UserRole.ADMIN],
  VIEW_ADMIN_PANEL: [UserRole.ADMIN],

  // 系统操作
  VIEW_SYSTEM_STATS: [UserRole.ADMIN],
  MANAGE_SYSTEM_SETTINGS: [UserRole.ADMIN]
} as const

// 菜单配置
export const ADMIN_MENU_ITEMS: MenuItem[] = [
  {
    path: '/admin/dashboard',
    name: '仪表盘',
    icon: 'DataBoard',
    roles: [UserRole.ADMIN]
  },
  {
    path: '/admin/users',
    name: '用户管理',
    icon: 'User',
    roles: [UserRole.ADMIN]
  },
  {
    path: '/admin/articles',
    name: '文章管理',
    icon: 'Document',
    roles: [UserRole.ADMIN]
  },
  {
    path: '/admin/comments',
    name: '评论管理',
    icon: 'ChatDotRound',
    roles: [UserRole.ADMIN]
  },
  {
    path: '/admin/categories',
    name: '分类管理',
    icon: 'FolderOpened',
    roles: [UserRole.ADMIN]
  },
  {
    path: '/admin/tags',
    name: '标签管理',
    icon: 'CollectionTag',
    roles: [UserRole.ADMIN]
  },
  {
    path: '/admin/settings',
    name: '系统设置',
    icon: 'Setting',
    roles: [UserRole.ADMIN]
  }
]

// 用户菜单配置
export const USER_MENU_ITEMS: MenuItem[] = [
  {
    path: '/profile',
    name: '个人资料',
    icon: 'User',
    roles: [UserRole.USER, UserRole.ADMIN]
  },
  {
    path: '/my-articles',
    name: '我的文章',
    icon: 'Document',
    roles: [UserRole.USER, UserRole.ADMIN]
  },
  {
    path: '/create-article',
    name: '写文章',
    icon: 'EditPen',
    roles: [UserRole.USER, UserRole.ADMIN]
  },
  {
    path: '/settings',
    name: '设置',
    icon: 'Setting',
    roles: [UserRole.USER, UserRole.ADMIN]
  }
]

// 权限检查函数类型
export type PermissionChecker = (userRole: UserRole) => boolean

// 常用权限检查函数
export const PERMISSION_CHECKERS = {
  // 检查是否为管理员
  isAdmin: (role: UserRole): boolean => role === UserRole.ADMIN,

  // 检查是否为普通用户
  isUser: (role: UserRole): boolean => role === UserRole.USER,

  // 检查是否可以访问管理员功能
  canAccessAdmin: (role: UserRole): boolean => role === UserRole.ADMIN,

  // 检查是否可以访问用户功能
  canAccessUser: (role: UserRole): boolean => [UserRole.USER, UserRole.ADMIN].includes(role),

  // 检查是否有操作权限
  hasActionPermission: (role: UserRole, action: keyof typeof ACTION_PERMISSIONS): boolean => {
    const requiredRoles = ACTION_PERMISSIONS[action]
    return requiredRoles.includes(role)
  }
} as const

// 默认权限配置
export const DEFAULT_PERMISSIONS = {
  defaultRole: UserRole.USER,
  autoAssignRole: true,
  allowRoleChange: false // 默认不允许普通用户自行修改角色
}

// 权限验证规则
export const PERMISSION_RULES = {
  // 角色提升需要管理员权限
  roleUpgradeRequiresAdmin: true,

  // 管理员不能被普通用户操作
  adminProtectedFromUser: true,

  // 检查权限时忽略大小写
  ignoreCase: false,

  // 权限检查缓存时间（毫秒）
  cacheTime: 5 * 60 * 1000 // 5分钟
}

// 错误代码映射
export const PERMISSION_ERROR_CODES = {
  UNAUTHORIZED: 'PERMISSION_UNAUTHORIZED',
  FORBIDDEN: 'PERMISSION_FORBIDDEN',
  ROLE_NOT_FOUND: 'PERMISSION_ROLE_NOT_FOUND',
  TOKEN_INVALID: 'PERMISSION_TOKEN_INVALID',
  TOKEN_EXPIRED: 'PERMISSION_TOKEN_EXPIRED',
  ACCESS_DENIED: 'PERMISSION_ACCESS_DENIED'
} as const