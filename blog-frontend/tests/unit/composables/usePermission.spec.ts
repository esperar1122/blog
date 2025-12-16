import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { usePermission } from '@/composables/usePermission'
import { useAuthStore } from '@/stores/auth'
import type { UserRole, RouteMeta } from '@/types/permission'
import { ROLE_INFO } from '@/types/permission'

// Mock auth store
vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    user: null,
    isAuthenticated: false
  })
}))

describe('usePermission', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  describe('基础权限检查', () => {
    it('应该正确检查管理员权限', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = { role: 'ADMIN' as UserRole, id: 1 }
      authStore.isAuthenticated = true

      const { isAdmin, isUser, hasRole } = usePermission()

      expect(isAdmin()).toBe(true)
      expect(isUser()).toBe(false)
      expect(hasRole('ADMIN')).toBe(true)
      expect(hasRole('USER')).toBe(false)
    })

    it('应该正确检查普通用户权限', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = { role: 'USER' as UserRole, id: 2 }
      authStore.isAuthenticated = true

      const { isAdmin, isUser, hasRole } = usePermission()

      expect(isAdmin()).toBe(false)
      expect(isUser()).toBe(true)
      expect(hasRole('USER')).toBe(true)
      expect(hasRole('ADMIN')).toBe(false)
    })

    it('应该正确处理未认证用户', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = null
      authStore.isAuthenticated = false

      const { isAdmin, isUser, hasRole } = usePermission()

      expect(isAdmin()).toBe(false)
      expect(isUser()).toBe(false)
      expect(hasRole('USER')).toBe(false)
      expect(hasRole('ADMIN')).toBe(false)
    })
  })

  describe('多角色检查', () => {
    it('应该正确检查是否具有任一角色', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = { role: 'ADMIN' as UserRole, id: 1 }
      authStore.isAuthenticated = true

      const { hasAnyRole, hasAllRoles } = usePermission()

      expect(hasAnyRole(['ADMIN', 'USER'])).toBe(true)
      expect(hasAnyRole(['USER'])).toBe(false)
      expect(hasAllRoles(['ADMIN'])).toBe(true)
      expect(hasAllRoles(['ADMIN', 'USER'])).toBe(false)
    })
  })

  describe('路由权限检查', () => {
    it('应该允许访问公共路由', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = null
      authStore.isAuthenticated = false

      const { canAccessRoute } = usePermission()

      const publicRoute: RouteMeta = {
        requiresAuth: false
      }

      const result = canAccessRoute(publicRoute)
      expect(result.hasPermission).toBe(true)
    })

    it('应该拒绝未认证用户访问受保护路由', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = null
      authStore.isAuthenticated = false

      const { canAccessRoute } = usePermission()

      const protectedRoute: RouteMeta = {
        requiresAuth: true
      }

      const result = canAccessRoute(protectedRoute)
      expect(result.hasPermission).toBe(false)
      expect(result.reason).toBe('需要登录')
    })

    it('应该拒绝普通用户访问管理员路由', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = { role: 'USER' as UserRole, id: 2 }
      authStore.isAuthenticated = true

      const { canAccessRoute } = usePermission()

      const adminRoute: RouteMeta = {
        requiresAuth: true,
        requiredRoles: ['ADMIN']
      }

      const result = canAccessRoute(adminRoute)
      expect(result.hasPermission).toBe(false)
      expect(result.reason).toContain('ADMIN')
    })

    it('应该允许管理员访问管理员路由', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = { role: 'ADMIN' as UserRole, id: 1 }
      authStore.isAuthenticated = true

      const { canAccessRoute } = usePermission()

      const adminRoute: RouteMeta = {
        requiresAuth: true,
        requiredRoles: ['ADMIN']
      }

      const result = canAccessRoute(adminRoute)
      expect(result.hasPermission).toBe(true)
    })
  })

  describe('功能权限检查', () => {
    it('应该正确检查管理员功能访问权限', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = { role: 'ADMIN' as UserRole, id: 1 }
      authStore.isAuthenticated = true

      const { canAccessAdmin, canAccessUser } = usePermission()

      expect(canAccessAdmin()).toBe(true)
      expect(canAccessUser()).toBe(true)
    })

    it('应该正确检查普通用户功能访问权限', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = { role: 'USER' as UserRole, id: 2 }
      authStore.isAuthenticated = true

      const { canAccessAdmin, canAccessUser } = usePermission()

      expect(canAccessAdmin()).toBe(false)
      expect(canAccessUser()).toBe(true)
    })
  })

  describe('菜单过滤', () => {
    it('应该根据角色过滤菜单', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = { role: 'ADMIN' as UserRole, id: 1 }
      authStore.isAuthenticated = true

      const { filterMenusByRole } = usePermission()

      const menus = [
        { path: '/public', name: '公共菜单' },
        { path: '/user', name: '用户菜单', roles: ['USER'] },
        { path: '/admin', name: '管理员菜单', roles: ['ADMIN'] }
      ]

      const filteredMenus = filterMenusByRole(menus)
      expect(filteredMenus).toHaveLength(2) // 公共菜单 + 管理员菜单
      expect(filteredMenus.some(m => m.path === '/admin')).toBe(true)
      expect(filteredMenus.some(m => m.path === '/user')).toBe(false)
    })
  })

  describe('角色信息', () => {
    it('应该正确获取角色描述', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = { role: 'ADMIN' as UserRole, id: 1 }
      authStore.isAuthenticated = true

      const { getRoleDescription, getCurrentRoleDescription, currentUserRoleInfo } = usePermission()

      expect(getRoleDescription('ADMIN')).toBe('管理员')
      expect(getRoleDescription('USER')).toBe('普通用户')
      expect(getCurrentRoleDescription()).toBe('管理员')
      expect(currentUserRoleInfo.value.description).toBe('管理员')
      expect(currentUserRoleInfo.value.level).toBe(2)
    })
  })

  describe('角色级别比较', () => {
    it('应该正确比较角色权限级别', () => {
      const { compareRoleLevel, hasHigherRole } = usePermission()

      expect(compareRoleLevel('ADMIN', 'USER')).toBeGreaterThan(0)
      expect(compareRoleLevel('USER', 'ADMIN')).toBeLessThan(0)
      expect(compareRoleLevel('ADMIN', 'ADMIN')).toBe(0)

      expect(hasHigherRole('ADMIN', 'USER')).toBe(true)
      expect(hasHigherRole('USER', 'ADMIN')).toBe(false)
      expect(hasHigherRole('ADMIN', 'ADMIN')).toBe(false)
    })

    it('应该正确检查当前用户是否比目标用户权限更高', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = { role: 'ADMIN' as UserRole, id: 1 }
      authStore.isAuthenticated = true

      const { hasHigherRoleThan } = usePermission()

      expect(hasHigherRoleThan('USER')).toBe(true)
      expect(hasHigherRoleThan('ADMIN')).toBe(false)
    })
  })

  describe('响应式数据', () => {
    it('应该正确响应角色变化', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = { role: 'USER' as UserRole, id: 2 }
      authStore.isAuthenticated = true

      const { currentUserRole, currentUserRoleInfo } = usePermission()

      expect(currentUserRole.value).toBe('USER')
      expect(currentUserRoleInfo.value.level).toBe(1)

      // 模拟角色变化
      authStore.user = { role: 'ADMIN' as UserRole, id: 2 }

      // 由于是响应式的，需要重新创建composable来获取更新后的值
      const { currentUserRole: updatedRole, currentUserRoleInfo: updatedRoleInfo } = usePermission()
      expect(updatedRole.value).toBe('ADMIN')
      expect(updatedRoleInfo.value.level).toBe(2)
    })
  })

  describe('边界情况', () => {
    it('应该处理空的角色数组', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = { role: 'USER' as UserRole, id: 2 }
      authStore.isAuthenticated = true

      const { hasAnyRole, hasAllRoles } = usePermission()

      expect(hasAnyRole([])).toBe(false)
      expect(hasAllRoles([])).toBe(true) // 空数组默认返回true
    })

    it('应该处理未定义的角色', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = { role: undefined }
      authStore.isAuthenticated = true

      const { currentUserRole, isAdmin, isUser } = usePermission()

      expect(currentUserRole.value).toBe('USER') // 默认角色
      expect(isAdmin()).toBe(false)
      expect(isUser()).toBe(true) // 默认为普通用户
    })

    it('应该处理没有角色要求的路由', () => {
      const authStore = vi.mocked(useAuthStore)
      authStore.user = null
      authStore.isAuthenticated = false

      const { canAccessRoute } = usePermission()

      const routeWithoutRequirements: RouteMeta = {}

      const result = canAccessRoute(routeWithoutRequirements)
      expect(result.hasPermission).toBe(true)
    })
  })
})