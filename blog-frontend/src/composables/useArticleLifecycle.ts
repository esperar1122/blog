import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Article, ArticleStatusManagementResponse, SchedulePublishRequest } from '@/types/article'
import {
  publishArticle as apiPublishArticle,
  unpublishArticle as apiUnpublishArticle,
  pinArticle as apiPinArticle,
  unpinArticle as apiUnpinArticle,
  schedulePublishArticle as apiSchedulePublishArticle,
  softDeleteArticle as apiSoftDeleteArticle,
  restoreArticle as apiRestoreArticle
} from '@/api/article'

export function useArticleLifecycle() {
  const loading = ref(false)
  const currentArticle = ref<Article | null>(null)

  // 计算文章状态
  const articleStatus = computed(() => {
    if (!currentArticle.value) return 'unknown'
    return currentArticle.value.status
  })

  // 计算是否为草稿
  const isDraft = computed(() => {
    return articleStatus.value === 'DRAFT'
  })

  // 计算是否已发布
  const isPublished = computed(() => {
    return articleStatus.value === 'PUBLISHED'
  })

  // 计算是否已删除
  const isDeleted = computed(() => {
    return articleStatus.value === 'DELETED'
  })

  // 计算是否已置顶
  const isPinned = computed(() => {
    return currentArticle.value?.isTop || false
  })

  // 设置当前文章
  function setCurrentArticle(article: Article | null) {
    currentArticle.value = article
  }

  // 发布文章
  async function publishArticle(articleId: number) {
    try {
      loading.value = true
      const response = await apiPublishArticle(articleId)
      if (response.operationResult === 'success') {
        ElMessage.success(response.message)
        if (currentArticle.value && currentArticle.value.id === articleId) {
          currentArticle.value.status = 'PUBLISHED'
          currentArticle.value.publishTime = new Date().toISOString()
        }
        return response
      } else {
        ElMessage.error(response.message)
        return null
      }
    } catch (error: any) {
      ElMessage.error(error.message || '发布失败')
      return null
    } finally {
      loading.value = false
    }
  }

  // 下线文章
  async function unpublishArticle(articleId: number) {
    try {
      await ElMessageBox.confirm('确定要下线这篇文章吗？下线后文章将不再对外可见。', '确认下线', {
        type: 'warning'
      })

      loading.value = true
      const response = await apiUnpublishArticle(articleId)
      if (response.operationResult === 'success') {
        ElMessage.success(response.message)
        if (currentArticle.value && currentArticle.value.id === articleId) {
          currentArticle.value.status = 'DRAFT'
        }
        return response
      } else {
        ElMessage.error(response.message)
        return null
      }
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error(error.message || '下线失败')
      }
      return null
    } finally {
      loading.value = false
    }
  }

  // 置顶文章
  async function pinArticle(articleId: number) {
    try {
      loading.value = true
      const response = await apiPinArticle(articleId)
      if (response.operationResult === 'success') {
        ElMessage.success(response.message)
        if (currentArticle.value && currentArticle.value.id === articleId) {
          currentArticle.value.isTop = true
        }
        return response
      } else {
        ElMessage.error(response.message)
        return null
      }
    } catch (error: any) {
      ElMessage.error(error.message || '置顶失败')
      return null
    } finally {
      loading.value = false
    }
  }

  // 取消置顶
  async function unpinArticle(articleId: number) {
    try {
      loading.value = true
      const response = await apiUnpinArticle(articleId)
      if (response.operationResult === 'success') {
        ElMessage.success(response.message)
        if (currentArticle.value && currentArticle.value.id === articleId) {
          currentArticle.value.isTop = false
        }
        return response
      } else {
        ElMessage.error(response.message)
        return null
      }
    } catch (error: any) {
      ElMessage.error(error.message || '取消置顶失败')
      return null
    } finally {
      loading.value = false
    }
  }

  // 定时发布
  async function schedulePublish(articleId: number, scheduledTime: string) {
    try {
      loading.value = true
      const data: SchedulePublishRequest = {
        articleId,
        scheduledPublishTime: scheduledTime
      }
      const response = await apiSchedulePublishArticle(articleId, data)
      if (response.operationResult === 'success') {
        ElMessage.success(response.message)
        if (currentArticle.value && currentArticle.value.id === articleId) {
          // 扩展 Article 接口以支持 scheduledPublishTime
          ;(currentArticle.value as any).scheduledPublishTime = scheduledTime
        }
        return response
      } else {
        ElMessage.error(response.message)
        return null
      }
    } catch (error: any) {
      ElMessage.error(error.message || '定时发布设置失败')
      return null
    } finally {
      loading.value = false
    }
  }

  // 软删除文章
  async function softDeleteArticle(articleId: number) {
    try {
      await ElMessageBox.confirm('确定要删除这篇文章吗？删除后可以在回收站中恢复。', '确认删除', {
        type: 'warning'
      })

      loading.value = true
      const response = await apiSoftDeleteArticle(articleId)
      if (response.operationResult === 'success') {
        ElMessage.success(response.message)
        if (currentArticle.value && currentArticle.value.id === articleId) {
          currentArticle.value.status = 'DELETED'
          currentArticle.value.isTop = false
        }
        return response
      } else {
        ElMessage.error(response.message)
        return null
      }
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error(error.message || '删除失败')
      }
      return null
    } finally {
      loading.value = false
    }
  }

  // 恢复文章
  async function restoreArticle(articleId: number) {
    try {
      await ElMessageBox.confirm('确定要恢复这篇文章吗？恢复后文章将变为草稿状态。', '确认恢复', {
        type: 'info'
      })

      loading.value = true
      const response = await apiRestoreArticle(articleId)
      if (response.operationResult === 'success') {
        ElMessage.success(response.message)
        if (currentArticle.value && currentArticle.value.id === articleId) {
          currentArticle.value.status = 'DRAFT'
        }
        return response
      } else {
        ElMessage.error(response.message)
        return null
      }
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error(error.message || '恢复失败')
      }
      return null
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    currentArticle,
    articleStatus,
    isDraft,
    isPublished,
    isDeleted,
    isPinned,
    setCurrentArticle,
    publishArticle,
    unpublishArticle,
    pinArticle,
    unpinArticle,
    schedulePublish,
    softDeleteArticle,
    restoreArticle
  }
}