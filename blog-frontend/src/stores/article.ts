import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Article, Category, Tag, PaginationParams, PaginationResult } from 'blog-shared'
import { getArticles, getArticle, getCategories, getTags, createArticle, updateArticle, publishArticle } from '@/api/article'

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
  async function fetchArticleDetail(id: number | string) {
    try {
      const article = await getArticle(Number(id))
      currentArticle.value = article
      return Promise.resolve(article)
    } catch (error) {
      return Promise.reject(error)
    }
  }

  // 获取单篇文章
  async function fetchArticle(id: number | string) {
    return fetchArticleDetail(id)
  }

  // 创建文章
  async function createNewArticle(data: any) {
    try {
      const article = await createArticle(data)
      return Promise.resolve(article)
    } catch (error) {
      return Promise.reject(error)
    }
  }

  // 更新文章
  async function updateExistingArticle(id: number, data: any) {
    try {
      const article = await updateArticle(id, data)
      return Promise.resolve(article)
    } catch (error) {
      return Promise.reject(error)
    }
  }

  // 发布文章
  async function publishExistingArticle(id: number) {
    try {
      await publishArticle(id)
      return Promise.resolve()
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
    fetchArticle,
    createArticle: createNewArticle,
    updateArticle: updateExistingArticle,
    publishArticle: publishExistingArticle,
    fetchCategories,
    fetchTags
  }
})