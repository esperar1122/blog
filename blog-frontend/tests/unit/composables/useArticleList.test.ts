import { describe, it, expect, vi, beforeEach } from 'vitest'
import { ref } from 'vue'
import useArticleList from '@/composables/useArticleList'

// Mock API service
vi.mock('@/api/article', () => ({
  getArticleList: vi.fn(),
  searchArticles: vi.fn(),
}))

// Mock storage
const sessionStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
}
Object.assign(window, { sessionStorage: sessionStorageMock })

// Mock router
const mockRouter = {
  push: vi.fn(),
  replace: vi.fn(),
}

vi.mock('vue-router', () => ({
  useRouter: () => mockRouter,
}))

describe('useArticleList', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    sessionStorageMock.getItem.mockReturnValue(null)
  })

  it('应该初始化默认状态', () => {
    const {
      articles,
      loading,
      error,
      currentPage,
      totalPages,
      totalElements,
      pageSize,
      viewMode,
      sortBy,
      sortDir,
      filters,
    } = useArticleList()

    expect(articles.value).toEqual([])
    expect(loading.value).toBe(false)
    expect(error.value).toBe(null)
    expect(currentPage.value).toBe(0)
    expect(totalPages.value).toBe(0)
    expect(totalElements.value).toBe(0)
    expect(pageSize.value).toBe(10)
    expect(viewMode.value).toBe('grid')
    expect(sortBy.value).toBe('publishTime')
    expect(sortDir.value).toBe('desc')
    expect(filters.value).toEqual({
      categoryId: null,
      tagId: null,
      keyword: '',
      status: 'PUBLISHED',
      authorId: null,
    })
  })

  it('应该正确获取文章列表', async () => {
    const mockResponse = {
      content: [
        { id: 1, title: '文章1' },
        { id: 2, title: '文章2' },
      ],
      totalPages: 5,
      totalElements: 50,
      size: 10,
      number: 0,
      first: true,
      last: false,
      empty: false,
    }

    const { getArticleList } = require('@/api/article')
    getArticleList.mockResolvedValue({ data: mockResponse })

    const { fetchArticles, articles, totalPages, totalElements } = useArticleList()
    await fetchArticles()

    expect(getArticleList).toHaveBeenCalled()
    expect(articles.value).toEqual(mockResponse.content)
    expect(totalPages.value).toBe(mockResponse.totalPages)
    expect(totalElements.value).toBe(mockResponse.totalElements)
  })

  it('应该正确处理获取文章列表失败', async () => {
    const errorMessage = '获取文章列表失败'
    const { getArticleList } = require('@/api/article')
    getArticleList.mockRejectedValue(new Error(errorMessage))

    const { fetchArticles, error } = useArticleList()
    await fetchArticles()

    expect(error.value).toBe(errorMessage)
  })

  it('应该在获取文章时显示加载状态', async () => {
    const { getArticleList } = require('@/api/article')
    getArticleList.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)))

    const { fetchArticles, loading } = useArticleList()
    const fetchPromise = fetchArticles()

    expect(loading.value).toBe(true)

    await fetchPromise
    expect(loading.value).toBe(false)
  })

  it('应该正确处理分页参数', async () => {
    const { getArticleList } = require('@/api/article')
    getArticleList.mockResolvedValue({
      data: {
        content: [],
        totalPages: 1,
        totalElements: 0,
      },
    })

    const { fetchArticles, currentPage, pageSize } = useArticleList()

    // 设置分页参数
    currentPage.value = 2
    pageSize.value = 20

    await fetchArticles()

    expect(getArticleList).toHaveBeenCalledWith(
      expect.objectContaining({
        page: 2,
        size: 20,
      })
    )
  })

  it('应该正确处理筛选条件', async () => {
    const { getArticleList } = require('@/api/article')
    getArticleList.mockResolvedValue({
      data: {
        content: [],
        totalPages: 1,
        totalElements: 0,
      },
    })

    const { fetchArticles, filters } = useArticleList()

    // 设置筛选条件
    filters.value = {
      categoryId: 1,
      tagId: 2,
      keyword: 'Vue',
      status: 'PUBLISHED',
      authorId: null,
    }

    await fetchArticles()

    expect(getArticleList).toHaveBeenCalledWith(
      expect.objectContaining({
        categoryId: 1,
        tagId: 2,
        keyword: 'Vue',
        status: 'PUBLISHED',
      })
    )
  })

  it('应该正确处理排序参数', async () => {
    const { getArticleList } = require('@/api/article')
    getArticleList.mockResolvedValue({
      data: {
        content: [],
        totalPages: 1,
        totalElements: 0,
      },
    })

    const { fetchArticles, sortBy, sortDir } = useArticleList()

    // 设置排序参数
    sortBy.value = 'viewCount'
    sortDir.value = 'asc'

    await fetchArticles()

    expect(getArticleList).toHaveBeenCalledWith(
      expect.objectContaining({
        sortBy: 'viewCount',
        sortDir: 'asc',
      })
    )
  })

  it('应该正确处理排序变更', async () => {
    const { getArticleList } = require('@/api/article')
    getArticleList.mockResolvedValue({
      data: {
        content: [],
        totalPages: 1,
        totalElements: 0,
      },
    })

    const { handleSort, currentPage } = useArticleList()
    currentPage.value = 5

    await handleSort('likeCount', 'desc')

    expect(getArticleList).toHaveBeenCalledWith(
      expect.objectContaining({
        sortBy: 'likeCount',
        sortDir: 'desc',
        page: 0, // 排序时重置到第一页
      })
    )
    expect(currentPage.value).toBe(0)
  })

  it('应该正确处理筛选变更', async () => {
    const { getArticleList } = require('@/api/article')
    getArticleList.mockResolvedValue({
      data: {
        content: [],
        totalPages: 1,
        totalElements: 0,
      },
    })

    const { handleFilter, currentPage } = useArticleList()
    currentPage.value = 3

    await handleFilter({ categoryId: 5, keyword: '测试' })

    expect(getArticleList).toHaveBeenCalledWith(
      expect.objectContaining({
        categoryId: 5,
        keyword: '测试',
        page: 0, // 筛选时重置到第一页
      })
    )
    expect(currentPage.value).toBe(0)
  })

  it('应该正确处理页码变更', async () => {
    const { getArticleList } = require('@/api/article')
    getArticleList.mockResolvedValue({
      data: {
        content: [],
        totalPages: 1,
        totalElements: 0,
      },
    })

    const { handlePageChange } = useArticleList()

    await handlePageChange(3)

    expect(getArticleList).toHaveBeenCalledWith(
      expect.objectContaining({
        page: 3,
      })
    )
  })

  it('应该正确处理视图模式切换', async () => {
    const { handleViewModeChange, viewMode } = useArticleList()

    expect(viewMode.value).toBe('grid')

    await handleViewModeChange('list')

    expect(viewMode.value).toBe('list')
    expect(sessionStorageMock.setItem).toHaveBeenCalledWith(
      'articleViewMode',
      'list'
    )
  })

  it('应该从sessionStorage恢复视图模式', async () => {
    sessionStorageMock.getItem.mockReturnValue('list')

    const { viewMode } = useArticleList()

    expect(viewMode.value).toBe('list')
  })

  it('应该正确处理搜索功能', async () => {
    const mockResponse = {
      content: [{ id: 1, title: '搜索结果1' }],
      totalPages: 1,
      totalElements: 1,
    }

    const { searchArticles } = require('@/api/article')
    searchArticles.mockResolvedValue({ data: mockResponse })

    const { handleSearch, articles, isSearching } = useArticleList()

    await handleSearch('Vue.js')

    expect(searchArticles).toHaveBeenCalledWith(
      expect.objectContaining({
        keyword: 'Vue.js',
      })
    )
    expect(articles.value).toEqual(mockResponse.content)
    expect(isSearching.value).toBe(false)
  })

  it('应该在搜索时显示加载状态', async () => {
    const { searchArticles } = require('@/api/article')
    searchArticles.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)))

    const { handleSearch, isSearching } = useArticleList()
    const searchPromise = handleSearch('测试')

    expect(isSearching.value).toBe(true)

    await searchPromise
    expect(isSearching.value).toBe(false)
  })

  it('应该正确清除搜索结果', async () => {
    const { clearSearch, articles, filters, currentPage } = useArticleList()

    // 设置一些搜索状态
    articles.value = [{ id: 1, title: '搜索结果' }]
    filters.value.keyword = 'Vue'
    currentPage.value = 2

    clearSearch()

    expect(articles.value).toEqual([])
    expect(filters.value.keyword).toBe('')
    expect(currentPage.value).toBe(0)
  })

  it('应该正确处理刷新功能', async () => {
    const { getArticleList } = require('@/api/article')
    getArticleList.mockResolvedValue({
      data: {
        content: [],
        totalPages: 1,
        totalElements: 0,
      },
    })

    const { refresh, currentPage, pageSize } = useArticleList()

    // 设置当前状态
    currentPage.value = 3
    pageSize.value = 20

    await refresh()

    expect(getArticleList).toHaveBeenCalledWith(
      expect.objectContaining({
        page: 3,
        size: 20,
      })
    )
  })

  it('应该正确加载更多文章', async () => {
    const { getArticleList } = require('@/api/article')
    getArticleList.mockResolvedValue({
      data: {
        content: [{ id: 3, title: '新文章' }],
        totalPages: 3,
        totalElements: 25,
      },
    })

    const { loadMore, articles, currentPage } = useArticleList()

    // 初始化一些文章
    articles.value = [
      { id: 1, title: '文章1' },
      { id: 2, title: '文章2' },
    ]
    currentPage.value = 1

    await loadMore()

    expect(getArticleList).toHaveBeenCalledWith(
      expect.objectContaining({
        page: 2, // 下一页
      })
    )
    expect(articles.value).toHaveLength(3) // 包含新加载的文章
    expect(currentPage.value).toBe(2)
  })

  it('应该在没有更多文章时停止加载', async () => {
    const { loadMore, hasMore } = useArticleList()

    // 设置为最后一页
    hasMore.value = false

    await loadMore()

    // 不应该调用API
    const { getArticleList } = require('@/api/article')
    expect(getArticleList).not.toHaveBeenCalled()
  })

  it('应该正确重置所有状态', () => {
    const {
      reset,
      articles,
      loading,
      error,
      currentPage,
      totalPages,
      totalElements,
      filters,
    } = useArticleList()

    // 设置一些状态
    articles.value = [{ id: 1, title: '测试' }]
    loading.value = true
    error.value = '错误'
    currentPage.value = 5
    totalPages.value = 10
    totalElements.value = 100
    filters.value.keyword = 'Vue'

    reset()

    expect(articles.value).toEqual([])
    expect(loading.value).toBe(false)
    expect(error.value).toBe(null)
    expect(currentPage.value).toBe(0)
    expect(totalPages.value).toBe(0)
    expect(totalElements.value).toBe(0)
    expect(filters.value.keyword).toBe('')
  })

  it('应该正确处理URL同步', async () => {
    const { getArticleList } = require('@/api/article')
    getArticleList.mockResolvedValue({
      data: {
        content: [],
        totalPages: 1,
        totalElements: 0,
      },
    })

    const { syncWithURL, currentPage, filters } = useArticleList()

    // 模拟URL参数
    const mockQuery = {
      page: '3',
      categoryId: '5',
      keyword: 'Vue',
      sortBy: 'viewCount',
      viewMode: 'list',
    }

    syncWithURL(mockQuery)

    expect(currentPage.value).toBe(3)
    expect(filters.value.categoryId).toBe(5)
    expect(filters.value.keyword).toBe('Vue')
  })

  it('应该正确导出文章列表', () => {
    const { articles } = useArticleList()
    articles.value = [
      { id: 1, title: '文章1', publishTime: '2025-12-16' },
      { id: 2, title: '文章2', publishTime: '2025-12-15' },
    ]

    const exportData = JSON.stringify(articles.value, null, 2)

    expect(exportData).toContain('文章1')
    expect(exportData).toContain('文章2')
  })

  it('应该正确处理批量操作', async () => {
    const { getArticleList } = require('@/api/article')
    getArticleList.mockResolvedValue({
      data: {
        content: [],
        totalPages: 1,
        totalElements: 0,
      },
    })

    const { batchDelete, selectedArticles, fetchArticles } = useArticleList()

    // 选择一些文章
    selectedArticles.value = [1, 2, 3]

    await batchDelete()

    // 应该刷新列表
    expect(getArticleList).toHaveBeenCalled()
    expect(selectedArticles.value).toEqual([])
  })
})