import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from 'blog-shared'
import { authService } from '@/services/authService'
import type { RegisterParams, LoginParams } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<User | null>(null)

  // 计算属性
  const isLoggedIn = computed(() => {
    return authService.isAuthenticated()
  })

  const isAdmin = computed(() => {
    return userInfo.value?.role === 'admin'
  })

  const currentUserName = computed(() => {
    return userInfo.value?.nickname || userInfo.value?.username || ''
  })

  // 初始化用户信息
  async function initializeUser() {
    try {
      const user = authService.getUserInfo()
      if (user) {
        userInfo.value = user
      } else {
        const currentUser = await authService.getCurrentUser()
        if (currentUser) {
          userInfo.value = currentUser
        }
      }
    } catch (error) {
      console.error('初始化用户信息失败:', error)
      userInfo.value = null
    }
  }

  // 登录
  async function login(credentials: LoginParams) {
    try {
      const response = await authService.login(credentials)
      userInfo.value = response.user
      return response
    } catch (error) {
      userInfo.value = null
      throw error
    }
  }

  // 注册
  async function register(credentials: RegisterParams) {
    try {
      const response = await authService.login(credentials)
      userInfo.value = response.user
      return response
    } catch (error) {
      userInfo.value = null
      throw error
    }
  }

  // 登出
  async function logout() {
    try {
      await authService.logout()
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      userInfo.value = null
    }
  }

  // 强制登出（用于处理认证错误）
  function forceLogout() {
    authService.forceLogout()
    userInfo.value = null
  }

  // 获取用户信息
  async function fetchUserInfo() {
    try {
      const user = await authService.getCurrentUser()
      userInfo.value = user
      return user
    } catch (error) {
      userInfo.value = null
      throw error
    }
  }

  // 更新用户信息
  function updateUserInfo(user: User) {
    userInfo.value = user
  }

  // 检查用户权限
  function hasPermission(permission: string): boolean {
    if (!userInfo.value) return false

    // 管理员拥有所有权限
    if (userInfo.value.role === 'admin') return true

    // 根据实际权限系统实现
    // 这里只是示例
    return false
  }

  // 检查用户是否可以编辑文章
  function canEditArticle(articleAuthorId: number): boolean {
    if (!userInfo.value) return false

    // 管理员可以编辑所有文章
    if (userInfo.value.role === 'admin') return true

    // 作者可以编辑自己的文章
    return userInfo.value.id === articleAuthorId
  }

  return {
    // 状态
    userInfo,

    // 计算属性
    isLoggedIn,
    isAdmin,
    currentUserName,

    // 方法
    initializeUser,
    login,
    register,
    logout,
    forceLogout,
    fetchUserInfo,
    updateUserInfo,
    hasPermission,
    canEditArticle
  }
})