import { describe, it, expect, vi, beforeEach } from 'vitest'
import { nextTick } from 'vue'
import ArticleList from '@/components/article/ArticleList.vue'
import { createTestWrapper, mockArticle } from '@/test/utils'
import useArticleList from '@/composables/useArticleList'

// Mock the composable
vi.mock('@/composables/useArticleList')

describe('ArticleList.vue', () => {
  let mockUseArticleList: any

  beforeEach(() => {
    mockUseArticleList = {
      articles: ref([]),
      loading: ref(false),
      error: ref(null),
      totalElements: ref(0),
      totalPages: ref(0),
      currentPage: ref(0),
      pageSize: ref(10),
      viewMode: ref('grid'),
      sortBy: ref('publishTime'),
      sortDir: ref('desc'),
      filters: ref({
        categoryId: null,
        tagId: null,
        keyword: '',
        status: 'PUBLISHED',
      }),
      fetchArticles: vi.fn(),
      handleSort: vi.fn(),
      handleFilter: vi.fn(),
      handlePageChange: vi.fn(),
      handleViewModeChange: vi.fn(),
      refresh: vi.fn(),
    }

    vi.mocked(useArticleList).mockReturnValue(mockUseArticleList)
  })

  it('应该正确渲染加载状态', async () => {
    mockUseArticleList.loading.value = true

    const wrapper = createTestWrapper(ArticleList)

    expect(wrapper.find('.fa-spinner').exists()).toBe(true)
    expect(wrapper.text()).toContain('加载中...')
  })

  it('应该正确渲染空状态', async () => {
    mockUseArticleList.loading.value = false
    mockUseArticleList.articles.value = []

    const wrapper = createTestWrapper(ArticleList)

    expect(wrapper.find('.fa-inbox').exists()).toBe(true)
    expect(wrapper.text()).toContain('暂无文章')
  })

  it('应该在网格模式下正确渲染文章列表', async () => {
    mockUseArticleList.loading.value = false
    mockUseArticleList.articles.value = [mockArticle, { ...mockArticle, id: 2 }]
    mockUseArticleList.viewMode.value = 'grid'

    const wrapper = createTestWrapper(ArticleList)

    await nextTick()

    const gridContainer = wrapper.find('.grid.grid-cols-1')
    expect(gridContainer.exists()).toBe(true)

    const articleCards = wrapper.findAllComponents({ name: 'ArticleCard' })
    expect(articleCards).toHaveLength(2)
  })

  it('应该在列表模式下正确渲染文章列表', async () => {
    mockUseArticleList.loading.value = false
    mockUseArticleList.articles.value = [mockArticle]
    mockUseArticleList.viewMode.value = 'list'

    const wrapper = createTestWrapper(ArticleList)

    await nextTick()

    // 检查列表视图容器
    const listContainer = wrapper.find('.space-y-4')
    expect(listContainer.exists()).toBe(true)

    // 检查文章信息显示
    expect(wrapper.text()).toContain(mockArticle.title)
    expect(wrapper.text()).toContain(mockArticle.summary)
  })

  it('应该正确切换视图模式', async () => {
    mockUseArticleList.loading.value = false
    mockUseArticleList.articles.value = [mockArticle]
    mockUseArticleList.viewMode.value = 'grid'

    const wrapper = createTestWrapper(ArticleList)

    await nextTick()

    // 点击列表视图按钮
    const listViewButton = wrapper.findAll('button').find(btn =>
      btn.text().includes('列表视图')
    )
    await listViewButton.trigger('click')

    expect(mockUseArticleList.handleViewModeChange).toHaveBeenCalledWith('list')
  })

  it('应该正确处理排序', async () => {
    mockUseArticleList.loading.value = false
    mockUseArticleList.articles.value = [mockArticle]

    const wrapper = createTestWrapper(ArticleList)

    await nextTick()

    // 选择排序字段
    const sortSelect = wrapper.find('select')
    await sortSelect.setValue('viewCount')

    // 点击排序方向按钮
    const sortDirButton = wrapper.find('button[title*="排序"]')
    await sortDirButton.trigger('click')

    expect(mockUseArticleList.handleSort).toHaveBeenCalled()
  })

  it('应该正确显示置顶文章标记', async () => {
    const topArticle = { ...mockArticle, isTop: true }
    mockUseArticleList.loading.value = false
    mockUseArticleList.articles.value = [topArticle]
    mockUseArticleList.viewMode.value = 'list'

    const wrapper = createTestWrapper(ArticleList)

    await nextTick()

    expect(wrapper.text()).toContain('置顶')
  })

  it('应该正确处理文章点击事件', async () => {
    mockUseArticleList.loading.value = false
    mockUseArticleList.articles.value = [mockArticle]
    mockUseArticleList.viewMode.value = 'list'

    const wrapper = createTestWrapper(ArticleList)

    await nextTick()

    const articleElement = wrapper.find('.cursor-pointer')
    await articleElement.trigger('click')

    // 验证路由跳转
    expect(wrapper.vm.$router.push).toHaveBeenCalledWith(`/articles/${mockArticle.id}`)
  })

  it('应该在没有封面图时显示占位符', async () => {
    const articleWithoutCover = { ...mockArticle, coverImage: null }
    mockUseArticleList.loading.value = false
    mockUseArticleList.articles.value = [articleWithoutCover]
    mockUseArticleList.viewMode.value = 'list'

    const wrapper = createTestWrapper(ArticleList)

    await nextTick()

    expect(wrapper.find('.fa-image').exists()).toBe(true)
  })

  it('应该正确显示文章统计信息', async () => {
    mockUseArticleList.loading.value = false
    mockUseArticleList.articles.value = [mockArticle]
    mockUseArticleList.viewMode.value = 'list'

    const wrapper = createTestWrapper(ArticleList)

    await nextTick()

    expect(wrapper.text()).toContain(mockArticle.viewCount.toString())
    expect(wrapper.text()).toContain(mockArticle.likeCount.toString())
    expect(wrapper.text()).toContain(mockArticle.commentCount.toString())
  })

  it('应该正确显示作者信息和分类', async () => {
    mockUseArticleList.loading.value = false
    mockUseArticleList.articles.value = [mockArticle]
    mockUseArticleList.viewMode.value = 'list'

    const wrapper = createTestWrapper(ArticleList)

    await nextTick()

    expect(wrapper.text()).toContain(mockArticle.authorName)
    expect(wrapper.text()).toContain(mockArticle.categoryName)
  })
})