import request from '@/utils/request'

export interface User {
  id: number
  username: string
  email: string
  nickname?: string
  avatar?: string
  bio?: string
  role: 'USER' | 'ADMIN'
  status: 'ACTIVE' | 'INACTIVE' | 'BANNED'
  createTime: string
  updateTime: string
  lastLoginTime?: string
}

export interface UserQueryParams {
  page?: number
  size?: number
  role?: string
  status?: string
  keyword?: string
}

export interface UserUpdateData {
  nickname?: string
  email?: string
  bio?: string
  role?: 'USER' | 'ADMIN'
  status?: 'ACTIVE' | 'INACTIVE' | 'BANNED'
}

export interface UserStats {
  totalUsers: number
  activeUsers: number
  bannedUsers: number
  inactiveUsers: number
  adminUsers: number
  normalUsers: number
  todayRegistrations: number
  weekRegistrations: number
  monthRegistrations: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

class AdminService {
  // 获取用户列表
  async getUserList(params: UserQueryParams): Promise<PageResult<User>> {
    const response = await request.get('/admin/users', { params })
    return response.data
  }

  // 获取用户详情
  async getUserById(id: number): Promise<User> {
    const response = await request.get(`/admin/users/${id}`)
    return response.data
  }

  // 更新用户信息
  async updateUser(id: number, data: UserUpdateData): Promise<User> {
    const response = await request.put(`/admin/users/${id}`, data)
    return response.data
  }

  // 切换用户状态
  async toggleUserStatus(id: number): Promise<User> {
    const response = await request.put(`/admin/users/${id}/toggle-status`)
    return response.data
  }

  // 删除用户
  async deleteUser(id: number, permanent = false): Promise<void> {
    await request.delete(`/admin/users/${id}`, { params: { permanent } })
  }

  // 获取用户统计
  async getUserStats(): Promise<UserStats> {
    const response = await request.get('/admin/users/stats')
    return response.data
  }
}

export default new AdminService()