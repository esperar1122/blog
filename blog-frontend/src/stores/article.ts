import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Article, Category, Tag, PaginationParams, PaginationResult } from 'blog-shared'
import { getArticles, getArticle, getCategories, getTags } from '@/api/article'

export const useArticleStore = defineStore('article', () => {
  const articles = ref<Article[]>([])
  const currentArticle = ref<Article | null>(null)
  const categories = ref<Category[]>([])
  const tags = ref<Tag[]>([])
  const pagination = ref({
    page: 1,
    size: 10,
    total: 0,
    totalPages: 0
  })

  // 获取文章列表
  async function fetchArticles(params: PaginationParams & { categoryId?: number; tagId?: number; keyword?: string } = {}) {
    try {
      const response = await getArticles(params)
      articles.value = response.list
      pagination.value = {
        page: response.page,
        size: response.size,
        total: response.total,
        totalPages: response.totalPages
      }
      return Promise.resolve(response)
    } catch (error) {
      return Promise.reject(error)
    }
  }

  // 获取文章详情
  async function fetchArticleDetail(id: number) {
    try {
      const article = await getArticle(id)
      currentArticle.value = article
      return Promise.resolve(article)
    } catch (error) {
      return Promise.reject(error)
    }
  }

  // 获取分类列表
  async function fetchCategories() {
    try {
      const list = await getCategories()
      categories.value = list
      return Promise.resolve(list)
    } catch (error) {
      return Promise.reject(error)
    }
  }

  // 获取标签列表
  async function fetchTags() {
    try {
      const list = await getTags()
      tags.value = list
      return Promise.resolve(list)
    } catch (error) {
      return Promise.reject(error)
    }
  }

  return {
    articles,
    currentArticle,
    categories,
    tags,
    pagination,
    fetchArticles,
    fetchArticleDetail,
    fetchCategories,
    fetchTags
  }
})