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

export interface RegisterParams {
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
export function register(params: RegisterParams): Promise<LoginResponse> {
  return post<LoginResponse>('/auth/register', params)
}

// 登出
export function logout(): Promise<void> {
  return post<void>('/auth/logout')
}

// 获取用户信息
export function getUserInfo(): Promise<User> {
  return request.get('/auth/me')
}

// 检查用户名是否存在
export function checkUsernameExists(username: string): Promise<boolean> {
  return request.get('/auth/check-username', { params: { username } })
}

// 检查邮箱是否存在
export function checkEmailExists(email: string): Promise<boolean> {
  return request.get('/auth/check-email', { params: { email } })
}