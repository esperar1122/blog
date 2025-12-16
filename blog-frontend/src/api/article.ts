import { get, post, put, del } from './index'
import type {
  Article,
  Category,
  Tag,
  ArticleListResponse,
  ArticleDetailResponse,
  ArticleQueryParams,
  ArticleVersion,
  ArticleOperationLog,
  ArticleStatusManagementResponse,
  SchedulePublishRequest
} from '@/types/article'

// 获取文章列表
export function getArticles(params: ArticleQueryParams): Promise<ArticleListResponse> {
  return get<ArticleListResponse>('/api/v1/articles', params)
}

// 获取文章详情
export function getArticle(id: number): Promise<ArticleDetailResponse> {
  return get<ArticleDetailResponse>(`/api/v1/articles/${id}`)
}

// 搜索文章
export function searchArticles(params: ArticleQueryParams): Promise<ArticleListResponse> {
  return get<ArticleListResponse>('/api/v1/articles/search', params)
}

// 获取我的文章
export function getMyArticles(params: ArticleQueryParams): Promise<ArticleListResponse> {
  return get<ArticleListResponse>('/api/v1/articles/my-articles', params)
}

// 获取分类列表
export function getCategories(): Promise<Category[]> {
  return get<Category[]>('/categories')
}

// 获取标签列表
export function getTags(): Promise<Tag[]> {
  return get<Tag[]>('/tags')
}

// 创建文章
export function createArticle(data: any): Promise<Article> {
  return post<Article>('/articles', data)
}

// 更新文章
export function updateArticle(id: number, data: any): Promise<Article> {
  return put<Article>(`/articles/${id}`, data)
}

// 删除文章
export function deleteArticle(id: number): Promise<void> {
  return del(`/articles/${id}`)
}

// 发布文章
export function publishArticle(id: number): Promise<void> {
  return post(`/articles/${id}/publish`)
}

// 取消发布文章
export function unpublishArticle(id: number): Promise<void> {
  return post(`/articles/${id}/unpublish`)
}

// 点赞文章
export function likeArticle(id: number): Promise<{ liked: boolean; message: string }> {
  return post(`/articles/${id}/like`)
}

// 置顶文章
export function setArticleTop(id: number, isTop: boolean): Promise<void> {
  return post(`/articles/${id}/top?isTop=${isTop}`)
}

// 获取我的草稿
export function getDraftArticles(): Promise<Article[]> {
  return get<Article[]>('/articles/drafts')
}

// 获取我的已发布文章
export function getMyPublishedArticles(): Promise<Article[]> {
  return get<Article[]>('/articles/published/me')
}

// 获取文章统计
export function getArticleStats(): Promise<{
  totalArticles: number
  publishedArticles: number
  draftArticles: number
}> {
  return get('/articles/stats')
}

// 文章生命周期管理API

// 发布文章
export function publishArticle(id: number): Promise<ArticleStatusManagementResponse> {
  return post<ArticleStatusManagementResponse>(`/api/v1/articles/${id}/publish`)
}

// 下线文章
export function unpublishArticle(id: number): Promise<ArticleStatusManagementResponse> {
  return post<ArticleStatusManagementResponse>(`/api/v1/articles/${id}/unpublish`)
}

// 置顶文章
export function pinArticle(id: number): Promise<ArticleStatusManagementResponse> {
  return post<ArticleStatusManagementResponse>(`/api/v1/articles/${id}/pin`)
}

// 取消置顶文章
export function unpinArticle(id: number): Promise<ArticleStatusManagementResponse> {
  return post<ArticleStatusManagementResponse>(`/api/v1/articles/${id}/unpin`)
}

// 定时发布文章
export function schedulePublishArticle(id: number, data: SchedulePublishRequest): Promise<ArticleStatusManagementResponse> {
  return post<ArticleStatusManagementResponse>(`/api/v1/articles/${id}/schedule-publish`, data)
}

// 软删除文章
export function softDeleteArticle(id: number): Promise<ArticleStatusManagementResponse> {
  return del<ArticleStatusManagementResponse>(`/api/v1/articles/${id}/soft-delete`)
}

// 恢复文章
export function restoreArticle(id: number): Promise<ArticleStatusManagementResponse> {
  return post<ArticleStatusManagementResponse>(`/api/v1/articles/${id}/restore`)
}

// 获取文章版本历史
export function getArticleVersions(id: number): Promise<ArticleVersion[]> {
  return get<ArticleVersion[]>(`/api/v1/articles/${id}/versions`)
}

// 获取文章操作日志
export function getArticleOperationLogs(id: number): Promise<ArticleOperationLog[]> {
  return get<ArticleOperationLog[]>(`/api/v1/articles/${id}/operation-logs`)
}

// 获取已删除的文章
export function getDeletedArticles(): Promise<Article[]> {
  return get<Article[]>('/api/v1/articles/deleted')
}

// 获取定时发布的文章
export function getScheduledArticles(): Promise<Article[]> {
  return get<Article[]>('/api/v1/articles/scheduled')
}

// 获取置顶的文章
export function getPinnedArticles(): Promise<Article[]> {
  return get<Article[]>('/api/v1/articles/pinned')
}