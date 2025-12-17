import request from '@/utils/request'
import type { AxiosResponse } from 'axios'

export interface ContentQueryRequest {
  page?: number
  size?: number
  status?: string
  keyword?: string
  categoryId?: number
  startTime?: string
  endTime?: string
  articleId?: number
  contentType?: string
}

export interface ArticleReviewRequest {
  id: number
  title: string
  content: string
  summary?: string
  coverImage?: string
  status: string
  categoryId?: number
  reviewNote?: string
}

export interface BatchOperationRequest {
  operationType: string
  ids: number[]
  targetType?: string
  newStatus?: string
  reason?: string
}

export interface ContentStatsResponse {
  totalArticles: number
  publishedArticles: number
  draftArticles: number
  totalComments: number
  activeComments: number
  deletedComments: number
  todayArticles: number
  todayComments: number
  categoryStats?: Record<string, number>
  monthlyStats?: Record<string, number>
}

export interface ExportResponse {
  fileName: string
  downloadUrl: string
  fileSize: number
  format: string
  recordCount: number
  exportTime: string
}

export interface SensitiveWord {
  id?: number
  word: string
  type: string
  level: string
  status: string
  createTime?: string
  updateTime?: string
}

export interface SystemSettings {
  id?: number
  settingKey: string
  settingValue?: string
  description?: string
  type?: string
  isPublic?: boolean
  createTime?: string
  updateTime?: string
}

class AdminContentService {
  private baseUrl = '/admin/content'

  async getArticles(params: ContentQueryRequest): Promise<AxiosResponse<any>> {
    return request({
      url: `${this.baseUrl}/articles`,
      method: 'get',
      params
    })
  }

  async getComments(params: ContentQueryRequest): Promise<AxiosResponse<any>> {
    return request({
      url: `${this.baseUrl}/comments`,
      method: 'get',
      params
    })
  }

  async reviewArticle(data: ArticleReviewRequest): Promise<AxiosResponse<any>> {
    return request({
      url: `${this.baseUrl}/articles/review`,
      method: 'put',
      data
    })
  }

  async batchOperateArticles(data: BatchOperationRequest): Promise<AxiosResponse<any>> {
    return request({
      url: `${this.baseUrl}/batch-articles`,
      method: 'post',
      data
    })
  }

  async batchOperateComments(data: BatchOperationRequest): Promise<AxiosResponse<any>> {
    return request({
      url: `${this.baseUrl}/batch-comments`,
      method: 'post',
      data
    })
  }

  async exportArticles(params: ContentQueryRequest, format: string = 'json'): Promise<AxiosResponse<ExportResponse>> {
    return request({
      url: `${this.baseUrl}/export/articles`,
      method: 'get',
      params: { ...params, format }
    })
  }

  async exportComments(params: ContentQueryRequest, format: string = 'json'): Promise<AxiosResponse<ExportResponse>> {
    return request({
      url: `${this.baseUrl}/export/comments`,
      method: 'get',
      params: { ...params, format }
    })
  }

  async getContentStats(): Promise<AxiosResponse<ContentStatsResponse>> {
    return request({
      url: `${this.baseUrl}/stats`,
      method: 'get'
    })
  }

  async checkContent(content: string): Promise<AxiosResponse<boolean>> {
    return request({
      url: `${this.baseUrl}/check-content`,
      method: 'post',
      params: { content }
    })
  }

  async filterContent(content: string): Promise<AxiosResponse<string>> {
    return request({
      url: `${this.baseUrl}/filter-content`,
      method: 'post',
      params: { content }
    })
  }

  async getSensitiveWords(params: {
    page?: number
    size?: number
    keyword?: string
    status?: string
  } = {}): Promise<AxiosResponse<any>> {
    return request({
      url: `${this.baseUrl}/sensitive-words`,
      method: 'get',
      params
    })
  }

  async addSensitiveWord(data: SensitiveWord): Promise<AxiosResponse<any>> {
    return request({
      url: `${this.baseUrl}/sensitive-words`,
      method: 'post',
      data
    })
  }

  async updateSensitiveWord(id: number, data: SensitiveWord): Promise<AxiosResponse<any>> {
    return request({
      url: `${this.baseUrl}/sensitive-words/${id}`,
      method: 'put',
      data
    })
  }

  async deleteSensitiveWord(id: number): Promise<AxiosResponse<any>> {
    return request({
      url: `${this.baseUrl}/sensitive-words/${id}`,
      method: 'delete'
    })
  }

  async updateSensitiveWordStatus(id: number, status: string): Promise<AxiosResponse<any>> {
    return request({
      url: `${this.baseUrl}/sensitive-words/${id}/status`,
      method: 'put',
      params: { status }
    })
  }

  async getAllSystemSettings(): Promise<AxiosResponse<any>> {
    return request({
      url: `${this.baseUrl}/system-settings`,
      method: 'get'
    })
  }

  async getPublicSystemSettings(): Promise<AxiosResponse<any>> {
    return request({
      url: `${this.baseUrl}/system-settings/public`,
      method: 'get'
    })
  }

  async updateSystemSetting(data: SystemSettings): Promise<AxiosResponse<any>> {
    return request({
      url: `${this.baseUrl}/system-settings`,
      method: 'put',
      data
    })
  }

  async batchUpdateSystemSettings(settings: Record<string, string>): Promise<AxiosResponse<any>> {
    return request({
      url: `${this.baseUrl}/system-settings/batch`,
      method: 'put',
      data: settings
    })
  }

  async getSystemSettingByKey(key: string): Promise<AxiosResponse<any>> {
    return request({
      url: `${this.baseUrl}/system-settings/${key}`,
      method: 'get'
    })
  }

  async resetSystemSettingToDefault(key: string): Promise<AxiosResponse<any>> {
    return request({
      url: `${this.baseUrl}/system-settings/${key}/reset`,
      method: 'post'
    })
  }

  downloadFile(fileName: string): string {
    return `${this.baseUrl}/download/${fileName}`
  }
}

export default new AdminContentService()