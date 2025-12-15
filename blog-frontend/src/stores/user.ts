import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from 'blog-shared'
import { login, logout, getUserInfo } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<User | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')

  // 登录
  async function doLogin(credentials: { username: string; password: string }) {
    try {
      const response = await login(credentials)
      token.value = response.token
      userInfo.value = response.user
      localStorage.setItem('token', response.token)
      return Promise.resolve()
    } catch (error) {
      return Promise.reject(error)
    }
  }

  // 登出
  async function doLogout() {
    try {
      await logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      token.value = null
      userInfo.value = null
      localStorage.removeItem('token')
    }
  }

  // 获取用户信息
  async function fetchUserInfo() {
    try {
      const user = await getUserInfo()
      userInfo.value = user
      return Promise.resolve(user)
    } catch (error) {
      // 如果获取用户信息失败，清除token
      token.value = null
      userInfo.value = null
      localStorage.removeItem('token')
      return Promise.reject(error)
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    doLogin,
    doLogout,
    fetchUserInfo
  }
})