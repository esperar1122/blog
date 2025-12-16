import { ref, reactive, computed } from 'vue'
import {
  getArticles,
  searchArticles,
  getMyArticles,
  likeArticle,
  deleteArticle as deleteArticleApi,
  publishArticle as publishArticleApi,
  unpublishArticle,
  setArticleTop
} from '@/api/article'
import type { ArticleSummary, ArticleListResponse, ArticleQueryParams } from '@/types/article'
import { ElMessage } from 'element-plus'

export function useArticleList(type: 'public' | 'my' = 'public') {
  // 响应式数据
  const loading = ref(false)
  const articles = ref<ArticleSummary[]>([])
  const totalPages = ref(1)
  const totalElements = ref(0)
  const currentPage = ref(1)

  // 查询参数
  const queryParams = reactive<ArticleQueryParams>({
    page: 0,
    size: 10,
    sortBy: 'publishTime',
    sortDir: 'desc',
    status: 'PUBLISHED',
    keyword: ''
  })

  // 计算属性
  const hasMore = computed(() => currentPage.value < totalPages.value)
  const isEmpty = computed(() => !loading.value && articles.value.length === 0)

  // 获取文章列表
  const fetchArticles = async (params?: Partial<ArticleQueryParams>) => {
    loading.value = true
    try {
      const mergedParams = { ...queryParams, ...params }

      let response: ArticleListResponse

      if (type === 'my') {
        response = await getMyArticles(mergedParams)
      } else if (mergedParams.keyword) {
        response = await searchArticles(mergedParams)
      } else {
        response = await getArticles(mergedParams)
      }

      articles.value = response.content
      totalPages.value = response.totalPages
      totalElements.value = response.totalElements
      currentPage.value = response.number + 1

      // 更新查询参数
      Object.assign(queryParams, mergedParams)
    } catch (error: any) {
      ElMessage.error(error.message || '获取文章列表失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 搜索文章
  const search = async (keyword: string) => {
    return fetchArticles({ keyword, page: 0 })
  }

  // 刷新列表
  const refresh = () => {
    return fetchArticles()
  }

  // 加载更多
  const loadMore = async () => {
    if (!hasMore.value || loading.value) return

    const nextPage = currentPage.value + 1
    const mergedParams = { ...queryParams, page: nextPage - 1 }

    try {
      loading.value = true
      let response: ArticleListResponse

      if (type === 'my') {
        response = await getMyArticles(mergedParams)
      } else if (mergedParams.keyword) {
        response = await searchArticles(mergedParams)
      } else {
        response = await getArticles(mergedParams)
      }

      articles.value.push(...response.content)
      currentPage.value = response.number + 1
    } catch (error: any) {
      ElMessage.error(error.message || '加载更多失败')
    } finally {
      loading.value = false
    }
  }

  // 切换页面
  const goToPage = (page: number) => {
    return fetchArticles({ page: page - 1 })
  }

  // 排序
  const sort = (sortBy: string, sortDir: string = 'desc') => {
    return fetchArticles({
      sortBy: sortBy as ArticleQueryParams['sortBy'],
      sortDir: sortDir as 'asc' | 'desc'
    })
  }

  // 筛选
  const filter = (filters: Partial<ArticleQueryParams>) => {
    return fetchArticles({ ...filters, page: 0 })
  }

  // 点赞文章
  const toggleLike = async (articleId: number) => {
    try {
      const response = await likeArticle(articleId)

      // 更新本地文章列表中的点赞状态
      const article = articles.value.find(a => a.id === articleId)
      if (article) {
        if (response.liked) {
          article.likeCount = (article.likeCount || 0) + 1
        } else {
          article.likeCount = Math.max(0, (article.likeCount || 0) - 1)
        }
      }

      return response.liked
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
      throw error
    }
  }

  // 删除文章
  const deleteArticle = async (articleId: number) => {
    try {
      await deleteArticleApi(articleId)
      articles.value = articles.value.filter(a => a.id !== articleId)
      totalElements.value = Math.max(0, totalElements.value - 1)
      ElMessage.success('删除成功')
    } catch (error: any) {
      ElMessage.error(error.message || '删除失败')
      throw error
    }
  }

  // 发布文章
  const publishArticle = async (articleId: number) => {
    try {
      await publishArticleApi(articleId)

      const article = articles.value.find(a => a.id === articleId)
      if (article) {
        article.status = 'PUBLISHED'
      }

      ElMessage.success('发布成功')
    } catch (error: any) {
      ElMessage.error(error.message || '发布失败')
      throw error
    }
  }

  // 取消发布文章
  const unpublishArticle = async (articleId: number) => {
    try {
      await unpublishArticle(articleId)

      const article = articles.value.find(a => a.id === articleId)
      if (article) {
        article.status = 'DRAFT'
      }

      ElMessage.success('取消发布成功')
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
      throw error
    }
  }

  // 切换置顶
  const toggleTop = async (articleId: number, isTop: boolean) => {
    try {
      await setArticleTop(articleId, isTop)

      const article = articles.value.find(a => a.id === articleId)
      if (article) {
        article.isTop = isTop
      }

      ElMessage.success(isTop ? '置顶成功' : '取消置顶成功')
    } catch (error: any) {
      ElMessage.error(error.message || '操作失败')
      throw error
    }
  }

  // 重置筛选条件
  const resetFilters = () => {
    Object.assign(queryParams, {
      page: 0,
      size: 10,
      sortBy: 'publishTime',
      sortDir: 'desc',
      status: type === 'my' ? 'ALL' : 'PUBLISHED',
      keyword: '',
      categoryId: undefined,
      tagId: undefined,
      authorId: undefined,
      isTop: undefined
    })

    return fetchArticles()
  }

  return {
    // 响应式数据
    loading,
    articles,
    totalPages,
    totalElements,
    currentPage,
    queryParams,

    // 计算属性
    hasMore,
    isEmpty,

    // 方法
    fetchArticles,
    search,
    refresh,
    loadMore,
    goToPage,
    sort,
    filter,
    toggleLike,
    deleteArticle,
    publishArticle,
    unpublishArticle,
    toggleTop,
    resetFilters
  }
}