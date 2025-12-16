import { login, logout, getUserInfo, refreshToken, type LoginParams } from '@/api/auth'
import type { User } from 'blog-shared'
import { ElMessage } from 'element-plus'
import router from '@/router'

export interface TokenInfo {
  accessToken: string
  tokenType: string
  expiresIn: number
  user: User
}

class AuthService {
  private tokenKey = 'access_token'
  private userKey = 'user_info'
  private refreshKey = 'refresh_token'
  private tokenRefreshTimer: NodeJS.Timeout | null = null

  /**
   * 登录
   */
  async login(params: LoginParams): Promise<TokenInfo> {
    try {
      const response = await login(params)

      // 存储访问令牌和用户信息
      this.setAccessToken(response.accessToken)
      this.setUserInfo(response.user)

      // 设置令牌自动刷新
      this.setupTokenRefresh(response.expiresIn)

      ElMessage.success('登录成功')
      return response
    } catch (error: any) {
      console.error('登录失败:', error)

      // 根据错误类型显示不同的错误信息
      if (error.response?.status === 423) {
        ElMessage.error(error.response.data.message || '账户已锁定，请稍后重试')
      } else {
        ElMessage.error(error.response?.data?.message || '登录失败，请检查用户名和密码')
      }

      throw error
    }
  }

  /**
   * 登出
   */
  async logout(): Promise<void> {
    try {
      await logout()
    } catch (error) {
      console.error('登出请求失败:', error)
    } finally {
      this.clearAuth()
      this.clearTokenRefresh()
      ElMessage.success('已退出登录')
      router.push('/login')
    }
  }

  /**
   * 获取用户信息
   */
  async getCurrentUser(): Promise<User | null> {
    try {
      const user = await getUserInfo()
      this.setUserInfo(user)
      return user
    } catch (error) {
      console.error('获取用户信息失败:', error)
      this.clearAuth()
      return null
    }
  }

  /**
   * 检查是否已登录
   */
  isAuthenticated(): boolean {
    const token = this.getAccessToken()
    return !!token && !this.isTokenExpired()
  }

  /**
   * 获取访问令牌
   */
  getAccessToken(): string | null {
    return localStorage.getItem(this.tokenKey)
  }

  /**
   * 设置访问令牌
   */
  private setAccessToken(token: string): void {
    localStorage.setItem(this.tokenKey, token)
  }

  /**
   * 获取用户信息
   */
  getUserInfo(): User | null {
    const userStr = localStorage.getItem(this.userKey)
    return userStr ? JSON.parse(userStr) : null
  }

  /**
   * 设置用户信息
   */
  private setUserInfo(user: User): void {
    localStorage.setItem(this.userKey, JSON.stringify(user))
  }

  /**
   * 获取刷新令牌（从Cookie中获取）
   */
  getRefreshToken(): string | null {
    // 从cookie中获取refresh token
    const cookies = document.cookie.split(';')
    const refreshCookie = cookies.find(cookie =>
      cookie.trim().startsWith('refreshToken=')
    )
    return refreshCookie ? refreshCookie.split('=')[1] : null
  }

  /**
   * 检查令牌是否过期
   */
  private isTokenExpired(): boolean {
    const token = this.getAccessToken()
    if (!token) return true

    try {
      // 解析JWT令牌获取过期时间
      const payload = JSON.parse(atob(token.split('.')[1]))
      const currentTime = Math.floor(Date.now() / 1000)

      // 检查令牌是否过期
      if (payload.exp && payload.exp < currentTime) {
        console.log('令牌已过期')
        return true
      }

      return false
    } catch (error) {
      console.error('解析令牌失败:', error)
      return true
    }
  }

  /**
   * 刷新访问令牌
   */
  async refreshAccessToken(): Promise<boolean> {
    try {
      const refreshToken = this.getRefreshToken()
      if (!refreshToken) {
        console.warn('没有找到刷新令牌')
        return false
      }

      const response = await refreshToken(refreshToken)

      this.setAccessToken(response.accessToken)
      this.setUserInfo(response.user)
      this.setupTokenRefresh(response.expiresIn)

      console.log('令牌刷新成功')
      return true
    } catch (error) {
      console.error('令牌刷新失败:', error)
      this.clearAuth()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
      return false
    }
  }

  /**
   * 设置令牌自动刷新定时器
   */
  private setupTokenRefresh(expiresIn: number): void {
    this.clearTokenRefresh()

    // 在令牌过期前5分钟开始刷新
    const refreshTime = (expiresIn - 300) * 1000

    if (refreshTime > 0) {
      this.tokenRefreshTimer = setTimeout(() => {
        this.refreshAccessToken()
      }, refreshTime)
    }
  }

  /**
   * 清除令牌刷新定时器
   */
  private clearTokenRefresh(): void {
    if (this.tokenRefreshTimer) {
      clearTimeout(this.tokenRefreshTimer)
      this.tokenRefreshTimer = null
    }
  }

  /**
   * 清除认证信息
   */
  private clearAuth(): void {
    localStorage.removeItem(this.tokenKey)
    localStorage.removeItem(this.userKey)
    this.clearTokenRefresh()
  }

  /**
   * 强制登出（用于处理认证错误）
   */
  forceLogout(): void {
    this.clearAuth()
    router.push('/login')
    ElMessage.error('登录状态已失效，请重新登录')
  }

  /**
   * 初始化认证状态（页面刷新时调用）
   */
  async initializeAuth(): Promise<boolean> {
    if (this.isAuthenticated()) {
      try {
        // 验证当前令牌是否有效
        const user = await this.getCurrentUser()
        if (user) {
          // 设置令牌刷新（假设15分钟过期）
          this.setupTokenRefresh(15 * 60)
          return true
        }
      } catch (error) {
        console.error('验证认证状态失败:', error)
      }
    }
    return false
  }
}

// 导出单例
export const authService = new AuthService()

// 类型导出
export type { TokenInfo }