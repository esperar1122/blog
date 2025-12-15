import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useArticleStore } from '@/stores/article'
import { ElMessage } from 'element-plus'

export function useArticle(articleId?: string | number) {
  const articleStore = useArticleStore()

  // 响应式数据
  const loading = ref(false)
  const saving = ref(false)
  const draftData = ref<any>(null)
  const autoSaveTimer = ref<number | null>(null)
  const lastSaveTime = ref<Date | null>(null)

  // 计算属性
  const article = computed(() => articleStore.currentArticle)
  const categories = computed(() => articleStore.categories)
  const tags = computed(() => articleStore.tags)

  // 自动保存功能
  const startAutoSave = (data: any) => {
    if (autoSaveTimer.value) {
      clearTimeout(autoSaveTimer.value)
    }

    // 5分钟后自动保存
    autoSaveTimer.value = window.setTimeout(() => {
      autoSave(data)
    }, 300000)
  }

  const autoSave = async (data: any) => {
    try {
      saving.value = true
      draftData.value = { ...data, status: 'DRAFT' }

      if (articleId) {
        await articleStore.updateArticle(Number(articleId), draftData.value)
      } else {
        const newArticle = await articleStore.createArticle(draftData.value)
        draftData.value.id = newArticle.id
      }

      lastSaveTime.value = new Date()
      ElMessage.success('自动保存成功')
    } catch (error: any) {
      ElMessage.warning('自动保存失败，请检查网络连接')
    } finally {
      saving.value = false
    }
  }

  // 手动保存草稿
  const saveDraft = async (data: any) => {
    try {
      saving.value = true
      const articleData = { ...data, status: 'DRAFT' }

      if (articleId) {
        await articleStore.updateArticle(Number(articleId), articleData)
        ElMessage.success('草稿更新成功')
      } else {
        const newArticle = await articleStore.createArticle(articleData)
        ElMessage.success('草稿保存成功')
        return newArticle
      }
    } catch (error: any) {
      ElMessage.error(error.message || '保存失败')
      throw error
    } finally {
      saving.value = false
    }
  }

  // 发布文章
  const publish = async (data: any) => {
    try {
      saving.value = true

      if (articleId) {
        // 先更新文章内容
        await articleStore.updateArticle(Number(articleId), { ...data, status: 'DRAFT' })
        // 再发布
        await articleStore.publishArticle(Number(articleId))
        ElMessage.success('文章发布成功')
      } else {
        // 创建新文章并直接发布
        const newArticle = await articleStore.createArticle({ ...data, status: 'PUBLISHED' })
        ElMessage.success('文章创建并发布成功')
        return newArticle
      }
    } catch (error: any) {
      ElMessage.error(error.message || '发布失败')
      throw error
    } finally {
      saving.value = false
    }
  }

  // 加载文章数据
  const loadArticle = async (id: string | number) => {
    try {
      loading.value = true
      await articleStore.fetchArticle(id)
    } catch (error: any) {
      ElMessage.error(error.message || '加载文章失败')
      throw error
    } finally {
      loading.value = false
    }
  }

  // 加载分类和标签
  const loadCategoriesAndTags = async () => {
    try {
      await Promise.all([
        articleStore.fetchCategories(),
        articleStore.fetchTags()
      ])
    } catch (error: any) {
      ElMessage.error('加载分类和标签失败')
    }
  }

  // 清理资源
  const cleanup = () => {
    if (autoSaveTimer.value) {
      clearTimeout(autoSaveTimer.value)
      autoSaveTimer.value = null
    }
  }

  // 格式化文章数据用于提交
  const formatArticleData = (formData: any) => {
    return {
      title: formData.title?.trim(),
      content: formData.content?.trim(),
      summary: formData.summary?.trim(),
      coverImage: formData.coverImage?.trim(),
      categoryId: formData.categoryId,
      tagIds: formData.tagIds || [],
      status: formData.status || 'DRAFT'
    }
  }

  // 从URL插入图片
  const insertImageFromUrl = (url: string, alt?: string) => {
    return `![${alt || '图片'}](${url})`
  }

  // 从URL插入链接
  const insertLinkFromUrl = (text: string, url: string) => {
    return `[${text}](${url})`
  }

  // 生命周期
  onMounted(() => {
    if (articleId) {
      loadArticle(articleId)
    }
    loadCategoriesAndTags()
  })

  onUnmounted(() => {
    cleanup()
  })

  return {
    // 响应式数据
    loading,
    saving,
    article,
    categories,
    tags,
    draftData,
    lastSaveTime,

    // 方法
    startAutoSave,
    autoSave,
    saveDraft,
    publish,
    loadArticle,
    loadCategoriesAndTags,
    cleanup,
    formatArticleData,
    insertImageFromUrl,
    insertLinkFromUrl
  }
}