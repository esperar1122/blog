import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { ref } from 'vue'
import useArticle from '@/composables/useArticle'

// Mock API service
vi.mock('@/api/article', () => ({
  getArticleDetail: vi.fn(),
  likeArticle: vi.fn(),
  shareArticle: vi.fn(),
}))

// Mock toast
vi.mock('@/utils/toast', () => ({
  success: vi.fn(),
  error: vi.fn(),
}))

// Mock storage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
}
Object.assign(window, { localStorage: localStorageMock })

describe('useArticle', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('应该初始化默认状态', () => {
    const { article, author, tags, loading, error, isLiked } = useArticle()

    expect(article.value).toBe(null)
    expect(author.value).toBe(null)
    expect(tags.value).toEqual([])
    expect(loading.value).toBe(false)
    expect(error.value).toBe(null)
    expect(isLiked.value).toBe(false)
  })

  it('应该正确获取文章详情', async () => {
    const mockArticle = {
      id: 1,
      title: '测试文章',
      content: '测试内容',
      authorId: 1,
    }
    const mockAuthor = {
      id: 1,
      nickname: '测试作者',
      avatar: 'avatar.jpg',
    }
    const mockTags = [
      { id: 1, name: 'Vue' },
      { id: 2, name: 'TypeScript' },
    ]

    const { getArticleDetail } = require('@/api/article')
    getArticleDetail.mockResolvedValue({
      data: {
        article: mockArticle,
        author: mockAuthor,
        tags: mockTags,
        isLiked: false,
      },
    })

    const { fetchArticle, article, author, tags, isLiked } = useArticle()
    await fetchArticle(1)

    expect(getArticleDetail).toHaveBeenCalledWith(1)
    expect(article.value).toEqual(mockArticle)
    expect(author.value).toEqual(mockAuthor)
    expect(tags.value).toEqual(mockTags)
    expect(isLiked.value).toBe(false)
  })

  it('应该正确处理获取文章详情失败', async () => {
    const errorMessage = '文章不存在'
    const { getArticleDetail } = require('@/api/article')
    getArticleDetail.mockRejectedValue(new Error(errorMessage))

    const { fetchArticle, error } = useArticle()
    await fetchArticle(999)

    expect(error.value).toBe(errorMessage)
    expect(error.value).toBeTruthy()
  })

  it('应该在获取文章时显示加载状态', async () => {
    const { getArticleDetail } = require('@/api/article')
    getArticleDetail.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)))

    const { fetchArticle, loading } = useArticle()
    const fetchPromise = fetchArticle(1)

    expect(loading.value).toBe(true)

    await fetchPromise
    expect(loading.value).toBe(false)
  })

  it('应该正确切换点赞状态', async () => {
    const mockArticle = {
      id: 1,
      likeCount: 10,
    }
    const { getArticleDetail, likeArticle } = require('@/api/article')
    getArticleDetail.mockResolvedValue({
      data: {
        article: mockArticle,
        author: null,
        tags: [],
        isLiked: false,
      },
    })
    likeArticle.mockResolvedValue({
      data: {
        liked: true,
        likeCount: 11,
      },
    })

    const { fetchArticle, toggleLike, isLiked, article } = useArticle()
    await fetchArticle(1)

    expect(isLiked.value).toBe(false)
    expect(article.value?.likeCount).toBe(10)

    await toggleLike(1)

    expect(likeArticle).toHaveBeenCalledWith(1)
    expect(isLiked.value).toBe(true)
    expect(article.value?.likeCount).toBe(11)
  })

  it('应该正确取消点赞', async () => {
    const mockArticle = {
      id: 1,
      likeCount: 10,
    }
    const { getArticleDetail, likeArticle } = require('@/api/article')
    getArticleDetail.mockResolvedValue({
      data: {
        article: mockArticle,
        author: null,
        tags: [],
        isLiked: true,
      },
    })
    likeArticle.mockResolvedValue({
      data: {
        liked: false,
        likeCount: 9,
      },
    })

    const { fetchArticle, toggleLike, isLiked, article } = useArticle()
    await fetchArticle(1)

    expect(isLiked.value).toBe(true)
    expect(article.value?.likeCount).toBe(10)

    await toggleLike(1)

    expect(likeArticle).toHaveBeenCalledWith(1)
    expect(isLiked.value).toBe(false)
    expect(article.value?.likeCount).toBe(9)
  })

  it('应该正确处理点赞失败', async () => {
    const { getArticleDetail, likeArticle } = require('@/api/article')
    getArticleDetail.mockResolvedValue({
      data: {
        article: { id: 1, likeCount: 10 },
        author: null,
        tags: [],
        isLiked: false,
      },
    })
    likeArticle.mockRejectedValue(new Error('点赞失败'))

    const { fetchArticle, toggleLike, isLiked } = useArticle()
    await fetchArticle(1)

    await toggleLike(1)

    expect(isLiked.value).toBe(false) // 状态不应改变
  })

  it('应该正确处理分享功能', async () => {
    const { shareArticle } = require('@/api/article')
    shareArticle.mockResolvedValue({
      data: {
        success: true,
        shareCount: 5,
      },
    })

    const { handleShare } = useArticle()
    const result = await handleShare(1, 'weibo')

    expect(shareArticle).toHaveBeenCalledWith(1, 'weibo')
    expect(result).toEqual({
      success: true,
      shareCount: 5,
    })
  })

  it('应该正确处理分享失败', async () => {
    const { shareArticle } = require('@/api/article')
    shareArticle.mockRejectedValue(new Error('分享失败'))

    const { handleShare } = useArticle()
    const result = await handleShare(1, 'weibo')

    expect(result.success).toBe(false)
  })

  it('应该正确缓存文章数据', async () => {
    const mockArticle = {
      id: 1,
      title: '测试文章',
    }
    const { getArticleDetail } = require('@/api/article')
    getArticleDetail.mockResolvedValue({
      data: {
        article: mockArticle,
        author: null,
        tags: [],
        isLiked: false,
      },
    })

    const { fetchArticle, article } = useArticle()

    // 第一次获取
    await fetchArticle(1)
    expect(getArticleDetail).toHaveBeenCalledTimes(1)

    // 第二次获取，应该使用缓存
    await fetchArticle(1)
    expect(getArticleDetail).toHaveBeenCalledTimes(1) // 不应该再次调用API

    expect(localStorageMock.setItem).toHaveBeenCalled()
  })

  it('应该支持强制刷新文章数据', async () => {
    const { getArticleDetail } = require('@/api/article')
    getArticleDetail.mockResolvedValue({
      data: {
        article: { id: 1, title: '测试文章' },
        author: null,
        tags: [],
        isLiked: false,
      },
    })

    const { fetchArticle } = useArticle()

    // 第一次获取
    await fetchArticle(1)
    expect(getArticleDetail).toHaveBeenCalledTimes(1)

    // 强制刷新
    await fetchArticle(1, true)
    expect(getArticleDetail).toHaveBeenCalledTimes(2) // 应该再次调用API
  })

  it('应该正确预加载相关文章', async () => {
    const { getArticleDetail } = require('@/api/article')
    getArticleDetail.mockResolvedValue({
      data: {
        article: { id: 1, categoryId: 1 },
        author: null,
        tags: [{ id: 1, name: 'Vue' }],
        isLiked: false,
        relatedArticles: [
          { id: 2, title: '相关文章1' },
          { id: 3, title: '相关文章2' },
        ],
      },
    })

    const { fetchArticle, relatedArticles } = useArticle()
    await fetchArticle(1)

    expect(relatedArticles.value).toHaveLength(2)
    expect(relatedArticles.value[0].title).toBe('相关文章1')
  })

  it('应该正确处理文章收藏功能', async () => {
    const mockArticle = {
      id: 1,
      isFavorited: false,
      favoriteCount: 5,
    }
    const { getArticleDetail } = require('@/api/article')
    getArticleDetail.mockResolvedValue({
      data: {
        article: mockArticle,
        author: null,
        tags: [],
        isLiked: false,
      },
    })

    const { fetchArticle, toggleFavorite, isFavorited, article } = useArticle()
    await fetchArticle(1)

    expect(isFavorited.value).toBe(false)
    expect(article.value?.favoriteCount).toBe(5)

    // 模拟收藏功能
    await toggleFavorite(1)

    // 注意：这里需要根据实际的API实现来调整
    // 假设有favoriteArticle API
  })

  it('应该正确处理文章举报功能', async () => {
    const { reportArticle } = require('@/api/article')
    reportArticle.mockResolvedValue({
      data: { success: true },
    })

    const { handleReport } = useArticle()
    const result = await handleReport(1, 'spam')

    expect(reportArticle).toHaveBeenCalledWith(1, 'spam')
    expect(result.success).toBe(true)
  })

  it('应该正确处理权限检查', async () => {
    // 模拟未登录用户
    localStorageMock.getItem.mockReturnValue(null)

    const { canEdit, canDelete } = useArticle(1, { authorId: 2 })

    expect(canEdit.value).toBe(false)
    expect(canDelete.value).toBe(false)

    // 模拟已登录用户
    const mockUser = { id: 2, role: 'user' }
    localStorageMock.getItem.mockReturnValue(JSON.stringify(mockUser))

    const { canEdit: canEdit2, canDelete: canDelete2 } = useArticle(1, { authorId: 2 })

    expect(canEdit2.value).toBe(true)
    expect(canDelete2.value).toBe(true)
  })

  it('应该正确处理管理员权限', async () => {
    const mockAdmin = { id: 1, role: 'admin' }
    localStorageMock.getItem.mockReturnValue(JSON.stringify(mockAdmin))

    const { canEdit, canDelete } = useArticle(1, { authorId: 2 })

    expect(canEdit.value).toBe(true) // 管理员可以编辑所有文章
    expect(canDelete.value).toBe(true) // 管理员可以删除所有文章
  })

  it('应该正确重置状态', () => {
    const { reset, article, author, tags, loading, error, isLiked } = useArticle()

    // 设置一些状态
    article.value = { id: 1, title: '测试' }
    author.value = { id: 1, nickname: '作者' }
    tags.value = [{ id: 1, name: 'Vue' }]
    loading.value = true
    error.value = '错误'
    isLiked.value = true

    // 重置状态
    reset()

    expect(article.value).toBe(null)
    expect(author.value).toBe(null)
    expect(tags.value).toEqual([])
    expect(loading.value).toBe(false)
    expect(error.value).toBe(null)
    expect(isLiked.value).toBe(false)
  })
})