import { get } from './index'
import type { Article, Category, Tag, PaginationParams, PaginationResult } from 'blog-shared'

// 获取文章列表
export function getArticles(params: PaginationParams & {
  categoryId?: number
  tagId?: number
  keyword?: string
  status?: string
}): Promise<PaginationResult<Article>> {
  return get<PaginationResult<Article>>('/articles', params)
}

// 获取文章详情
export function getArticle(id: number): Promise<Article> {
  return get<Article>(`/articles/${id}`)
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