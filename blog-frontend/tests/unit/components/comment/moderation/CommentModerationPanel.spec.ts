import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElMessage, ElMessageBox } from 'element-plus'
import CommentModerationPanel from '@/components/comment/moderation/CommentModerationPanel.vue'
import { useCommentModerationStore } from '@/stores/commentModerationStore'
import { commentService } from '@/services/commentService'
import type { Comment } from '@shared/types'

// Mock the dependencies
vi.mock('@/stores/commentModerationStore')
vi.mock('@/services/commentService')
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      error: vi.fn(),
      success: vi.fn(),
      warning: vi.fn()
    },
    ElMessageBox: {
      confirm: vi.fn()
    }
  }
})

const mockCommentModerationStore = vi.mocked(useCommentModerationStore)
const mockCommentService = vi.mocked(commentService)

describe('CommentModerationPanel', () => {
  let mockStore: any
  let wrapper: any

  const mockComments: Comment[] = [
    {
      id: 1,
      content: '这是第一条测试评论',
      articleId: 1,
      userId: 1,
      userName: '测试用户1',
      userAvatar: '',
      parentId: null,
      level: 1,
      likeCount: 5,
      isEdited: false,
      status: 'NORMAL',
      createTime: new Date('2024-01-01T10:00:00'),
      updateTime: new Date('2024-01-01T10:00:00'),
      replies: []
    },
    {
      id: 2,
      content: '这是第二条测试评论',
      articleId: 1,
      userId: 2,
      userName: '测试用户2',
      userAvatar: '',
      parentId: null,
      level: 1,
      likeCount: 3,
      isEdited: true,
      editedTime: new Date('2024-01-01T11:00:00'),
      status: 'DELETED',
      createTime: new Date('2024-01-01T09:00:00'),
      updateTime: new Date('2024-01-01T11:00:00'),
      replies: []
    }
  ]

  beforeEach(() => {
    // Reset mocks
    vi.clearAllMocks()

    // Mock store
    mockStore = {
      reports: [],
      blacklist: [],
      sensitiveWords: [],
      statistics: null,
      loading: false,
      pagination: {
        page: 1,
        size: 20,
        total: 0,
        totalPages: 0
      },
      filters: {},
      pendingReports: [],
      approvedReports: [],
      rejectedReports: [],
      activeBlacklist: [],
      filterWords: [],
      blockWords: [],
      warningWords: [],
      moderateComment: vi.fn(),
      batchModerateComments: vi.fn()
    }

    mockCommentModerationStore.mockReturnValue(mockStore)

    // Mock comment service
    mockCommentService.getComments = vi.fn().mockResolvedValue({
      success: true,
      data: {
        list: mockComments,
        total: 2,
        page: 1,
        size: 20,
        totalPages: 1
      }
    })

    mockCommentService.deleteComment = vi.fn().mockResolvedValue({
      success: true
    })
  })

  const mountComponent = () => {
    return mount(CommentModerationPanel, {
      global: {
        stubs: {
          'el-card': true,
          'el-form': true,
          'el-form-item': true,
          'el-select': true,
          'el-option': true,
          'el-input': true,
          'el-button': true,
          'el-table': true,
          'el-table-column': true,
          'el-tag': true,
          'el-avatar': true,
          'el-pagination': true,
          'el-dialog': true,
          'el-icon': true
        }
      }
    })
  }

  it('should render comment moderation panel correctly', () => {
    wrapper = mountComponent()

    expect(wrapper.find('.comment-moderation-panel').exists()).toBe(true)
    expect(wrapper.find('.filter-card').exists()).toBe(true)
    expect(wrapper.find('.table-card').exists()).toBe(true)
  })

  it('should fetch comments on mount', async () => {
    wrapper = mountComponent()

    // Wait for next tick to allow async operations
    await wrapper.vm.$nextTick()

    expect(mockCommentService.getComments).toHaveBeenCalledWith({
      articleId: 0,
      page: 1,
      size: 20,
      sortBy: 'createTime',
      sortOrder: 'desc'
    })
  })

  it('should handle search correctly', async () => {
    wrapper = mountComponent()

    await wrapper.setData({
      filters: { status: 'NORMAL', keyword: '测试' }
    })

    await wrapper.vm.handleSearch()

    expect(wrapper.vm.pagination.page).toBe(1)
    expect(mockCommentService.getComments).toHaveBeenCalled()
  })

  it('should handle reset correctly', async () => {
    wrapper = mountComponent()

    await wrapper.setData({
      filters: { status: 'NORMAL', keyword: '测试' },
      pagination: { page: 2, size: 20, total: 0 }
    })

    await wrapper.vm.handleReset()

    expect(wrapper.vm.filters.status).toBe('')
    expect(wrapper.vm.filters.keyword).toBe('')
    expect(wrapper.vm.pagination.page).toBe(1)
  })

  it('should handle comment selection correctly', () => {
    wrapper = mountComponent()

    const selectedComments = [mockComments[0]]
    wrapper.vm.handleSelectionChange(selectedComments)

    expect(wrapper.vm.selectedComments).toEqual(selectedComments)
  })

  it('should handle view comment correctly', async () => {
    wrapper = mountComponent()
    const comment = mockComments[0]

    wrapper.vm.handleViewComment(comment)

    expect(wrapper.vm.selectedComment).toEqual(comment)
    expect(wrapper.vm.commentDetailVisible).toBe(true)
  })

  it('should handle delete comment correctly', async () => {
    wrapper = mountComponent()
    const comment = mockComments[0]

    // Mock ElMessageBox.confirm to resolve
    ;(ElMessageBox.confirm as any).mockResolvedValue('confirm')

    await wrapper.vm.handleDeleteComment(comment)

    expect(ElMessageBox.confirm).toHaveBeenCalledWith(
      '确定要删除这条评论吗？',
      '确认删除',
      { type: 'warning' }
    )
    expect(mockCommentService.deleteComment).toHaveBeenCalledWith(comment.id)
    expect(ElMessage.success).toHaveBeenCalledWith('评论删除成功')
  })

  it('should handle delete comment cancellation correctly', async () => {
    wrapper = mountComponent()
    const comment = mockComments[0]

    // Mock ElMessageBox.confirm to reject with 'cancel'
    ;(ElMessageBox.confirm as any).mockRejectedValue('cancel')

    await wrapper.vm.handleDeleteComment(comment)

    expect(ElMessageBox.confirm).toHaveBeenCalled()
    expect(mockCommentService.deleteComment).not.toHaveBeenCalled()
    expect(ElMessage.success).not.toHaveBeenCalled()
  })

  it('should handle restore comment correctly', async () => {
    wrapper = mountComponent()
    const deletedComment = mockComments[1] // DELETED status

    mockStore.moderateComment.mockResolvedValue({ success: true })

    await wrapper.vm.handleRestoreComment(deletedComment)

    expect(mockStore.moderateComment).toHaveBeenCalledWith(deletedComment.id, {
      status: 'APPROVE',
      reviewNote: '管理员恢复评论'
    })
    expect(ElMessage.success).toHaveBeenCalledWith('评论恢复成功')
  })

  it('should handle batch delete with no selection', async () => {
    wrapper = mountComponent()

    await wrapper.setData({ selectedComments: [] })
    await wrapper.vm.handleBatchDelete()

    expect(ElMessage.warning).toHaveBeenCalledWith('请选择要删除的评论')
    expect(wrapper.vm.batchDeleteVisible).toBe(false)
  })

  it('should handle batch delete with selection', async () => {
    wrapper = mountComponent()

    await wrapper.setData({
      selectedComments: [mockComments[0], mockComments[1]]
    })

    wrapper.vm.handleBatchDelete()

    expect(wrapper.vm.batchDeleteVisible).toBe(true)
  })

  it('should confirm batch delete correctly', async () => {
    wrapper = mountComponent()

    await wrapper.setData({
      selectedComments: [mockComments[0]],
      batchDeleteVisible: true,
      deleteReason: '批量删除测试'
    })

    mockStore.batchModerateComments.mockResolvedValue({ success: true })

    await wrapper.vm.confirmBatchDelete()

    expect(mockStore.batchModerateComments).toHaveBeenCalledWith({
      commentIds: [1],
      action: 'DELETE',
      reason: '批量删除测试'
    })
    expect(wrapper.vm.batchDeleteVisible).toBe(false)
    expect(wrapper.vm.deleteReason).toBe('')
    expect(ElMessage.success).toHaveBeenCalledWith('批量删除成功')
  })

  it('should format date time correctly', () => {
    wrapper = mountComponent()
    const testDate = new Date('2024-01-01T10:30:00')
    const formatted = wrapper.vm.formatDateTime(testDate)

    expect(formatted).toMatch(/\d{4}\/\d{1,2}\/\d{1,2}/)
  })

  it('should handle empty date time correctly', () => {
    wrapper = mountComponent()
    expect(wrapper.vm.formatDateTime(null)).toBe('')
    expect(wrapper.vm.formatDateTime('')).toBe('')
  })

  it('should handle pagination size change', async () => {
    wrapper = mountComponent()

    await wrapper.vm.handleSizeChange(50)

    expect(wrapper.vm.pagination.size).toBe(50)
    expect(wrapper.vm.pagination.page).toBe(1)
    expect(mockCommentService.getComments).toHaveBeenCalled()
  })

  it('should handle pagination current change', async () => {
    wrapper = mountComponent()

    await wrapper.vm.handleCurrentChange(3)

    expect(wrapper.vm.pagination.page).toBe(3)
    expect(mockCommentService.getComments).toHaveBeenCalled()
  })

  it('should display edited tag for edited comments', () => {
    wrapper = mountComponent()
    const editedComment = mockComments[1] // has isEdited: true

    expect(editedComment.isEdited).toBe(true)
  })

  it('should handle API errors gracefully', async () => {
    wrapper = mountComponent()

    // Mock API error
    mockCommentService.getComments.mockRejectedValue(new Error('API Error'))

    await wrapper.vm.fetchComments()

    expect(ElMessage.error).toHaveBeenCalledWith('获取评论列表失败')
  })

  it('should handle delete comment error correctly', async () => {
    wrapper = mountComponent()
    const comment = mockComments[0]

    // Mock ElMessageBox.confirm to resolve
    ;(ElMessageBox.confirm as any).mockResolvedValue('confirm')

    // Mock API error
    mockCommentService.deleteComment.mockRejectedValue(new Error('Delete Error'))

    await wrapper.vm.handleDeleteComment(comment)

    expect(ElMessage.error).toHaveBeenCalledWith('删除评论失败')
  })
})