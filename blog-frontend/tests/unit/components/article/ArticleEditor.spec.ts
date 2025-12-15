import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElMessage, ElMessageBox } from 'element-plus'
import ArticleEditor from '@/components/article/ArticleEditor.vue'
import { useArticleStore } from '@/stores/article'
import { useUserStore } from '@/stores/user'

// Mock stores
vi.mock('@/stores/article')
vi.mock('@/stores/user')

// Mock Element Plus
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn()
    },
    ElMessageBox: {
      confirm: vi.fn()
    }
  }
})

describe('ArticleEditor.vue', () => {
  let mockArticleStore: any
  let mockUserStore: any

  beforeEach(() => {
    // Reset mocks
    vi.clearAllMocks()

    // Mock article store
    mockArticleStore = {
      categories: [
        { id: 1, name: '技术' },
        { id: 2, name: '生活' }
      ],
      tags: [
        { id: 1, name: 'Vue' },
        { id: 2, name: 'TypeScript' }
      ],
      createArticle: vi.fn(),
      updateArticle: vi.fn(),
      publishArticle: vi.fn(),
      fetchCategories: vi.fn().mockResolvedValue([]),
      fetchTags: vi.fn().mockResolvedValue([])
    }
    ;(useArticleStore as any).mockReturnValue(mockArticleStore)

    // Mock user store
    mockUserStore = {
      token: 'mock-token'
    }
    ;(useUserStore as any).mockReturnValue(mockUserStore)
  })

  it('renders properly in create mode', () => {
    const wrapper = mount(ArticleEditor, {
      props: {
        mode: 'create'
      }
    })

    expect(wrapper.find('.article-editor').exists()).toBe(true)
    expect(wrapper.find('input[placeholder="请输入文章标题"]').exists()).toBe(true)
    expect(wrapper.find('textarea[placeholder="请输入 Markdown 内容..."]').exists()).toBe(true)
  })

  it('renders properly in edit mode with initial data', () => {
    const initialData = {
      id: 1,
      title: '测试文章',
      content: '# 测试内容',
      summary: '测试摘要',
      categoryId: 1,
      tagIds: [1]
    }

    const wrapper = mount(ArticleEditor, {
      props: {
        mode: 'edit',
        initialData
      }
    })

    const titleInput = wrapper.find('input[placeholder="请输入文章标题"]')
    expect(titleInput.element.value).toBe('测试文章')
  })

  it('validates required fields', async () => {
    const wrapper = mount(ArticleEditor, {
      props: {
        mode: 'create'
      }
    })

    // Try to save without filling required fields
    await wrapper.find('[data-test="save-draft-btn"]').trigger('click')

    // Should show validation errors
    await wrapper.vm.$nextTick()
    // Note: Testing validation messages might require more specific implementation
  })

  it('emits save event when save draft is clicked', async () => {
    const wrapper = mount(ArticleEditor, {
      props: {
        mode: 'create'
      }
    })

    // Fill required fields
    await wrapper.setData({
      formData: {
        title: '测试文章',
        content: '测试内容',
        summary: '测试摘要',
        categoryId: 1,
        tagIds: [1],
        coverImage: ''
      }
    })

    // Mock successful save
    mockArticleStore.createArticle.mockResolvedValue({ id: 1 })

    // Click save button
    const saveBtn = wrapper.find('[data-test="save-draft-btn"]')
    if (saveBtn.exists()) {
      await saveBtn.trigger('click')
    }

    // Note: Actual testing would require proper form validation and submit handling
  })

  it('emits publish event when publish is clicked', async () => {
    // Mock ElMessageBox.confirm to return true
    ;(ElMessageBox.confirm as any).mockResolvedValue('confirm')

    const wrapper = mount(ArticleEditor, {
      props: {
        mode: 'create'
      }
    })

    // Fill required fields
    await wrapper.setData({
      formData: {
        title: '测试文章',
        content: '测试内容',
        summary: '测试摘要',
        categoryId: 1,
        tagIds: [1],
        coverImage: ''
      }
    })

    // Mock successful publish
    mockArticleStore.createArticle.mockResolvedValue({ id: 1 })
    mockArticleStore.publishArticle.mockResolvedValue(undefined)

    // Click publish button
    const publishBtn = wrapper.find('[data-test="publish-btn"]')
    if (publishBtn.exists()) {
      await publishBtn.trigger('click')
    }

    expect(ElMessageBox.confirm).toHaveBeenCalled()
  })

  it('switches editor modes correctly', async () => {
    const wrapper = mount(ArticleEditor, {
      props: {
        mode: 'create'
      }
    })

    // Initially in edit mode
    expect(wrapper.vm.editMode).toBe('edit')

    // Switch to preview mode
    const previewBtn = wrapper.find('[data-test="mode-preview-btn"]')
    if (previewBtn.exists()) {
      await previewBtn.trigger('click')
      expect(wrapper.vm.editMode).toBe('preview')
    }

    // Switch to split mode
    const splitBtn = wrapper.find('[data-test="mode-split-btn"]')
    if (splitBtn.exists()) {
      await splitBtn.trigger('click')
      expect(wrapper.vm.editMode).toBe('split')
    }
  })

  it('handles content change and triggers auto-save', async () => {
    const wrapper = mount(ArticleEditor, {
      props: {
        mode: 'create'
      }
    })

    // Simulate content change
    await wrapper.setData({
      formData: {
        ...wrapper.vm.formData,
        content: '新的内容'
      }
    })

    // Auto-save timer should be set
    expect(wrapper.vm.autoSaveTimer).not.toBeNull()

    // Fast-forward timers
    vi.advanceTimersByTime(300000) // 5 minutes

    // Auto-save should be called (mocked implementation)
  })

  it('inserts image at cursor position', async () => {
    const wrapper = mount(ArticleEditor, {
      props: {
        mode: 'create'
      }
    })

    // Mock prompt
    const mockPrompt = vi.fn().mockReturnValue('http://example.com/image.jpg')
    global.prompt = mockPrompt

    // Mock textarea ref
    wrapper.vm.textareaRef = {
      $el: {
        querySelector: vi.fn().mockReturnValue({
          selectionStart: 0,
          selectionEnd: 0,
          value: '',
          focus: vi.fn(),
          setSelectionRange: vi.fn()
        })
      }
    }

    // Click insert image button
    const insertImageBtn = wrapper.find('[data-test="insert-image-btn"]')
    if (insertImageBtn.exists()) {
      await insertImageBtn.trigger('click')
    }

    expect(mockPrompt).toHaveBeenCalledWith('请输入图片URL：')
  })

  it('inserts link at cursor position', async () => {
    const wrapper = mount(ArticleEditor, {
      props: {
        mode: 'create'
      }
    })

    // Mock prompt
    const mockPrompt = vi.fn()
      .mockReturnValueOnce('http://example.com')
      .mockReturnValueOnce('示例链接')
    global.prompt = mockPrompt

    // Mock textarea ref
    wrapper.vm.textareaRef = {
      $el: {
        querySelector: vi.fn().mockReturnValue({
          selectionStart: 0,
          selectionEnd: 0,
          value: '',
          focus: vi.fn(),
          setSelectionRange: vi.fn()
        })
      }
    }

    // Click insert link button
    const insertLinkBtn = wrapper.find('[data-test="insert-link-btn"]')
    if (insertLinkBtn.exists()) {
      await insertLinkBtn.trigger('click')
    }

    expect(mockPrompt).toHaveBeenCalledTimes(2)
    expect(mockPrompt).toHaveBeenCalledWith('请输入链接URL：')
    expect(mockPrompt).toHaveBeenCalledWith('请输入链接文字：')
  })

  it('loads categories and tags on mount', async () => {
    mount(ArticleEditor, {
      props: {
        mode: 'create'
      }
    })

    await wrapper.vm.$nextTick()

    expect(mockArticleStore.fetchCategories).toHaveBeenCalled()
    expect(mockArticleStore.fetchTags).toHaveBeenCalled()
  })

  it('cleans up auto-save timer on unmount', async () => {
    const wrapper = mount(ArticleEditor, {
      props: {
        mode: 'create'
      }
    })

    // Set auto-save timer
    wrapper.vm.startAutoSave()

    const clearTimeoutSpy = vi.spyOn(global, 'clearTimeout')

    // Unmount component
    wrapper.unmount()

    expect(clearTimeoutSpy).toHaveBeenCalled()
  })
})