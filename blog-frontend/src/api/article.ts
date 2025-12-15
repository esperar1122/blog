import { get, post, put, del } from './index'
import type { Article, Category, Tag, PaginationParams, PaginationResult } from 'blog-shared'

// 获取文章列表
export function getArticles(params: PaginationParams & {
  categoryId?: number
  tagId?: number
  keyword?: string
}): Promise<PaginationResult<Article>> {
  return get<PaginationResult<Article>>('/articles', params)
}

// 获取文章详情
export function getArticle(id: number): Promise<Article> {
  return get<Article>(`/articles/${id}`)
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