import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElMessage } from 'element-plus'
import CommentForm from '@/components/comment/CommentForm.vue'
import { useUserStore } from '@/stores/user'
import { useCommentStore } from '@/stores/commentStore'
import type { CreateCommentRequest } from '@shared/types'

// Mock stores
vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn(() => ({
    isLoggedIn: true,
    user: {
      id: 1,
      username: 'testuser',
      nickname: '测试用户'
    }
  }))
}))

vi.mock('@/stores/commentStore', () => ({
  useCommentStore: vi.fn(() => ({
    createComment: vi.fn().mockResolvedValue({ id: 1, content: '测试评论' })
  }))
}))

// Mock Element Plus Message
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      warning: vi.fn(),
      error: vi.fn()
    }
  }
})

describe('CommentForm.vue', () => {
  let wrapper: any

  const defaultProps = {
    articleId: 1,
    placeholder: '写下你的评论...',
    submitText: '发表评论',
    showCancel: false
  }

  beforeEach(() => {
    wrapper = mount(CommentForm, {
      props: defaultProps,
      global: {
        stubs: {
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-button': true
        }
      }
    })
  })

  afterEach(() => {
    wrapper.unmount()
  })

  it('renders correctly with default props', () => {
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.find('.comment-form').exists()).toBe(true)
  })

  it('renders with custom props', () => {
    const customProps = {
      articleId: 123,
      placeholder: '自定义占位符',
      submitText: '发送',
      showCancel: true
    }

    const customWrapper = mount(CommentForm, {
      props: customProps,
      global: {
        stubs: {
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-button': true
        }
      }
    })

    expect(customWrapper.exists()).toBe(true)
    customWrapper.unmount()
  })

  it('shows warning when user is not logged in', async () => {
    // Mock user not logged in
    ;(useUserStore as any).mockReturnValue({
      isLoggedIn: false,
      user: null
    })

    const wrapper = mount(CommentForm, {
      props: defaultProps,
      global: {
        stubs: {
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-button': true
        }
      }
    })

    // Trigger submit
    await wrapper.vm.handleSubmit()

    expect(ElMessage.warning).toHaveBeenCalledWith('请先登录后再发表评论')
    wrapper.unmount()
  })

  it('validates comment content is required', async () => {
    const commentStore = useCommentStore()

    // Mock empty content validation error
    const validate = vi.fn().mockRejectedValue(new Error('内容不能为空'))
    wrapper.vm.formRef = { validate }

    await wrapper.vm.handleSubmit()

    expect(validate).toHaveBeenCalled()
  })

  it('calls comment store with correct data when submitting valid comment', async () => {
    const commentStore = useCommentStore()
    const userStore = useUserStore()

    // Mock form validation success
    const validate = vi.fn().mockResolvedValue(true)
    wrapper.vm.formRef = { validate, resetFields: vi.fn() }

    // Set form data
    wrapper.vm.form.content = '这是一条测试评论'

    await wrapper.vm.handleSubmit()

    expect(commentStore.createComment).toHaveBeenCalledWith({
      content: '这是一条测试评论',
      articleId: 1,
      parentId: undefined
    })

    expect(ElMessage.success).toHaveBeenCalledWith('评论发表成功')
  })

  it('handles submit with parent ID for reply', async () => {
    const commentStore = useCommentStore()

    const wrapper = mount(CommentForm, {
      props: {
        ...defaultProps,
        parentId: 123
      },
      global: {
        stubs: {
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-button': true
        }
      }
    })

    // Mock form validation success
    const validate = vi.fn().mockResolvedValue(true)
    wrapper.vm.formRef = { validate, resetFields: vi.fn() }

    wrapper.vm.form.content = '这是一条回复'
    wrapper.vm.form.parentId = 123

    await wrapper.vm.handleSubmit()

    expect(commentStore.createComment).toHaveBeenCalledWith({
      content: '这是一条回复',
      articleId: 1,
      parentId: 123
    })

    wrapper.unmount()
  })

  it('emits success event after successful submission', async () => {
    const commentStore = useCommentStore()

    // Mock form validation success
    const validate = vi.fn().mockResolvedValue(true)
    wrapper.vm.formRef = { validate, resetFields: vi.fn() }

    wrapper.vm.form.content = '测试评论内容'

    await wrapper.vm.handleSubmit()

    expect(wrapper.emitted().success).toBeTruthy()
    expect(wrapper.emitted().success[0]).toEqual([])
  })

  it('emits cancel event when cancel button is clicked', async () => {
    const wrapper = mount(CommentForm, {
      props: {
        ...defaultProps,
        showCancel: true
      },
      global: {
        stubs: {
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-button': true
        }
      }
    })

    await wrapper.vm.handleCancel()

    expect(wrapper.emitted().cancel).toBeTruthy()
    expect(wrapper.emitted().cancel[0]).toEqual([])
    wrapper.unmount()
  })

  it('shows error message when submission fails', async () => {
    const commentStore = useCommentStore()

    // Mock submission failure
    commentStore.createComment = vi.fn().mockRejectedValue(new Error('网络错误'))

    // Mock form validation success
    const validate = vi.fn().mockResolvedValue(true)
    wrapper.vm.formRef = { validate, resetFields: vi.fn() }

    wrapper.vm.form.content = '测试评论内容'

    await wrapper.vm.handleSubmit()

    expect(ElMessage.error).toHaveBeenCalledWith('发表评论失败，请重试')
  })

  it('resets form after successful submission', async () => {
    const commentStore = useCommentStore()
    const resetFields = vi.fn()

    // Mock form validation success
    const validate = vi.fn().mockResolvedValue(true)
    wrapper.vm.formRef = { validate, resetFields }

    wrapper.vm.form.content = '测试评论内容'

    await wrapper.vm.handleSubmit()

    expect(resetFields).toHaveBeenCalled()
    expect(wrapper.vm.form.content).toBe('')
  })

  it('exposes focus and reset methods', () => {
    expect(typeof wrapper.vm.focus).toBe('function')
    expect(typeof wrapper.vm.reset).toBe('function')
  })

  it('calls reset method correctly', () => {
    const resetFields = vi.fn()
    wrapper.vm.formRef = { resetFields }

    wrapper.vm.form.content = '测试内容'
    wrapper.vm.reset()

    expect(wrapper.vm.form.content).toBe('')
    expect(resetFields).toHaveBeenCalled()
  })
})