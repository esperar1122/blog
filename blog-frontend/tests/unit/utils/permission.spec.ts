import { describe, it, expect, beforeEach, vi } from 'vitest'
import {
  hasRole,
  hasAnyRole,
  hasAllRoles,
  isAdmin,
  isUser,
  compareRoleLevel,
  hasHigherRole,
  canManageUser,
  getRoleDescription,
  getRoleColor,
  hasActionPermission,
  canAccessRoute,
  filterMenusByRole,
  getPermissionMessage,
  isValidRole,
  getHighestRole,
  checkPermissionScope,
  detailedPermissionCheck,
  cachedPermissionCheck,
  clearPermissionCache
} from '@/utils/permission'
import type { UserRole } from '@/types/permission'

describe('权限工具函数', () => {
  beforeEach(() => {
    clearPermissionCache()
  })

  describe('基础角色检查', () => {
    it('应该正确检查单个角色', () => {
      expect(hasRole('ADMIN', 'ADMIN')).toBe(true)
      expect(hasRole('ADMIN', 'USER')).toBe(false)
      expect(hasRole('USER', 'USER')).toBe(true)
      expect(hasRole('USER', 'ADMIN')).toBe(false)
    })

    it('应该正确检查多个角色', () => {
      expect(hasAnyRole('ADMIN', ['ADMIN', 'USER'])).toBe(true)
      expect(hasAnyRole('USER', ['ADMIN', 'USER'])).toBe(true)
      expect(hasAnyRole('ADMIN', ['USER'])).toBe(false)

      expect(hasAllRoles('ADMIN', ['ADMIN'])).toBe(true)
      expect(hasAllRoles('ADMIN', ['ADMIN', 'USER'])).toBe(false)
    })

    it('应该正确识别管理员和普通用户', () => {
      expect(isAdmin('ADMIN')).toBe(true)
      expect(isAdmin('USER')).toBe(false)
      expect(isUser('USER')).toBe(true)
      expect(isUser('ADMIN')).toBe(false)
    })
  })

  describe('角色级别比较', () => {
    it('应该正确比较角色级别', () => {
      expect(compareRoleLevel('ADMIN', 'USER')).toBe(1)
      expect(compareRoleLevel('USER', 'ADMIN')).toBe(-1)
      expect(compareRoleLevel('ADMIN', 'ADMIN')).toBe(0)
      expect(compareRoleLevel('USER', 'USER')).toBe(0)
    })

    it('应该正确判断更高权限', () => {
      expect(hasHigherRole('ADMIN', 'USER')).toBe(true)
      expect(hasHigherRole('USER', 'ADMIN')).toBe(false)
      expect(hasHigherRole('ADMIN', 'ADMIN')).toBe(false)
    })

    it('应该正确判断用户管理权限', () => {
      expect(canManageUser('ADMIN', 'USER')).toBe(true)
      expect(canManageUser('USER', 'ADMIN')).toBe(false)
      expect(canManageUser('USER', 'USER')).toBe(false)
      expect(canManageUser('ADMIN', 'ADMIN')).toBe(false)
    })
  })

  describe('角色信息获取', () => {
    it('应该正确获取角色描述', () => {
      expect(getRoleDescription('ADMIN')).toBe('管理员')
      expect(getRoleDescription('USER')).toBe('普通用户')
      expect(getRoleDescription('UNKNOWN' as UserRole)).toBe('未知角色')
    })

    it('应该正确获取角色颜色', () => {
      expect(getRoleColor('ADMIN')).toBe('#F56C6C')
      expect(getRoleColor('USER')).toBe('#409EFF')
      expect(getRoleColor('UNKNOWN' as UserRole)).toBe('#909399')
    })
  })

  describe('操作权限检查', () => {
    it('应该正确检查文章操作权限', () => {
      expect(hasActionPermission('USER', 'CREATE_ARTICLE')).toBe(true)
      expect(hasActionPermission('ADMIN', 'CREATE_ARTICLE')).toBe(true)
      expect(hasActionPermission('USER', 'MANAGE_USERS')).toBe(false)
      expect(hasActionPermission('ADMIN', 'MANAGE_USERS')).toBe(true)
    })
  })

  describe('路由权限检查', () => {
    it('应该正确检查路由访问权限', () => {
      // 这个测试需要根据实际的路由权限配置来调整
      expect(canAccessRoute('USER', '/profile')).toBe(true)
      expect(canAccessRoute('USER', '/admin')).toBe(false)
      expect(canAccessRoute('ADMIN', '/admin')).toBe(true)
    })
  })

  describe('菜单过滤', () => {
    it('应该正确过滤菜单项', () => {
      const menus = [
        { path: '/public', name: '公共菜单' },
        { path: '/user', name: '用户菜单', roles: ['USER'] },
        { path: '/admin', name: '管理员菜单', roles: ['ADMIN'] },
        { path: '/both', name: '通用菜单', roles: ['USER', 'ADMIN'] }
      ]

      const userMenus = filterMenusByRole('USER', menus)
      expect(userMenus).toHaveLength(3)
      expect(userMenus.some(m => m.path === '/admin')).toBe(false)

      const adminMenus = filterMenusByRole('ADMIN', menus)
      expect(adminMenus).toHaveLength(4)
      expect(adminMenus.some(m => m.path === '/admin')).toBe(true)
    })
  })

  describe('权限消息', () => {
    it('应该正确获取权限消息', () => {
      expect(getPermissionMessage('LOGIN_REQUIRED')).toBe('请先登录')
      expect(getPermissionMessage('ADMIN_REQUIRED')).toBe('需要管理员权限')
      expect(getPermissionMessage('UNKNOWN' as any)).toBe('权限错误')
    })
  })

  describe('角色验证', () => {
    it('应该正确验证角色有效性', () => {
      expect(isValidRole('ADMIN')).toBe(true)
      expect(isValidRole('USER')).toBe(true)
      expect(isValidRole('INVALID')).toBe(false)
      expect(isValidRole('')).toBe(false)
    })
  })

  describe('最高角色获取', () => {
    it('应该正确获取最高角色', () => {
      expect(getHighestRole(['USER', 'ADMIN'])).toBe('ADMIN')
      expect(getHighestRole(['USER'])).toBe('USER')
      expect(getHighestRole([])).toBe('USER') // 默认角色
    })
  })

  describe('权限范围检查', () => {
    it('应该正确检查权限范围', () => {
      // 管理员可以操作所有资源
      expect(checkPermissionScope('ADMIN', 1, 2)).toBe(true)
      expect(checkPermissionScope('ADMIN', 1, 1)).toBe(true)

      // 普通用户只能操作自己的资源
      expect(checkPermissionScope('USER', 1, 1)).toBe(true)
      expect(checkPermissionScope('USER', 1, 2)).toBe(false)

      // 测试管理员覆盖开关
      expect(checkPermissionScope('ADMIN', 1, 2, false)).toBe(false)
    })
  })

  describe('详细权限检查', () => {
    it('应该正确进行详细权限检查', () => {
      // 检查操作权限
      const actionResult = detailedPermissionCheck('USER', [], 'CREATE_ARTICLE')
      expect(actionResult.hasPermission).toBe(true)
      expect(actionResult.currentRole).toBe('USER')

      // 检查角色权限
      const roleResult = detailedPermissionCheck('USER', ['USER'])
      expect(roleResult.hasPermission).toBe(true)

      // 检查失败情况
      const failResult = detailedPermissionCheck('USER', ['ADMIN'], 'MANAGE_USERS')
      expect(failResult.hasPermission).toBe(false)
      expect(failResult.reason).toContain('权限不足')
      expect(failResult.requiredRole).toBe('ADMIN')
    })
  })

  describe('缓存权限检查', () => {
    it('应该正确缓存权限检查结果', () => {
      let callCount = 0
      const checker = () => {
        callCount++
        return true
      }

      // 第一次调用
      expect(cachedPermissionCheck('test-key', checker)).toBe(true)
      expect(callCount).toBe(1)

      // 第二次调用应该使用缓存
      expect(cachedPermissionCheck('test-key', checker)).toBe(true)
      expect(callCount).toBe(1)

      // 不同的key应该重新调用
      expect(cachedPermissionCheck('test-key-2', checker)).toBe(true)
      expect(callCount).toBe(2)
    })

    it('应该正确清除缓存', () => {
      let callCount = 0
      const checker = () => {
        callCount++
        return true
      }

      // 建立缓存
      cachedPermissionCheck('test-key', checker)
      expect(callCount).toBe(1)

      // 清除缓存后应该重新调用
      clearPermissionCache()
      cachedPermissionCheck('test-key', checker)
      expect(callCount).toBe(2)
    })
  })

  describe('边界情况', () => {
    it('应该处理空数组', () => {
      expect(hasAnyRole('USER', [])).toBe(false)
      expect(hasAllRoles('USER', [])).toBe(true)
      expect(filterMenusByRole('USER', [])).toEqual([])
    })

    it('应该处理无效输入', () => {
      expect(getRoleDescription(null as any)).toBe('未知角色')
      expect(getRoleColor(undefined as any)).toBe('#909399')
    })
  })
})