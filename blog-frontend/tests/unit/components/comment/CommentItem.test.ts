import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElMessage, ElMessageBox } from 'element-plus'
import CommentItem from '@/components/comment/CommentItem.vue'
import { useUserStore } from '@/stores/user'
import { useCommentStore } from '@/stores/commentStore'
import type { Comment } from '@shared/types'

// Mock stores
const mockUser = {
  id: 1,
  username: 'testuser',
  nickname: '测试用户',
  avatar: 'https://example.com/avatar.jpg'
}

const mockComment: Comment = {
  id: 1,
  content: '这是一条测试评论',
  articleId: 1,
  userId: 1,
  userName: '测试用户',
  userAvatar: 'https://example.com/avatar.jpg',
  level: 1,
  likeCount: 5,
  status: 'NORMAL',
  createTime: new Date('2025-12-17T10:00:00Z'),
  updateTime: new Date('2025-12-17T10:00:00Z'),
  replies: []
}

vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn(() => ({
    isLoggedIn: true,
    user: mockUser
  }))
}))

vi.mock('@/stores/commentStore', () => ({
  useCommentStore: vi.fn(() => ({
    likeComment: vi.fn().mockResolvedValue(undefined),
    unlikeComment: vi.fn().mockResolvedValue(undefined),
    deleteComment: vi.fn().mockResolvedValue(undefined)
  }))
}))

// Mock time formatter
vi.mock('@/utils/timeFormatter', () => ({
  formatCommentTime: vi.fn(() => ({
    relative: '2小时前',
    full: '2025-12-17 10:00:00'
  }))
}))

// Mock Element Plus
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      warning: vi.fn(),
      error: vi.fn()
    },
    ElMessageBox: {
      confirm: vi.fn()
    },
    ElIcon: {
      name: 'ElIcon',
      template: '<div></div>'
    },
    User: {
      name: 'User',
      template: '<div></div>'
    },
    Edit: {
      name: 'Edit',
      template: '<div></div>'
    },
    Delete: {
      name: 'Delete',
      template: '<div></div>'
    },
    MoreFilled: {
      name: 'MoreFilled',
      template: '<div></div>'
    }
  }
})

describe('CommentItem.vue', () => {
  let wrapper: any

  beforeEach(() => {
    wrapper = mount(CommentItem, {
      props: {
        comment: mockComment,
        currentUserId: 1,
        articleAuthorId: 1
      },
      global: {
        stubs: {
          'el-avatar': true,
          'el-tag': true,
          'el-button': true,
          'el-dropdown': true,
          'el-dropdown-menu': true,
          'el-dropdown-item': true,
          'el-icon': true,
          CommentForm: true
        }
      }
    })
  })

  afterEach(() => {
    wrapper.unmount()
  })

  it('renders comment correctly', () => {
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.comment-item').exists()).toBe(true)
    expect(wrapper.find('.comment-avatar').exists()).toBe(true)
    expect(wrapper.find('.comment-content').exists()).toBe(true)
  })

  it('displays comment author name', () => {
    expect(wrapper.text()).toContain('测试用户')
  })

  it('displays comment content', () => {
    expect(wrapper.text()).toContain('这是一条测试评论')
  })

  it('displays like count', () => {
    expect(wrapper.text()).toContain('5')
  })

  it('shows level tag for nested comments', async () => {
    const nestedComment = { ...mockComment, level: 2 }

    await wrapper.setProps({ comment: nestedComment })

    const levelTag = wrapper.findComponent({ name: 'ElTag' })
    expect(levelTag.exists()).toBe(true)
  })

  it('hides edit/delete buttons for other users comments', async () => {
    const otherUserComment = { ...mockComment, userId: 2 }

    await wrapper.setProps({ comment: otherUserComment })

    const dropdown = wrapper.findComponent({ name: 'ElDropdown' })
    expect(dropdown.exists()).toBe(false)
  })

  it('shows edit/delete buttons for own comments', () => {
    const dropdown = wrapper.findComponent({ name: 'ElDropdown' })
    expect(dropdown.exists()).toBe(true)
  })

  it('calls likeComment when like button is clicked', async () => {
    const commentStore = useCommentStore()

    await wrapper.vm.handleLike()

    expect(commentStore.likeComment).toHaveBeenCalledWith(1)
  })

  it('calls unlikeComment when already liked and unlike button is clicked', async () => {
    const commentStore = useCommentStore()

    // Set as already liked
    wrapper.vm.isLiked = true

    await wrapper.vm.handleLike()

    expect(commentStore.unlikeComment).toHaveBeenCalledWith(1)
  })

  it('shows reply form when reply button is clicked', async () => {
    expect(wrapper.vm.showReplyForm).toBe(false)

    await wrapper.vm.handleReply()

    expect(wrapper.vm.showReplyForm).toBe(true)
  })

  it('emits reply event', async () => {
    await wrapper.vm.handleReplyToReply({
      commentId: 1,
      userName: '测试用户'
    })

    expect(wrapper.emitted().reply).toBeTruthy()
    expect(wrapper.emitted().reply[0]).toEqual([{
      commentId: 1,
      userName: '测试用户'
    }])
  })

  it('hides reply form after successful reply', async () => {
    wrapper.vm.showReplyForm = true

    await wrapper.vm.handleReplySuccess()

    expect(wrapper.vm.showReplyForm).toBe(false)
  })

  it('hides reply form when cancel is clicked', async () => {
    wrapper.vm.showReplyForm = true

    await wrapper.vm.handleReplyCancel()

    expect(wrapper.vm.showReplyForm).toBe(false)
  })

  it('hides edit form after successful edit', async () => {
    wrapper.vm.showEditForm = true

    await wrapper.vm.handleEditSuccess()

    expect(wrapper.vm.showEditForm).toBe(false)
  })

  it('hides edit form when cancel is clicked', async () => {
    wrapper.vm.showEditForm = true

    await wrapper.vm.handleEditCancel()

    expect(wrapper.vm.showEditForm).toBe(false)
  })

  it('shows edit form when edit command is selected', async () => {
    await wrapper.vm.handleCommand('edit')

    expect(wrapper.vm.showEditForm).toBe(true)
  })

  it('shows delete confirmation when delete command is selected', async () => {
    // Mock confirmation dialog
    ;(ElMessageBox.confirm as any).mockResolvedValue('confirm')

    await wrapper.vm.handleCommand('delete')

    expect(ElMessageBox.confirm).toHaveBeenCalledWith(
      '确定要删除这条评论吗？删除后无法恢复。',
      '确认删除',
      expect.any(Object)
    )
  })

  it('calls deleteComment after confirmation', async () => {
    const commentStore = useCommentStore()

    // Mock confirmation dialog
    ;(ElMessageBox.confirm as any).mockResolvedValue('confirm')

    await wrapper.vm.handleCommand('delete')

    expect(commentStore.deleteComment).toHaveBeenCalledWith(1)
    expect(ElMessage.success).toHaveBeenCalledWith('评论删除成功')
  })

  it('does not delete comment when confirmation is cancelled', async () => {
    const commentStore = useCommentStore()

    // Mock cancelled confirmation
    ;(ElMessageBox.confirm as any).mockRejectedValue('cancel')

    await wrapper.vm.handleCommand('delete')

    expect(commentStore.deleteComment).not.toHaveBeenCalled()
  })

  it('shows error when delete fails', async () => {
    const commentStore = useCommentStore()

    // Mock delete failure
    commentStore.deleteComment = vi.fn().mockRejectedValue(new Error('删除失败'))

    // Mock confirmation dialog
    ;(ElMessageBox.confirm as any).mockResolvedValue('confirm')

    await wrapper.vm.handleCommand('delete')

    expect(ElMessage.error).toHaveBeenCalledWith('删除评论失败，请重试')
  })

  it('calculates correct margin based on comment level', async () => {
    const nestedComment = { ...mockComment, level: 3 }

    await wrapper.setProps({ comment: nestedComment })

    const commentItem = wrapper.find('.comment-item')
    const marginLeft = getComputedStyle(commentItem.element).marginLeft

    // Should be (3-1) * 20px = 40px
    expect(marginLeft).toBe('40px')
  })

  it('renders replies when they exist', async () => {
    const reply: Comment = {
      ...mockComment,
      id: 2,
      parentId: 1,
      level: 2,
      content: '这是一条回复'
    }

    const commentWithReplies = { ...mockComment, replies: [reply] }

    await wrapper.setProps({ comment: commentWithReplies })

    const repliesContainer = wrapper.find('.comment-replies')
    expect(repliesContainer.exists()).toBe(true)
  })

  it('shows warning for like action when not logged in', async () => {
    // Mock user not logged in
    ;(useUserStore as any).mockReturnValue({
      isLoggedIn: false,
      user: null
    })

    const wrapper = mount(CommentItem, {
      props: {
        comment: mockComment,
        currentUserId: null,
        articleAuthorId: 1
      },
      global: {
        stubs: {
          'el-avatar': true,
          'el-tag': true,
          'el-button': true,
          'el-dropdown': true,
          'el-dropdown-menu': true,
          'el-dropdown-item': true,
          'el-icon': true,
          CommentForm: true
        }
      }
    })

    await wrapper.vm.handleLike()

    expect(ElMessage.warning).toHaveBeenCalledWith('请先登录后再点赞')

    wrapper.unmount()
  })
})