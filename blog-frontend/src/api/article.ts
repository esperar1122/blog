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
  return get<ArticleListResponse>('/articles', params)
}

// 兼容性API - 获取文章列表 (使用旧接口)
export function getArticlesCompat(params: PaginationParams & {
  categoryId?: number
  tagId?: number
  keyword?: string
  status?: string
}): Promise<PaginationResult<Article>> {
  return get<PaginationResult<Article>>('/articles', params)
}

// 获取文章详情
export function getArticle(id: number): Promise<ArticleDetailResponse> {
  return get<ArticleDetailResponse>(`/articles/${id}`)
}

// 搜索文章
export function searchArticles(params: ArticleQueryParams): Promise<ArticleListResponse> {
  return get<ArticleListResponse>('/articles/search', params)
}

// 获取我的文章
export function getMyArticles(params: ArticleQueryParams): Promise<ArticleListResponse> {
  return get<ArticleListResponse>('/articles/my-articles', params)
}

// 获取最新文章
export function getLatestArticles(limit = 10): Promise<Article[]> {
  return get<Article[]>('/articles/latest', { limit })
}

// 获取热门文章
export function getPopularArticles(limit = 10): Promise<Article[]> {
  return get<Article[]>('/articles/popular', { limit })
}

// 获取标签下的文章列表
export function getArticlesByTag(tagId: number, page = 1, size = 10) {
  return get(`/tags/${tagId}/articles`, { page, size })
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

// 分页获取已发布文章（兼容视图调用）
export function getPublishedArticlesWithPagination(params: ArticleQueryParams): Promise<ArticleListResponse> {
  return get<ArticleListResponse>('/articles', params)
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
  return post<ArticleStatusManagementResponse>(`/articles/${id}/publish`)
}

// 下线文章
export function unpublishArticle(id: number): Promise<ArticleStatusManagementResponse> {
  return post<ArticleStatusManagementResponse>(`/articles/${id}/unpublish`)
}

// 别名函数，用于兼容 MyArticles.vue 中的导入
export function publishArticleApi(id: number): Promise<ArticleStatusManagementResponse> {
  return publishArticle(id)
}

export function unpublishArticleApi(id: number): Promise<ArticleStatusManagementResponse> {
  return unpublishArticle(id)
}

export function setArticleTopApi(id: number, isTop: boolean): Promise<void> {
  return setArticleTop(id, isTop)
}

// 置顶文章
export function pinArticle(id: number): Promise<ArticleStatusManagementResponse> {
  return post<ArticleStatusManagementResponse>(`/articles/${id}/pin`)
}

// 取消置顶文章
export function unpinArticle(id: number): Promise<ArticleStatusManagementResponse> {
  return post<ArticleStatusManagementResponse>(`/articles/${id}/unpin`)
}

// 定时发布文章
export function schedulePublishArticle(id: number, data: SchedulePublishRequest): Promise<ArticleStatusManagementResponse> {
  return post<ArticleStatusManagementResponse>(`/articles/${id}/schedule-publish`, data)
}

// 软删除文章
export function softDeleteArticle(id: number): Promise<ArticleStatusManagementResponse> {
  return del<ArticleStatusManagementResponse>(`/articles/${id}/soft-delete`)
}

// 恢复文章
export function restoreArticle(id: number): Promise<ArticleStatusManagementResponse> {
  return post<ArticleStatusManagementResponse>(`/articles/${id}/restore`)
}

// 获取文章版本历史
export function getArticleVersions(id: number): Promise<ArticleVersion[]> {
  return get<ArticleVersion[]>(`/articles/${id}/versions`)
}

// 获取文章操作日志
export function getArticleOperationLogs(id: number): Promise<ArticleOperationLog[]> {
  return get<ArticleOperationLog[]>(`/articles/${id}/operation-logs`)
}

// 获取已删除的文章
export function getDeletedArticles(): Promise<Article[]> {
  return get<Article[]>('/articles/deleted')
}

// 获取定时发布的文章
export function getScheduledArticles(): Promise<Article[]> {
  return get<Article[]>('/articles/scheduled')
}

// 获取置顶的文章
export function getPinnedArticles(): Promise<Article[]> {
  return get<Article[]>('/articles/pinned')
}