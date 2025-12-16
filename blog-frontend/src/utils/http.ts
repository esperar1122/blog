import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { authService } from '@/services/authService'
import { ElMessage } from 'element-plus'

// 创建axios实例
const http: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
http.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 添加认证token
    const token = authService.getAccessToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
http.interceptors.response.use(
  (response) => {
    return response.data
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    // 处理401未授权错误
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        // 尝试刷新token
        const refreshSuccess = await authService.refreshAccessToken()

        if (refreshSuccess && originalRequest.headers) {
          // 重新设置请求头中的token
          const newToken = authService.getAccessToken()
          if (newToken) {
            originalRequest.headers.Authorization = `Bearer ${newToken}`
          }

          // 重新发起原始请求
          return http(originalRequest)
        }
      } catch (refreshError) {
        console.error('Token刷新失败:', refreshError)
        authService.forceLogout()
        return Promise.reject(error)
      }
    }

    // 处理423锁定状态
    if (error.response?.status === 423) {
      ElMessage.error(error.response.data.message || '账户已被锁定，请稍后重试')
      return Promise.reject(error)
    }

    // 处理其他HTTP错误
    const message = error.response?.data?.message || error.message || '请求失败'

    // 根据错误类型显示不同的提示
    if (error.response?.status >= 500) {
      ElMessage.error('服务器错误，请稍后重试')
    } else if (error.response?.status === 403) {
      ElMessage.error('权限不足')
    } else if (error.response?.status === 404) {
      ElMessage.error('请求的资源不存在')
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接')
    } else {
      // 其他错误不显示提示，让业务层面处理
      console.warn('HTTP请求错误:', message)
    }

    return Promise.reject(error)
  }
)

export default http