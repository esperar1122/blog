import { get } from './index'
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