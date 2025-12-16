import type { Router } from 'vue-router'
import { authService } from '@/services/authService'
import { usePermission } from '@/composables/usePermission'
import { ElMessage } from 'element-plus'
import type { UserRole } from '@/types/permission'

// 路由权限配置
interface RoutePermission {
  path: string
  requiresAuth?: boolean
  requiredRoles?: UserRole[]
}

// 路由权限映射
const routePermissions: RoutePermission[] = [
  // 公共路由（不需要认证）
  { path: '/', requiresAuth: false },
  { path: '/login', requiresAuth: false },
  { path: '/register', requiresAuth: false },
  { path: '/article', requiresAuth: false },
  { path: '/about', requiresAuth: false },

  // 用户路由（需要认证）
  { path: '/profile', requiresAuth: true },
  { path: '/create-article', requiresAuth: true },
  { path: '/edit', requiresAuth: true },
  { path: '/settings', requiresAuth: true },

  // 管理员路由（需要管理员权限）
  { path: '/admin', requiresAuth: true, requiredRoles: [UserRole.ADMIN] }
]

// 便捷的路由分组
const publicRoutes = routePermissions
  .filter(route => !route.requiresAuth)
  .map(route => route.path)

const protectedRoutes = routePermissions
  .filter(route => route.requiresAuth)
  .map(route => route.path)

const adminRoutes = routePermissions
  .filter(route => route.requiredRoles?.includes(UserRole.ADMIN))
  .map(route => route.path)

export function setupAuthGuards(router: Router) {
  router.beforeEach(async (to, from, next) => {
    try {
      // 初始化认证状态
      const isAuthenticated = await authService.initializeAuth()
      const { canAccessRoute } = usePermission()

      // 查找匹配的路由权限配置
      const routePermission = findMatchingRoutePermission(to.path)

      console.log(`路由导航: ${to.path}, 认证状态: ${isAuthenticated}, 路由权限:`, routePermission)

      if (!routePermission) {
        // 未找到路由权限配置，默认允许访问
        next()
        return
      }

      // 使用权限检查函数
      const permissionResult = canAccessRoute(routePermission)

      if (!permissionResult.hasPermission) {
        if (!isAuthenticated) {
          // 未认证，重定向到登录页
          ElMessage.warning('请先登录')
          next({
            path: '/login',
            query: { redirect: to.fullPath }
          })
        } else {
          // 已认证但权限不足
          ElMessage.error(permissionResult.reason || '权限不足')
          // 重定向到首页或上一页
          next(from.path || '/')
        }
        return
      }

      // 权限检查通过
      next()
    } catch (error) {
      console.error('路由守卫错误:', error)
      ElMessage.error('路由验证失败')

      // 默认重定向到首页
      next('/')
    }
  })

  router.onError((error) => {
    console.error('路由错误:', error)
    ElMessage.error('页面加载失败，请重试')
  })
}

/**
 * 查找匹配的路由权限配置
 */
function findMatchingRoutePermission(path: string): RoutePermission | null {
  // 按路径长度排序，确保优先匹配更具体的路径
  const sortedPermissions = [...routePermissions].sort((a, b) => {
    return b.path.length - a.path.length
  })

  for (const permission of sortedPermissions) {
    if (permission.path === '/') {
      if (path === permission.path) {
        return permission
      }
    } else if (path.startsWith(permission.path)) {
      return permission
    }
  }

  return null
}