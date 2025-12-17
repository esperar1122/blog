import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { commentModerationService } from '@/services/commentModerationService'
import type {
  CommentReport,
  CommentBlacklist,
  SensitiveWord,
  CommentModerationQuery,
  CommentStatistics,
  CommentReportRequest,
  CommentModerationRequest,
  BlacklistRequest,
  SensitiveWordRequest,
  BatchModerationRequest,
  PaginationResult
} from '@shared/types'

export const useCommentModerationStore = defineStore('commentModeration', () => {
  // State
  const reports = ref<CommentReport[]>([])
  const blacklist = ref<CommentBlacklist[]>([])
  const sensitiveWords = ref<SensitiveWord[]>([])
  const statistics = ref<CommentStatistics | null>(null)
  const loading = ref(false)
  const pagination = ref({
    page: 1,
    size: 10,
    total: 0,
    totalPages: 0
  })

  // Filters
  const filters = ref<CommentModerationQuery>({})

  // Getters
  const pendingReports = computed(() =>
    reports.value.filter(report => report.status === 'PENDING')
  )

  const approvedReports = computed(() =>
    reports.value.filter(report => report.status === 'APPROVED')
  )

  const rejectedReports = computed(() =>
    reports.value.filter(report => report.status === 'REJECTED')
  )

  const activeBlacklist = computed(() =>
    blacklist.value.filter(entry => !entry.expireTime || new Date(entry.expireTime) > new Date())
  )

  const filterWords = computed(() =>
    sensitiveWords.value.filter(word => word.type === 'FILTER')
  )

  const blockWords = computed(() =>
    sensitiveWords.value.filter(word => word.type === 'BLOCK')
  )

  const warningWords = computed(() =>
    sensitiveWords.value.filter(word => word.type === 'WARNING')
  )

  // Actions
  /**
   * 获取举报列表
   */
  const fetchReports = async (query: CommentModerationQuery = {}) => {
    loading.value = true
    try {
      const response = await commentModerationService.getReports({
        ...filters.value,
        ...query,
        page: query.page || pagination.value.page,
        size: query.size || pagination.value.size
      })

      if (response.success) {
        reports.value = response.data?.list || []
        pagination.value = {
          page: response.data?.page || 1,
          size: response.data?.size || 10,
          total: response.data?.total || 0,
          totalPages: response.data?.totalPages || 0
        }
      }
    } catch (error) {
      console.error('获取举报列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 举报评论
   */
  const reportComment = async (commentId: number, data: CommentReportRequest) => {
    try {
      const response = await commentModerationService.reportComment(commentId, data)
      if (response.success) {
        await fetchReports() // Refresh reports list
      }
      return response
    } catch (error) {
      console.error('举报评论失败:', error)
      throw error
    }
  }

  /**
   * 审核举报
   */
  const reviewReport = async (reportId: number, data: CommentModerationRequest) => {
    try {
      const response = await commentModerationService.reviewReport(reportId, data)
      if (response.success) {
        await fetchReports() // Refresh reports list
        await fetchStatistics() // Refresh statistics
      }
      return response
    } catch (error) {
      console.error('审核举报失败:', error)
      throw error
    }
  }

  /**
   * 审核评论
   */
  const moderateComment = async (commentId: number, data: CommentModerationRequest) => {
    try {
      const response = await commentModerationService.moderateComment(commentId, data)
      if (response.success) {
        await fetchReports() // Refresh reports list
        await fetchStatistics() // Refresh statistics
      }
      return response
    } catch (error) {
      console.error('审核评论失败:', error)
      throw error
    }
  }

  /**
   * 批量审核评论
   */
  const batchModerateComments = async (data: BatchModerationRequest) => {
    try {
      const response = await commentModerationService.batchModerateComments(data)
      if (response.success) {
        await fetchReports() // Refresh reports list
        await fetchStatistics() // Refresh statistics
      }
      return response
    } catch (error) {
      console.error('批量审核评论失败:', error)
      throw error
    }
  }

  /**
   * 获取黑名单
   */
  const fetchBlacklist = async () => {
    try {
      const response = await commentModerationService.getBlacklistedUsers()
      if (response.success) {
        blacklist.value = response.data || []
      }
    } catch (error) {
      console.error('获取黑名单失败:', error)
      throw error
    }
  }

  /**
   * 添加用户到黑名单
   */
  const addToBlacklist = async (userId: number, data: BlacklistRequest) => {
    try {
      const response = await commentModerationService.addToBlacklist(userId, data)
      if (response.success) {
        await fetchBlacklist() // Refresh blacklist
      }
      return response
    } catch (error) {
      console.error('添加黑名单失败:', error)
      throw error
    }
  }

  /**
   * 从黑名单移除用户
   */
  const removeFromBlacklist = async (userId: number) => {
    try {
      const response = await commentModerationService.removeFromBlacklist(userId)
      if (response.success) {
        await fetchBlacklist() // Refresh blacklist
      }
      return response
    } catch (error) {
      console.error('移除黑名单失败:', error)
      throw error
    }
  }

  /**
   * 获取敏感词列表
   */
  const fetchSensitiveWords = async (type?: string) => {
    try {
      const response = await commentModerationService.getSensitiveWords(type)
      if (response.success) {
        sensitiveWords.value = response.data || []
      }
    } catch (error) {
      console.error('获取敏感词失败:', error)
      throw error
    }
  }

  /**
   * 添加敏感词
   */
  const addSensitiveWord = async (data: SensitiveWordRequest) => {
    try {
      const response = await commentModerationService.addSensitiveWord(data)
      if (response.success) {
        await fetchSensitiveWords() // Refresh sensitive words
      }
      return response
    } catch (error) {
      console.error('添加敏感词失败:', error)
      throw error
    }
  }

  /**
   * 更新敏感词
   */
  const updateSensitiveWord = async (id: number, data: SensitiveWordRequest) => {
    try {
      const response = await commentModerationService.updateSensitiveWord(id, data)
      if (response.success) {
        await fetchSensitiveWords() // Refresh sensitive words
      }
      return response
    } catch (error) {
      console.error('更新敏感词失败:', error)
      throw error
    }
  }

  /**
   * 删除敏感词
   */
  const deleteSensitiveWord = async (id: number) => {
    try {
      const response = await commentModerationService.deleteSensitiveWord(id)
      if (response.success) {
        await fetchSensitiveWords() // Refresh sensitive words
      }
      return response
    } catch (error) {
      console.error('删除敏感词失败:', error)
      throw error
    }
  }

  /**
   * 过滤敏感词
   */
  const filterSensitiveWords = async (text: string) => {
    try {
      const response = await commentModerationService.filterSensitiveWords(text)
      return response
    } catch (error) {
      console.error('过滤敏感词失败:', error)
      throw error
    }
  }

  /**
   * 获取统计数据
   */
  const fetchStatistics = async () => {
    try {
      const response = await commentModerationService.getCommentStatistics()
      if (response.success) {
        statistics.value = response.data
      }
    } catch (error) {
      console.error('获取统计数据失败:', error)
      throw error
    }
  }

  /**
   * 获取举报统计数据
   */
  const fetchReportStatistics = async () => {
    try {
      const response = await commentModerationService.getReportStatistics()
      if (response.success) {
        statistics.value = response.data
      }
    } catch (error) {
      console.error('获取举报统计数据失败:', error)
      throw error
    }
  }

  /**
   * 更新过滤器
   */
  const updateFilters = (newFilters: Partial<CommentModerationQuery>) => {
    filters.value = { ...filters.value, ...newFilters }
  }

  /**
   * 重置过滤器
   */
  const resetFilters = () => {
    filters.value = {}
    pagination.value = {
      page: 1,
      size: 10,
      total: 0,
      totalPages: 0
    }
  }

  /**
   * 初始化数据
   */
  const initializeData = async () => {
    await Promise.all([
      fetchReports(),
      fetchBlacklist(),
      fetchSensitiveWords(),
      fetchStatistics()
    ])
  }

  return {
    // State
    reports,
    blacklist,
    sensitiveWords,
    statistics,
    loading,
    pagination,
    filters,

    // Getters
    pendingReports,
    approvedReports,
    rejectedReports,
    activeBlacklist,
    filterWords,
    blockWords,
    warningWords,

    // Actions
    fetchReports,
    reportComment,
    reviewReport,
    moderateComment,
    batchModerateComments,
    fetchBlacklist,
    addToBlacklist,
    removeFromBlacklist,
    fetchSensitiveWords,
    addSensitiveWord,
    updateSensitiveWord,
    deleteSensitiveWord,
    filterSensitiveWords,
    fetchStatistics,
    fetchReportStatistics,
    updateFilters,
    resetFilters,
    initializeData
  }
})