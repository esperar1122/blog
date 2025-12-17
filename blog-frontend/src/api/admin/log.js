import request from '@/utils/request'

// 操作日志API
export const adminLogApi = {
  // 获取操作日志列表
  getLogs(params) {
    return request({
      url: '/admin/logs',
      method: 'get',
      params
    })
  },

  // 获取日志详情
  getLogDetail(id) {
    return request({
      url: `/admin/logs/${id}`,
      method: 'get'
    })
  },

  // 删除日志
  deleteLog(id) {
    return request({
      url: `/admin/logs/${id}`,
      method: 'delete'
    })
  },

  // 批量删除日志
  deleteLogs(ids) {
    return request({
      url: '/admin/logs/batch',
      method: 'delete',
      data: ids
    })
  },

  // 按时间范围删除日志
  deleteLogsByTimeRange(startTime, endTime) {
    return request({
      url: '/admin/logs/by-time-range',
      method: 'delete',
      params: { startTime, endTime }
    })
  },

  // 获取日志统计
  getLogStats() {
    return request({
      url: '/admin/logs/stats',
      method: 'get'
    })
  },

  // 获取操作类型统计
  getOperationStats() {
    return request({
      url: '/admin/logs/operation-stats',
      method: 'get'
    })
  },

  // 获取用户操作统计
  getUserStats() {
    return request({
      url: '/admin/logs/user-stats',
      method: 'get'
    })
  },

  // 获取操作趋势数据
  getOperationTrend(days) {
    return request({
      url: '/admin/logs/trend',
      method: 'get',
      params: { days }
    })
  },

  // 导出日志
  exportLogs(params) {
    return request({
      url: '/admin/logs/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },

  // 清空所有日志
  clearAllLogs() {
    return request({
      url: '/admin/logs/clear',
      method: 'delete'
    })
  }
}