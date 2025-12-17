import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { SearchResult, SearchHistory, HotKeywords, SearchQuery } from 'blog-shared'
import {
  searchArticles,
  getSearchHistory,
  saveSearchHistory,
  clearSearchHistory,
  getHotKeywords,
  getRelatedArticles
} from '@/api/search'

export const useSearchStore = defineStore('search', () => {
  // 搜索结果
  const searchResult = ref<SearchResult | null>(null)
  const searchHistory = ref<SearchHistory[]>([])
  const hotKeywords = ref<HotKeywords[]>([])
  const relatedArticles = ref<SearchResult['results']>([])
  const loading = ref(false)
  const currentQuery = ref<SearchQuery | null>(null)

  // 搜索状态
  const hasResults = computed(() => {
    return searchResult.value && searchResult.value.results.length > 0
  })

  const resultCount = computed(() => {
    return searchResult.value?.pagination.total || 0
  })

  const recentHistory = computed(() => {
    return searchHistory.value.slice(0, 10) // 最多显示10条历史记录
  })

  // 执行搜索
  async function performSearch(query: SearchQuery) {
    loading.value = true
    currentQuery.value = query

    try {
      const result = await searchArticles({
        q: query.q,
        fields: query.fields,
        categoryId: query.categoryId,
        tagIds: query.tagIds,
        startDate: query.startDate,
        endDate: query.endDate,
        sortBy: query.sortBy,
        page: 1,
        size: 10
      })

      searchResult.value = result

      // 保存搜索历史
      try {
        await saveSearchHistory(query.q, result.searchMeta.resultCount)
      } catch (error) {
        console.warn('Failed to save search history:', error)
      }

      // 更新搜索历史列表
      await fetchSearchHistory()

      return result
    } catch (error) {
      console.error('Search failed:', error)
      searchResult.value = null
      throw error
    } finally {
      loading.value = false
    }
  }

  // 加载更多搜索结果
  async function loadMoreResults(page: number) {
    if (!currentQuery.value || loading.value) return

    loading.value = true

    try {
      const result = await searchArticles({
        ...currentQuery.value,
        page,
        size: 10
      })

      if (searchResult.value) {
        // 合并结果
        searchResult.value.results.push(...result.results)
        searchResult.value.pagination = result.pagination
      }

      return result
    } catch (error) {
      console.error('Load more results failed:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取搜索历史
  async function fetchSearchHistory() {
    try {
      const result = await getSearchHistory(1, 20)
      searchHistory.value = result.list
      return result
    } catch (error) {
      console.error('Failed to fetch search history:', error)
      throw error
    }
  }

  // 清空搜索历史
  async function clearHistory() {
    try {
      await clearSearchHistory()
      searchHistory.value = []
    } catch (error) {
      console.error('Failed to clear search history:', error)
      throw error
    }
  }

  // 删除单条搜索历史
  async function removeHistory(id: number) {
    try {
      // 这里需要在API中添加删除单条记录的方法
      // await deleteSearchHistory(id)
      searchHistory.value = searchHistory.value.filter(item => item.id !== id)
    } catch (error) {
      console.error('Failed to remove search history:', error)
      throw error
    }
  }

  // 获取热门关键词
  async function fetchHotKeywords(limit = 20) {
    try {
      const keywords = await getHotKeywords(limit)
      hotKeywords.value = keywords
      return keywords
    } catch (error) {
      console.error('Failed to fetch hot keywords:', error)
      throw error
    }
  }

  // 获取相关文章
  async function fetchRelatedArticles(articleId: number, limit = 5) {
    try {
      const articles = await getRelatedArticles(articleId, limit)
      relatedArticles.value = articles
      return articles
    } catch (error) {
      console.error('Failed to fetch related articles:', error)
      throw error
    }
  }

  // 清空搜索结果
  function clearResults() {
    searchResult.value = null
    currentQuery.value = null
  }

  // 初始化
  async function initialize() {
    try {
      await Promise.all([
        fetchSearchHistory(),
        fetchHotKeywords()
      ])
    } catch (error) {
      console.error('Failed to initialize search store:', error)
    }
  }

  return {
    // State
    searchResult,
    searchHistory,
    hotKeywords,
    relatedArticles,
    loading,
    currentQuery,

    // Computed
    hasResults,
    resultCount,
    recentHistory,

    // Actions
    performSearch,
    loadMoreResults,
    fetchSearchHistory,
    clearHistory,
    removeHistory,
    fetchHotKeywords,
    fetchRelatedArticles,
    clearResults,
    initialize
  }
})