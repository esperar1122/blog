import request from '@/utils/request'
import type {
  CommentReport,
  CommentBlacklist,
  SensitiveWord,
  CommentModerationQuery,
  CommentStatistics,
  CommentReportRequest,
  CommentModerationRequest,
  BlacklistRequest,
  SensitiveWordRequest,
  SensitiveWordFilterResult,
  BatchModerationRequest,
  PaginationResult,
  ApiResponse
} from '@shared/types'

const BASE_URL = '/api/v1'

export const commentModerationService = {
  // Report related APIs
  /**
   * 举报评论
   */
  reportComment: (commentId: number, data: CommentReportRequest): Promise<ApiResponse<CommentReport>> => {
    return request({
      url: `${BASE_URL}/comments/${commentId}/report`,
      method: 'POST',
      data
    })
  },

  /**
   * 获取举报列表
   */
  getReports: (query: CommentModerationQuery): Promise<ApiResponse<PaginationResult<CommentReport>>> => {
    return request({
      url: `${BASE_URL}/comments/reports`,
      method: 'GET',
      params: query
    })
  },

  /**
   * 审核举报
   */
  reviewReport: (reportId: number, data: CommentModerationRequest): Promise<ApiResponse<void>> => {
    return request({
      url: `${BASE_URL}/comments/reports/${reportId}/review`,
      method: 'PUT',
      data
    })
  },

  // Comment moderation APIs
  /**
   * 审核评论
   */
  moderateComment: (commentId: number, data: CommentModerationRequest): Promise<ApiResponse<void>> => {
    return request({
      url: `${BASE_URL}/comments/${commentId}/moderate`,
      method: 'PUT',
      data
    })
  },

  /**
   * 批量审核评论
   */
  batchModerateComments: (data: BatchModerationRequest): Promise<ApiResponse<void>> => {
    return request({
      url: `${BASE_URL}/comments/batch-moderate`,
      method: 'POST',
      data
    })
  },

  // Blacklist APIs
  /**
   * 添加用户到黑名单
   */
  addToBlacklist: (userId: number, data: BlacklistRequest): Promise<ApiResponse<CommentBlacklist>> => {
    return request({
      url: `${BASE_URL}/users/${userId}/blacklist`,
      method: 'POST',
      data
    })
  },

  /**
   * 从黑名单移除用户
   */
  removeFromBlacklist: (userId: number): Promise<ApiResponse<void>> => {
    return request({
      url: `${BASE_URL}/users/${userId}/blacklist`,
      method: 'DELETE'
    })
  },

  /**
   * 获取黑名单列表
   */
  getBlacklistedUsers: (): Promise<ApiResponse<CommentBlacklist[]>> => {
    return request({
      url: `${BASE_URL}/blacklist`,
      method: 'GET'
    })
  },

  // Sensitive word APIs
  /**
   * 获取敏感词列表
   */
  getSensitiveWords: (type?: string): Promise<ApiResponse<SensitiveWord[]>> => {
    return request({
      url: `${BASE_URL}/sensitive-words`,
      method: 'GET',
      params: { type }
    })
  },

  /**
   * 添加敏感词
   */
  addSensitiveWord: (data: SensitiveWordRequest): Promise<ApiResponse<SensitiveWord>> => {
    return request({
      url: `${BASE_URL}/sensitive-words`,
      method: 'POST',
      data
    })
  },

  /**
   * 更新敏感词
   */
  updateSensitiveWord: (id: number, data: SensitiveWordRequest): Promise<ApiResponse<SensitiveWord>> => {
    return request({
      url: `${BASE_URL}/sensitive-words/${id}`,
      method: 'PUT',
      data
    })
  },

  /**
   * 删除敏感词
   */
  deleteSensitiveWord: (id: number): Promise<ApiResponse<void>> => {
    return request({
      url: `${BASE_URL}/sensitive-words/${id}`,
      method: 'DELETE'
    })
  },

  /**
   * 过滤敏感词
   */
  filterSensitiveWords: (text: string): Promise<ApiResponse<SensitiveWordFilterResult>> => {
    return request({
      url: `${BASE_URL}/comments/filter`,
      method: 'POST',
      data: { text }
    })
  },

  // Statistics APIs
  /**
   * 获取评论统计数据
   */
  getCommentStatistics: (): Promise<ApiResponse<CommentStatistics>> => {
    return request({
      url: `${BASE_URL}/comments/statistics`,
      method: 'GET'
    })
  },

  /**
   * 获取举报统计数据
   */
  getReportStatistics: (): Promise<ApiResponse<CommentStatistics>> => {
    return request({
      url: `${BASE_URL}/comments/reports/statistics`,
      method: 'GET'
    })
  }
}