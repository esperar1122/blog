import { get, post, del } from './index'
import type {
  SearchQuery,
  SearchResult,
  SearchHistory,
  SearchStats,
  HotKeywords,
  PaginationParams,
  PaginationResult
} from 'blog-shared'

// 执行搜索
export function searchArticles(params: SearchQuery & PaginationParams): Promise<SearchResult> {
  return get<SearchResult>('/search', params)
}

// 获取搜索建议
export function getSearchSuggestions(keyword: string, limit = 10): Promise<string[]> {
  return get<string[]>('/search/suggestions', { keyword, limit })
}

// 获取用户搜索历史
export function getSearchHistory(page = 1, size = 20): Promise<PaginationResult<SearchHistory>> {
  return get<PaginationResult<SearchHistory>>('/search/history', { page, size })
}

// 保存搜索历史
export function saveSearchHistory(keyword: string, resultCount: number): Promise<void> {
  return post<void>('/search/history', { keyword, resultCount })
}

// 清空搜索历史
export function clearSearchHistory(): Promise<void> {
  return del<void>('/search/history')
}

// 获取热门搜索关键词
export function getHotKeywords(limit = 20): Promise<HotKeywords[]> {
  return get<HotKeywords[]>('/search/hot-keywords', { limit })
}

// 获取搜索统计数据（管理员）
export function getSearchStats(page = 1, size = 20): Promise<PaginationResult<SearchStats>> {
  return get<PaginationResult<SearchStats>>('/search/stats', { page, size })
}

// 获取相关文章推荐
export function getRelatedArticles(articleId: number, limit = 5): Promise<SearchResult['results']> {
  return get<SearchResult['results']>(`/search/related/${articleId}`, { limit })
}

// 更新热门关键词排名（管理员）
export function updateHotKeywordsRanking(): Promise<void> {
  return post<void>('/search/update-hot-keywords')
}