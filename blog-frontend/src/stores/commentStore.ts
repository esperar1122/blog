import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { commentService } from '@/services/commentService'
import type {
  Comment,
  CreateCommentRequest,
  UpdateCommentRequest,
  CommentQuery,
  PaginationResult
} from '@shared/types'

export const useCommentStore = defineStore('comment', () => {
  // State
  const comments = ref<Comment[]>([])
  const currentComments = ref<Comment[]>([])
  const commentCount = ref(0)
  const loading = ref(false)
  const pagination = ref({
    page: 1,
    size: 20,
    total: 0,
    totalPages: 0
  })

  // Getters
  const hasComments = computed(() => comments.value.length > 0)
  const topLevelComments = computed(() =>
    currentComments.value.filter(comment => !comment.parentId)
  )

  // Actions
  /**
   * 获取文章的嵌套评论
   */
  const fetchNestedComments = async (
    articleId: number,
    sortBy: 'createTime' | 'likeCount' = 'createTime',
    sortOrder: 'asc' | 'desc' = 'desc'
  ) => {
    loading.value = true
    try {
      const response = await commentService.getNestedComments(articleId, sortBy, sortOrder)
      if (response.success) {
        currentComments.value = response.data || []
      }
    } catch (error) {
      console.error('获取评论失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取分页评论
   */
  const fetchComments = async (query: CommentQuery) => {
    loading.value = true
    try {
      const response = await commentService.getComments(query)
      if (response.success) {
        const paginationData = response.data as PaginationResult<Comment>
        currentComments.value = paginationData.list
        pagination.value = {
          page: paginationData.page,
          size: paginationData.size,
          total: paginationData.total,
          totalPages: paginationData.totalPages
        }
      }
    } catch (error) {
      console.error('获取评论失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建评论
   */
  const createComment = async (request: CreateCommentRequest) => {
    loading.value = true
    try {
      const response = await commentService.createComment(request)
      if (response.success) {
        const newComment = response.data
        if (request.parentId) {
          // 如果是回复，添加到对应父评论的回复列表中
          addReplyToComment(newComment)
        } else {
          // 如果是顶级评论，添加到开头
          currentComments.value.unshift(newComment)
        }

        // 更新评论数量
        commentCount.value += 1

        return newComment
      }
      throw new Error('创建评论失败')
    } catch (error) {
      console.error('创建评论失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新评论
   */
  const updateComment = async (id: number, request: UpdateCommentRequest) => {
    try {
      const response = await commentService.updateComment(id, request)
      if (response.success) {
        updateCommentInList(response.data)
        return response.data
      }
      throw new Error('更新评论失败')
    } catch (error) {
      console.error('更新评论失败:', error)
      throw error
    }
  }

  /**
   * 删除评论
   */
  const deleteComment = async (id: number) => {
    try {
      await commentService.deleteComment(id)
      removeCommentFromList(id)
      commentCount.value = Math.max(0, commentCount.value - 1)
    } catch (error) {
      console.error('删除评论失败:', error)
      throw error
    }
  }

  /**
   * 点赞评论
   */
  const likeComment = async (id: number) => {
    try {
      await commentService.likeComment(id)
      incrementCommentLikeCount(id)
    } catch (error) {
      console.error('点赞评论失败:', error)
      throw error
    }
  }

  /**
   * 取消点赞
   */
  const unlikeComment = async (id: number) => {
    try {
      await commentService.unlikeComment(id)
      decrementCommentLikeCount(id)
    } catch (error) {
      console.error('取消点赞失败:', error)
      throw error
    }
  }

  /**
   * 获取评论数量
   */
  const fetchCommentCount = async (articleId: number) => {
    try {
      const response = await commentService.getCommentCount(articleId)
      if (response.success) {
        commentCount.value = response.data.count
      }
    } catch (error) {
      console.error('获取评论数量失败:', error)
    }
  }

  /**
   * 清空评论列表
   */
  const clearComments = () => {
    currentComments.value = []
    commentCount.value = 0
    pagination.value = {
      page: 1,
      size: 20,
      total: 0,
      totalPages: 0
    }
  }

  // Helper methods
  const addReplyToComment = (reply: Comment) => {
    const findAndAddReply = (comments: Comment[]): boolean => {
      for (const comment of comments) {
        if (comment.id === reply.parentId) {
          if (!comment.replies) {
            comment.replies = []
          }
          comment.replies.push(reply)
          return true
        }
        if (comment.replies && findAndAddReply(comment.replies)) {
          return true
        }
      }
      return false
    }

    findAndAddReply(currentComments.value)
  }

  const updateCommentInList = (updatedComment: Comment) => {
    const findAndUpdate = (comments: Comment[]): boolean => {
      for (let i = 0; i < comments.length; i++) {
        if (comments[i].id === updatedComment.id) {
          comments[i] = { ...comments[i], ...updatedComment }
          return true
        }
        if (comments[i].replies && findAndUpdate(comments[i].replies)) {
          return true
        }
      }
      return false
    }

    findAndUpdate(currentComments.value)
  }

  const removeCommentFromList = (commentId: number) => {
    const findAndRemove = (comments: Comment[]): boolean => {
      for (let i = 0; i < comments.length; i++) {
        if (comments[i].id === commentId) {
          comments.splice(i, 1)
          return true
        }
        if (comments[i].replies && findAndRemove(comments[i].replies)) {
          return true
        }
      }
      return false
    }

    findAndRemove(currentComments.value)
  }

  const incrementCommentLikeCount = (commentId: number) => {
    const findAndIncrement = (comments: Comment[]): boolean => {
      for (const comment of comments) {
        if (comment.id === commentId) {
          comment.likeCount = (comment.likeCount || 0) + 1
          return true
        }
        if (comment.replies && findAndIncrement(comment.replies)) {
          return true
        }
      }
      return false
    }

    findAndIncrement(currentComments.value)
  }

  const decrementCommentLikeCount = (commentId: number) => {
    const findAndDecrement = (comments: Comment[]): boolean => {
      for (const comment of comments) {
        if (comment.id === commentId) {
          comment.likeCount = Math.max(0, (comment.likeCount || 0) - 1)
          return true
        }
        if (comment.replies && findAndDecrement(comment.replies)) {
          return true
        }
      }
      return false
    }

    findAndDecrement(currentComments.value)
  }

  return {
    // State
    comments,
    currentComments,
    commentCount,
    loading,
    pagination,

    // Getters
    hasComments,
    topLevelComments,

    // Actions
    fetchNestedComments,
    fetchComments,
    createComment,
    updateComment,
    deleteComment,
    likeComment,
    unlikeComment,
    fetchCommentCount,
    clearComments
  }
})