import { describe, it, expect, vi } from 'vitest'
import { nextTick } from 'vue'
import ArticleCard from '@/components/article/ArticleCard.vue'
import { createTestWrapper, mockArticle } from '@/test/utils'

describe('ArticleCard.vue', () => {
  it('应该正确渲染文章卡片的基本信息', async () => {
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: mockArticle,
      },
    })

    await nextTick()

    expect(wrapper.text()).toContain(mockArticle.title)
    expect(wrapper.text()).toContain(mockArticle.summary)
    expect(wrapper.text()).toContain(mockArticle.authorName)
  })

  it('应该正确渲染文章封面图', async () => {
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: mockArticle,
      },
    })

    await nextTick()

    const coverImage = wrapper.find('img')
    expect(coverImage.exists()).toBe(true)
    expect(coverImage.attributes('src')).toBe(mockArticle.coverImage)
    expect(coverImage.attributes('alt')).toBe(mockArticle.title)
  })

  it('应该在没有封面图时显示占位符', async () => {
    const articleWithoutCover = { ...mockArticle, coverImage: null }
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: articleWithoutCover,
      },
    })

    await nextTick()

    const coverImage = wrapper.find('img')
    expect(coverImage.exists()).toBe(false)

    expect(wrapper.find('.bg-gray-200').exists()).toBe(true)
    expect(wrapper.find('.fa-image').exists()).toBe(true)
  })

  it('应该正确显示置顶标记', async () => {
    const topArticle = { ...mockArticle, isTop: true }
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: topArticle,
      },
    })

    await nextTick()

    expect(wrapper.text()).toContain('置顶')
    expect(wrapper.find('.bg-red-500').exists()).toBe(true)
  })

  it('应该正确显示文章状态', async () => {
    // 测试已发布状态
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: mockArticle,
      },
    })

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

  it('应该正确显示文章统计信息', async () => {
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: mockArticle,
      },
    })

    await nextTick()

    expect(wrapper.text()).toContain(mockArticle.viewCount.toString())
    expect(wrapper.text()).toContain(mockArticle.likeCount.toString())
    expect(wrapper.text()).toContain(mockArticle.commentCount.toString())

    expect(wrapper.find('.fa-eye').exists()).toBe(true)
    expect(wrapper.find('.fa-heart').exists()).toBe(true)
    expect(wrapper.find('.fa-comment').exists()).toBe(true)
  })

  it('应该正确显示作者信息', async () => {
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: mockArticle,
      },
    })

    await nextTick()

    expect(wrapper.text()).toContain(mockArticle.authorName)
    const avatar = wrapper.find('img[alt="测试作者"]')
    expect(avatar.exists()).toBe(true)
    expect(avatar.attributes('src')).toBe(mockArticle.authorAvatar)
  })

  it('应该正确显示分类信息', async () => {
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: mockArticle,
      },
    })

    await nextTick()

    expect(wrapper.text()).toContain(mockArticle.categoryName)
    expect(wrapper.find('.fa-folder').exists()).toBe(true)
  })

  it('应该正确显示标签', async () => {
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: mockArticle,
      },
    })

    await nextTick()

    mockArticle.tags.forEach(tag => {
      expect(wrapper.text()).toContain(tag.name)
    })

    const tagElements = wrapper.findAll('.bg-blue-100')
    expect(tagElements.length).toBe(mockArticle.tags.length)
  })

  it('应该正确显示发布时间', async () => {
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: mockArticle,
      },
    })

    await nextTick()

    // Mock formatDate函数应该被调用
    expect(wrapper.text()).toContain('2025年12月16日')
  })

  it('应该正确处理点击事件', async () => {
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: mockArticle,
      },
    })

    await nextTick()

    await wrapper.trigger('click')

    expect(wrapper.emitted('click')).toBeTruthy()
    expect(wrapper.emitted('click')[0]).toEqual([mockArticle])
  })

  it('应该在没有摘要时不显示摘要区域', async () => {
    const articleWithoutSummary = { ...mockArticle, summary: null }
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: articleWithoutSummary,
      },
    })

    await nextTick()

    expect(wrapper.text()).not.toContain('测试摘要')
  })

  it('应该在没有分类时不显示分类信息', async () => {
    const articleWithoutCategory = { ...mockArticle, categoryName: null }
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: articleWithoutCategory,
      },
    })

    await nextTick()

    expect(wrapper.text()).not.toContain(mockArticle.categoryName)
    expect(wrapper.find('.fa-folder').exists()).toBe(false)
  })

  it('应该在没有标签时不显示标签区域', async () => {
    const articleWithoutTags = { ...mockArticle, tags: [] }
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: articleWithoutTags,
      },
    })

    await nextTick()

    const tagElements = wrapper.findAll('.bg-blue-100')
    expect(tagElements.length).toBe(0)
  })

  it('应该在没有作者头像时使用默认头像', async () => {
    const articleWithoutAvatar = { ...mockArticle, authorAvatar: null }
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: articleWithoutAvatar,
      },
    })

    await nextTick()

    const avatar = wrapper.find('img[alt="测试作者"]')
    expect(avatar.exists()).toBe(true)
    expect(avatar.attributes('src')).toBe('/default-avatar.png')
  })

  it('应该正确处理悬停效果', async () => {
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: mockArticle,
      },
    })

    await nextTick()

    // 检查是否有悬停样式类
    const card = wrapper.find('.cursor-pointer')
    expect(card.classes()).toContain('hover:shadow-lg')
  })

  it('应该正确限制标题长度', async () => {
    const longTitleArticle = {
      ...mockArticle,
      title: '这是一篇非常非常长的文章标题，应该被截断并显示省略号',
    }
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: longTitleArticle,
      },
    })

    await nextTick()

    const titleElement = wrapper.find('.text-lg.font-semibold')
    expect(titleElement.classes()).toContain('line-clamp-2')
  })

  it('应该正确限制摘要长度', async () => {
    const longSummaryArticle = {
      ...mockArticle,
      summary: '这是一篇非常非常长的文章摘要，应该被截断并显示省略号。'.repeat(5),
    }
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: longSummaryArticle,
      },
    })

    await nextTick()

    const summaryElement = wrapper.find('.text-gray-600')
    expect(summaryElement.classes()).toContain('line-clamp-3')
  })

  it('应该支持不同的显示模式', async () => {
    // 测试紧凑模式
    const wrapper = createTestWrapper(ArticleCard, {
      props: {
        article: mockArticle,
        compact: true,
      },
    })

    await nextTick()

    // 检查是否有紧凑模式的样式类
    expect(wrapper.find('.p-4').exists()).toBe(true)

    // 测试正常模式
    await wrapper.setProps({ compact: false })
    await nextTick()

    expect(wrapper.find('.p-6').exists()).toBe(true)
  })
})