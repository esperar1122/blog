import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import type { RegisterParams } from '@/api/auth'

export function useAuth() {
  const userStore = useUserStore()

  // 计算属性
  const isLoggedIn = computed(() => userStore.isLoggedIn)
  const isAdmin = computed(() => userStore.isAdmin)
  const userInfo = computed(() => userStore.userInfo)
  const token = computed(() => userStore.token)

  // 登录
  const login = async (credentials: { username: string; password: string }) => {
    return await userStore.doLogin(credentials)
  }

  // 注册
  const register = async (userData: RegisterParams) => {
    return await userStore.doRegister(userData)
  }

  // 登出
  const logout = async () => {
    return await userStore.doLogout()
  }

  // 获取用户信息
  const fetchUserInfo = async () => {
    return await userStore.fetchUserInfo()
  }

  // 检查用户权限
  const hasPermission = (permission: string) => {
    // TODO: 实现权限检查逻辑
    return isAdmin.value
  }

  return {
    // 状态
    isLoggedIn,
    isAdmin,
    userInfo,
    token,

    // 方法
    login,
    register,
    logout,
    fetchUserInfo,
    hasPermission
  }
}