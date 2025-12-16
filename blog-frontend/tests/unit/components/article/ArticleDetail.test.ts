import { describe, it, expect, vi, beforeEach } from 'vitest'
import { nextTick } from 'vue'
import ArticleDetail from '@/components/article/ArticleDetail.vue'
import { createTestWrapper, mockArticle, mockAuthor, mockCategory } from '@/test/utils'

// Mock composables and services
vi.mock('@/composables/useArticle', () => ({
  default: () => ({
    article: ref(mockArticle),
    author: ref(mockAuthor),
    category: ref(mockCategory),
    tags: ref(mockArticle.tags),
    isLiked: ref(false),
    loading: ref(false),
    error: ref(null),
    fetchArticle: vi.fn(),
    toggleLike: vi.fn(),
    shareArticle: vi.fn(),
  }),
}))

vi.mock('@/utils/date', () => ({
  formatDate: (date: string) => '2025年12月16日',
}))

describe('ArticleDetail.vue', () => {
  let wrapper: any

  beforeEach(() => {
    wrapper = createTestWrapper(ArticleDetail, {
      props: {
        articleId: 1,
      },
    })
  })

  it('应该正确渲染文章封面图', async () => {
    await nextTick()

    const coverImage = wrapper.find('img[alt="测试文章标题"]')
    expect(coverImage.exists()).toBe(true)
    expect(coverImage.attributes('src')).toBe(mockArticle.coverImage)
  })

  it('应该正确渲染文章标题和摘要', async () => {
    await nextTick()

    expect(wrapper.text()).toContain(mockArticle.title)
    expect(wrapper.text()).toContain(mockArticle.summary)
  })

  it('应该正确显示置顶标记', async () => {
    const topArticle = { ...mockArticle, isTop: true }
    await wrapper.setProps({ article: topArticle })
    await nextTick()

    expect(wrapper.text()).toContain('置顶')
    expect(wrapper.find('.bg-red-500').exists()).toBe(true)
  })

  it('应该正确显示文章状态', async () => {
    await nextTick()

    expect(wrapper.text()).toContain('已发布')
    expect(wrapper.find('.bg-green-100').exists()).toBe(true)

    // 测试草稿状态
    const draftArticle = { ...mockArticle, status: 'DRAFT' }
    await wrapper.setProps({ article: draftArticle })
    await nextTick()

    expect(wrapper.text()).toContain('草稿')
    expect(wrapper.find('.bg-yellow-100').exists()).toBe(true)
  })

  it('应该正确渲染作者信息', async () => {
    await nextTick()

    expect(wrapper.text()).toContain(mockAuthor.nickname)
    expect(wrapper.find('img[alt="测试作者"]').exists()).toBe(true)
    expect(wrapper.find('img[alt="测试作者"]').attributes('src')).toBe(mockAuthor.avatar)
  })

  it('应该正确显示文章统计信息', async () => {
    await nextTick()

    expect(wrapper.text()).toContain(mockArticle.viewCount.toString())
    expect(wrapper.text()).toContain(mockArticle.likeCount.toString())
    expect(wrapper.text()).toContain(mockArticle.commentCount.toString())

    expect(wrapper.find('.fa-eye').exists()).toBe(true)
    expect(wrapper.find('.fa-heart').exists()).toBe(true)
    expect(wrapper.find('.fa-comment').exists()).toBe(true)
  })

  it('应该正确显示分类信息', async () => {
    await nextTick()

    expect(wrapper.text()).toContain(mockCategory.name)
    expect(wrapper.find('.fa-folder').exists()).toBe(true)
  })

  it('应该正确处理点赞功能', async () => {
    await nextTick()

    const likeButton = wrapper.find('button:has(.fa-heart)')
    expect(likeButton.exists()).toBe(true)

    // 测试未点赞状态
    expect(likeButton.classes()).toContain('bg-gray-100')
    expect(wrapper.text()).toContain(mockArticle.likeCount.toString())

    await likeButton.trigger('click')

    // 验证toggleLike被调用
    expect(wrapper.vm.toggleLike).toHaveBeenCalled()
  })

  it('应该在已点赞时显示不同的样式', async () => {
    // 模拟已点赞状态
    const { useArticle } = await import('@/composables/useArticle')
    vi.mocked(useArticle).mockReturnValue({
      article: ref(mockArticle),
      author: ref(mockAuthor),
      category: ref(mockCategory),
      tags: ref(mockArticle.tags),
      isLiked: ref(true), // 已点赞
      loading: ref(false),
      error: ref(null),
      fetchArticle: vi.fn(),
      toggleLike: vi.fn(),
      shareArticle: vi.fn(),
    })

    wrapper = createTestWrapper(ArticleDetail, {
      props: { articleId: 1 },
    })

    await nextTick()

    const likeButton = wrapper.find('button:has(.fa-heart)')
    expect(likeButton.classes()).toContain('bg-red-500')
    expect(wrapper.find('.fas.fa-heart').exists()).toBe(true)
  })

  it('应该正确渲染文章内容', async () => {
    await nextTick()

    // 检查Markdown内容是否被渲染
    const contentContainer = wrapper.find('.prose')
    expect(contentContainer.exists()).toBe(true)
  })

  it('应该正确显示标签', async () => {
    await nextTick()

    mockArticle.tags.forEach(tag => {
      expect(wrapper.text()).toContain(tag.name)
    })
  })

  it('应该正确处理分享功能', async () => {
    await nextTick()

    const shareButton = wrapper.find('button:has(.fa-share-alt)')
    expect(shareButton.exists()).toBe(true)

    await shareButton.trigger('click')

    // 验证shareArticle被调用
    expect(wrapper.vm.shareArticle).toHaveBeenCalled()
  })

  it('应该在编辑按钮显示时正确处理编辑事件', async () => {
    // 模拟作者为当前用户
    const currentUser = { ...mockAuthor, id: mockArticle.authorId }
    localStorage.setItem('currentUser', JSON.stringify(currentUser))

    wrapper = createTestWrapper(ArticleDetail, {
      props: { articleId: 1 },
    })

    await nextTick()

    const editButton = wrapper.find('button:has(.fa-edit)')
    expect(editButton.exists()).toBe(true)

    await editButton.trigger('click')

    // 验证路由跳转到编辑页面
    expect(wrapper.vm.$router.push).toHaveBeenCalledWith(`/articles/${mockArticle.id}/edit`)
  })

  it('应该在没有封面图时隐藏封面区域', async () => {
    const articleWithoutCover = { ...mockArticle, coverImage: null }
    await wrapper.setProps({ article: articleWithoutCover })
    await nextTick()

    const coverImage = wrapper.find('img[alt="测试文章标题"]')
    expect(coverImage.exists()).toBe(false)
  })

  it('应该在没有摘要时隐藏摘要区域', async () => {
    const articleWithoutSummary = { ...mockArticle, summary: null }
    await wrapper.setProps({ article: articleWithoutSummary })
    await nextTick()

    expect(wrapper.text()).not.toContain('测试摘要')
  })

  it('应该在没有分类时不显示分类信息', async () => {
    const articleWithoutCategory = { ...mockArticle, categoryName: null }
    await wrapper.setProps({ article: articleWithoutCategory })
    await nextTick()

    expect(wrapper.text()).not.toContain(mockCategory.name)
  })

  it('应该正确处理加载状态', async () => {
    const { useArticle } = await import('@/composables/useArticle')
    vi.mocked(useArticle).mockReturnValue({
      article: ref(null),
      author: ref(null),
      category: ref(null),
      tags: ref([]),
      isLiked: ref(false),
      loading: ref(true), // 加载中
      error: ref(null),
      fetchArticle: vi.fn(),
      toggleLike: vi.fn(),
      shareArticle: vi.fn(),
    })

    wrapper = createTestWrapper(ArticleDetail, {
      props: { articleId: 1 },
    })

    await nextTick()

    expect(wrapper.find('.fa-spinner').exists()).toBe(true)
    expect(wrapper.text()).toContain('加载中...')
  })

  it('应该正确处理错误状态', async () => {
    const { useArticle } = await import('@/composables/useArticle')
    vi.mocked(useArticle).mockReturnValue({
      article: ref(null),
      author: ref(null),
      category: ref(null),
      tags: ref([]),
      isLiked: ref(false),
      loading: ref(false),
      error: ref('文章不存在'), // 错误状态
      fetchArticle: vi.fn(),
      toggleLike: vi.fn(),
      shareArticle: vi.fn(),
    })

    wrapper = createTestWrapper(ArticleDetail, {
      props: { articleId: 1 },
    })

    await nextTick()

    expect(wrapper.text()).toContain('文章不存在')
  })
})