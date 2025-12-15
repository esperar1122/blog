import request, { post } from './index'
import type { User } from 'blog-shared'

interface LoginResponse {
  token: string
  user: User
}

interface LoginParams {
  username: string
  password: string
}

interface RegisterParams {
  username: string
  email: string
  password: string
  nickname?: string
}

// 登录
export function login(params: LoginParams): Promise<LoginResponse> {
  return post<LoginResponse>('/auth/login', params)
}

// 注册
export function register(params: RegisterParams): Promise<User> {
  return post<User>('/auth/register', params)
}

// 登出
export function logout(): Promise<void> {
  return post<void>('/auth/logout')
}

// 获取用户信息
export function getUserInfo(): Promise<User> {
  return request.get('/auth/me')
}