import request from '@/utils/request'
import type {
  Comment,
  CreateCommentRequest,
  UpdateCommentRequest,
  CommentQuery,
  PaginationResult,
  ApiResponse
} from '@shared/types'

const BASE_URL = '/api/v1/comments'

export const commentService = {
  /**
   * 创建评论
   */
  createComment: (data: CreateCommentRequest): Promise<ApiResponse<Comment>> => {
    return request({
      url: BASE_URL,
      method: 'POST',
      data
    })
  },

  /**
   * 更新评论
   */
  updateComment: (id: number, data: UpdateCommentRequest): Promise<ApiResponse<Comment>> => {
    return request({
      url: `${BASE_URL}/${id}`,
      method: 'PUT',
      data
    })
  },

  /**
   * 删除评论
   */
  deleteComment: (id: number): Promise<ApiResponse<void>> => {
    return request({
      url: `${BASE_URL}/${id}`,
      method: 'DELETE'
    })
  },

  /**
   * 获取评论列表（分页）
   */
  getComments: (query: CommentQuery): Promise<ApiResponse<PaginationResult<Comment>>> => {
    return request({
      url: BASE_URL,
      method: 'GET',
      params: query
    })
  },

  /**
   * 获取嵌套评论列表
   */
  getNestedComments: (
    articleId: number,
    sortBy: 'createTime' | 'likeCount' = 'createTime',
    sortOrder: 'asc' | 'desc' = 'desc'
  ): Promise<ApiResponse<Comment[]>> => {
    return request({
      url: `${BASE_URL}/nested`,
      method: 'GET',
      params: { articleId, sortBy, sortOrder }
    })
  },

  /**
   * 获取文章的所有评论
   */
  getArticleComments: (articleId: number): Promise<ApiResponse<Comment[]>> => {
    return request({
      url: `${BASE_URL}/articles/${articleId}/all`,
      method: 'GET'
    })
  },

  /**
   * 获取我的评论
   */
  getMyComments: (
    page: number = 1,
    size: number = 10
  ): Promise<ApiResponse<PaginationResult<Comment>>> => {
    return request({
      url: `${BASE_URL}/user/me`,
      method: 'GET',
      params: { page, size }
    })
  },

  /**
   * 点赞评论
   */
  likeComment: (id: number): Promise<ApiResponse<void>> => {
    return request({
      url: `${BASE_URL}/${id}/like`,
      method: 'POST'
    })
  },

  /**
   * 取消点赞
   */
  unlikeComment: (id: number): Promise<ApiResponse<void>> => {
    return request({
      url: `${BASE_URL}/${id}/like`,
      method: 'DELETE'
    })
  },

  /**
   * 检查是否已点赞
   */
  checkIfLiked: (id: number): Promise<ApiResponse<{ liked: boolean }>> => {
    return request({
      url: `${BASE_URL}/${id}/liked`,
      method: 'GET'
    })
  },

  /**
   * 获取文章评论数量
   */
  getCommentCount: (articleId: number): Promise<ApiResponse<{ count: number }>> => {
    return request({
      url: `${BASE_URL}/articles/${articleId}/count`,
      method: 'GET'
    })
  }
}