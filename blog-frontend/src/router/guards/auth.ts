import type { Router } from 'vue-router'
import { authService } from '@/services/authService'
import { ElMessage } from 'element-plus'

// 需要认证的路由列表
const protectedRoutes = [
  '/profile',
  '/create-article',
  '/edit',
  '/admin',
  '/settings'
]

// 不需要认证的路由列表（白名单）
const publicRoutes = [
  '/login',
  '/register',
  '/',
  '/article',
  '/about'
]

export function setupAuthGuards(router: Router) {
  router.beforeEach(async (to, from, next) => {
    try {
      // 初始化认证状态
      const isAuthenticated = await authService.initializeAuth()

      // 检查是否为公共路由
      const isPublicRoute = publicRoutes.some(route => {
        if (route === '/') {
          return to.path === route
        }
        return to.path.startsWith(route)
      })

      // 检查是否为受保护的路由
      const isProtectedRoute = protectedRoutes.some(route =>
        to.path.startsWith(route)
      )

      console.log(`路由导航: ${to.path}, 认证状态: ${isAuthenticated}, 公共路由: ${isPublicRoute}, 受保护路由: ${isProtectedRoute}`)

      if (isPublicRoute) {
        // 公共路由直接通过
        next()
      } else if (isProtectedRoute && !isAuthenticated) {
        // 受保护的路由但未认证，重定向到登录页
        ElMessage.warning('请先登录')
        next({
          path: '/login',
          query: { redirect: to.fullPath }
        })
      } else {
        // 已认证访问受保护路由，或访问其他路由
        next()
      }
    } catch (error) {
      console.error('路由守卫错误:', error)

      // 如果是受保护的路由，重定向到登录页
      const isProtectedRoute = protectedRoutes.some(route =>
        to.path.startsWith(route)
      )

      if (isProtectedRoute) {
        next({
          path: '/login',
          query: { redirect: to.fullPath }
        })
      } else {
        next()
      }
    }
  })

  router.onError((error) => {
    console.error('路由错误:', error)
    ElMessage.error('页面加载失败，请重试')
  })
}