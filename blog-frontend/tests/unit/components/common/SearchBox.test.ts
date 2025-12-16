import { describe, it, expect, vi, beforeEach } from 'vitest'
import { nextTick } from 'vue'
import SearchBox from '@/components/common/SearchBox.vue'
import { createTestWrapper } from '@/test/utils'

describe('SearchBox.vue', () => {
  let wrapper: any

  beforeEach(() => {
    wrapper = createTestWrapper(SearchBox, {
      props: {
        placeholder: '搜索文章...',
        modelValue: '',
      },
    })
  })

  it('应该正确渲染搜索框', async () => {
    await nextTick()

    const searchInput = wrapper.find('input[type="search"]')
    expect(searchInput.exists()).toBe(true)
    expect(searchInput.attributes('placeholder')).toBe('搜索文章...')
  })

  it('应该正确显示搜索图标', async () => {
    await nextTick()

    expect(wrapper.find('.fa-search').exists()).toBe(true)
  })

  it('应该支持输入搜索关键词', async () => {
    await nextTick()

    const searchInput = wrapper.find('input[type="search"]')
    await searchInput.setValue('Vue.js')

    expect(searchInput.element.value).toBe('Vue.js')
    expect(wrapper.emitted('update:modelValue')).toBeTruthy()
    expect(wrapper.emitted('update:modelValue')[0]).toEqual(['Vue.js'])
  })

  it('应该在按下回车键时触发搜索', async () => {
    await nextTick()

    const searchInput = wrapper.find('input[type="search"]')
    await searchInput.setValue('Vue.js')
    await searchInput.trigger('keydown.enter')

    expect(wrapper.emitted('search')).toBeTruthy()
    expect(wrapper.emitted('search')[0]).toEqual(['Vue.js'])
  })

  it('应该在点击搜索按钮时触发搜索', async () => {
    await nextTick()

    const searchInput = wrapper.find('input[type="search"]')
    await searchInput.setValue('Vue.js')

    const searchButton = wrapper.find('button')
    await searchButton.trigger('click')

    expect(wrapper.emitted('search')).toBeTruthy()
    expect(wrapper.emitted('search')[0]).toEqual(['Vue.js'])
  })

  it('应该在清空按钮点击时清空搜索框', async () => {
    await nextTick()

    const searchInput = wrapper.find('input[type="search"]')
    await searchInput.setValue('Vue.js')

    // 触发input事件以显示清空按钮
    await searchInput.trigger('input')
    await nextTick()

    const clearButton = wrapper.find('button:has(.fa-times)')
    if (clearButton.exists()) {
      await clearButton.trigger('click')

      expect(searchInput.element.value).toBe('')
      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
      expect(wrapper.emitted('update:modelValue').pop()).toEqual([''])
      expect(wrapper.emitted('clear')).toBeTruthy()
    }
  })

  it('应该在有搜索内容时显示清空按钮', async () => {
    await nextTick()

    // 初始状态不应显示清空按钮
    expect(wrapper.find('button:has(.fa-times)').exists()).toBe(false)

    const searchInput = wrapper.find('input[type="search"]')
    await searchInput.setValue('Vue.js')
    await searchInput.trigger('input')
    await nextTick()

    // 有内容时应显示清空按钮
    expect(wrapper.find('button:has(.fa-times)').exists()).toBe(true)
  })

  it('应该支持防抖搜索', async () => {
    wrapper = createTestWrapper(SearchBox, {
      props: {
        placeholder: '搜索文章...',
        modelValue: '',
        debounce: 300,
      },
    })

    await nextTick()

    const searchInput = wrapper.find('input[type="search"]')

    // 快速输入多个值
    await searchInput.setValue('Vue')
    vi.advanceTimersByTime(100)
    await searchInput.setValue('Vue.js')
    vi.advanceTimersByTime(200)
    await searchInput.setValue('Vue.js 3')
    vi.advanceTimersByTime(400)

    await nextTick()

    // 应该只触发最后一次搜索
    expect(wrapper.emitted('search')).toHaveLength(1)
    expect(wrapper.emitted('search')[0]).toEqual(['Vue.js 3'])
  })

  it('应该正确处理搜索建议', async () => {
    const suggestions = ['Vue.js', 'React', 'Angular']
    wrapper = createTestWrapper(SearchBox, {
      props: {
        placeholder: '搜索文章...',
        modelValue: '',
        suggestions,
      },
    })

    await nextTick()

    const searchInput = wrapper.find('input[type="search"]')
    await searchInput.setValue('Vue')
    await searchInput.trigger('input')
    await nextTick()

    // 应该显示搜索建议
    expect(wrapper.find('.search-suggestions').exists()).toBe(true)
    expect(wrapper.text()).toContain('Vue.js')
  })

  it('应该在选择搜索建议时触发搜索', async () => {
    const suggestions = ['Vue.js', 'React', 'Angular']
    wrapper = createTestWrapper(SearchBox, {
      props: {
        placeholder: '搜索文章...',
        modelValue: '',
        suggestions,
      },
    })

    await nextTick()

    const searchInput = wrapper.find('input[type="search"]')
    await searchInput.setValue('Vue')
    await searchInput.trigger('input')
    await nextTick()

    const suggestionItem = wrapper.find('.search-suggestion')
    await suggestionItem.trigger('click')

    expect(wrapper.emitted('search')).toBeTruthy()
    expect(wrapper.emitted('search')[0]).toEqual(['Vue.js'])
  })

  it('应该正确显示搜索历史', async () => {
    const searchHistory = ['Vue.js 教程', 'React hooks', 'TypeScript']
    wrapper = createTestWrapper(SearchBox, {
      props: {
        placeholder: '搜索文章...',
        modelValue: '',
        searchHistory,
      },
    })

    await nextTick()

    const searchInput = wrapper.find('input[type="search"]')
    await searchInput.trigger('focus')
    await nextTick()

    // 应该显示搜索历史
    expect(wrapper.find('.search-history').exists()).toBe(true)
    searchHistory.forEach(item => {
      expect(wrapper.text()).toContain(item)
    })
  })

  it('应该在选择历史记录时触发搜索', async () => {
    const searchHistory = ['Vue.js 教程', 'React hooks']
    wrapper = createTestWrapper(SearchBox, {
      props: {
        placeholder: '搜索文章...',
        modelValue: '',
        searchHistory,
      },
    })

    await nextTick()

    const searchInput = wrapper.find('input[type="search"]')
    await searchInput.trigger('focus')
    await nextTick()

    const historyItem = wrapper.find('.search-history-item')
    await historyItem.trigger('click')

    expect(wrapper.emitted('search')).toBeTruthy()
    expect(wrapper.emitted('search')[0]).toEqual(['Vue.js 教程'])
  })

  it('应该支持热门搜索标签', async () => {
    const hotSearches = ['Vue3', 'TypeScript', '前端开发']
    wrapper = createTestWrapper(SearchBox, {
      props: {
        placeholder: '搜索文章...',
        modelValue: '',
        hotSearches,
      },
    })

    await nextTick()

    expect(wrapper.find('.hot-searches').exists()).toBe(true)
    hotSearches.forEach(tag => {
      expect(wrapper.text()).toContain(tag)
    })
  })

  it('应该在点击热门搜索标签时触发搜索', async () => {
    const hotSearches = ['Vue3', 'TypeScript']
    wrapper = createTestWrapper(SearchBox, {
      props: {
        placeholder: '搜索文章...',
        modelValue: '',
        hotSearches,
      },
    })

    await nextTick()

    const hotSearchTag = wrapper.find('.hot-search-tag')
    await hotSearchTag.trigger('click')

    expect(wrapper.emitted('search')).toBeTruthy()
    expect(wrapper.emitted('search')[0]).toEqual(['Vue3'])
  })

  it('应该正确响应外部modelValue变化', async () => {
    await nextTick()

    await wrapper.setProps({ modelValue: '新的搜索值' })
    await nextTick()

    const searchInput = wrapper.find('input[type="search"]')
    expect(searchInput.element.value).toBe('新的搜索值')
  })

  it('应该支持禁用状态', async () => {
    wrapper = createTestWrapper(SearchBox, {
      props: {
        placeholder: '搜索文章...',
        modelValue: '',
        disabled: true,
      },
    })

    await nextTick()

    const searchInput = wrapper.find('input[type="search"]')
    expect(searchInput.attributes('disabled')).toBeDefined()
    expect(searchInput.classes()).toContain('bg-gray-100')
  })

  it('应该支持不同的尺寸', async () => {
    // 测试小尺寸
    wrapper = createTestWrapper(SearchBox, {
      props: {
        placeholder: '搜索文章...',
        modelValue: '',
        size: 'small',
      },
    })

    await nextTick()

    expect(wrapper.find('.input-sm').exists()).toBe(true)

    // 测试大尺寸
    await wrapper.setProps({ size: 'large' })
    await nextTick()

    expect(wrapper.find('.input-lg').exists()).toBe(true)
  })

  it('应该正确处理键盘导航', async () => {
    const suggestions = ['Vue.js', 'React', 'Angular']
    wrapper = createTestWrapper(SearchBox, {
      props: {
        placeholder: '搜索文章...',
        modelValue: '',
        suggestions,
      },
    })

    await nextTick()

    const searchInput = wrapper.find('input[type="search"]')
    await searchInput.setValue('Vue')
    await searchInput.trigger('input')
    await nextTick()

    // 按下箭头键
    await searchInput.trigger('keydown.down')
    await nextTick()

    // 检查是否有选中的建议项
    expect(wrapper.find('.search-suggestion.active').exists()).toBe(true)

    // 按回车键选择
    await searchInput.trigger('keydown.enter')
    await nextTick()

    expect(wrapper.emitted('search')).toBeTruthy()
  })

  it('应该在没有搜索结果时显示无结果提示', async () => {
    wrapper = createTestWrapper(SearchBox, {
      props: {
        placeholder: '搜索文章...',
        modelValue: '',
        suggestions: [],
      },
    })

    await nextTick()

    const searchInput = wrapper.find('input[type="search"]')
    await searchInput.setValue('不存在的关键词')
    await searchInput.trigger('input')
    await nextTick()

    expect(wrapper.text()).toContain('无搜索建议')
  })

  it('应该正确处理loading状态', async () => {
    wrapper = createTestWrapper(SearchBox, {
      props: {
        placeholder: '搜索文章...',
        modelValue: '',
        loading: true,
      },
    })

    await nextTick()

    expect(wrapper.find('.fa-spinner').exists()).toBe(true)
    expect(wrapper.find('button').attributes('disabled')).toBeDefined()
  })
})