import request from '@/utils/request'

// 系统监控API
export const systemMonitorApi = {
  // 获取系统状态
  getSystemStatus() {
    return request({
      url: '/admin/system/status',
      method: 'get'
    })
  },

  // 获取系统指标
  getSystemMetrics() {
    return request({
      url: '/admin/system/metrics',
      method: 'get'
    })
  },

  // 健康检查
  healthCheck() {
    return request({
      url: '/admin/system/health',
      method: 'get'
    })
  },

  // 获取数据库状态
  getDatabaseStatus() {
    return request({
      url: '/admin/system/database',
      method: 'get'
    })
  },

  // 获取Redis状态
  getRedisStatus() {
    return request({
      url: '/admin/system/redis',
      method: 'get'
    })
  },

  // 获取应用信息
  getApplicationInfo() {
    return request({
      url: '/admin/system/info',
      method: 'get'
    })
  },

  // 获取系统负载
  getSystemLoad() {
    return request({
      url: '/admin/system/load',
      method: 'get'
    })
  }
}