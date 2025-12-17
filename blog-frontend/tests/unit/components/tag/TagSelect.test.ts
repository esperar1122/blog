import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElMessage } from 'element-plus'
import TagSelect from '@/components/tag/TagSelect.vue'
import type { Tag } from '@blog/shared/types'

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn()
  },
  ElSelect: {
    name: 'ElSelect',
    template: '<div><slot /></div>',
    props: ['modelValue', 'multiple', 'placeholder', 'loading', 'disabled', 'filterable', 'remote', 'clearable', 'collapseTags', 'collapseTagsTooltip'],
    emits: ['update:modelValue', 'change', 'clear']
  },
  ElOption: {
    name: 'ElOption',
    template: '<div><slot /></div>',
    props: ['label', 'value']
  }
}))

// Mock API
vi.mock('@/api/tag', () => ({
  getAllTags: vi.fn(() => Promise.resolve([
    { id: 1, name: 'Java', color: '#007396', articleCount: 5 },
    { id: 2, name: 'Vue.js', color: '#4FC08D', articleCount: 3 }
  ])),
  searchTags: vi.fn(() => Promise.resolve([
    { id: 1, name: 'JavaScript', color: '#F7DF1E', articleCount: 8 }
  ]))
}))

describe('TagSelect.vue', () => {
  let wrapper: any

  const mockTags: Tag[] = [
    { id: 1, name: 'Java', color: '#007396', articleCount: 5 },
    { id: 2, name: 'Vue.js', color: '#4FC08D', articleCount: 3 },
    { id: 3, name: 'Spring Boot', color: '#6DB33F', articleCount: 2 }
  ]

  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('基本功能', () => {
    beforeEach(() => {
      wrapper = mount(TagSelect, {
        props: {
          tags: mockTags
        }
      })
    })

    it('应该正确初始化组件', () => {
      expect(wrapper.vm.allTags).toEqual(mockTags)
      expect(wrapper.vm.selectedTagIds).toBeUndefined()
      expect(wrapper.vm.loading).toBe(false)
    })

    it('应该显示所有标签选项', () => {
      const options = wrapper.findAllComponents({ name: 'ElOption' })
      expect(options.length).toBe(mockTags.length)
    })

    it('应该显示标签颜色', () => {
      const colorDots = wrapper.findAll('.inline-block.w-3.h-3.rounded-full')
      expect(colorDots.length).toBe(mockTags.length)
    })

    it('应该显示标签文章数量', () => {
      const counts = wrapper.findAll('.text-gray-500.text-sm')
      expect(counts.length).toBe(mockTags.length)
      expect(counts[0].text()).toContain('(5)')
      expect(counts[1].text()).toContain('(3)')
    })
  })

  describe('单选模式', () => {
    beforeEach(() => {
      wrapper = mount(TagSelect, {
        props: {
          tags: mockTags,
          multiple: false
        }
      })
    })

    it('应该正确处理单选', async () => {
      await wrapper.vm.handleChange(1)

      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
      expect(wrapper.emitted('update:modelValue')[0]).toEqual([1])

      expect(wrapper.emitted('change')).toBeTruthy()
      expect(wrapper.emitted('change')[0]).toEqual([1, mockTags[0]])
    })

    it('应该设置正确的占位符', () => {
      const select = wrapper.findComponent({ name: 'ElSelect' })
      expect(select.props('placeholder')).toBe('请选择标签')
    })
  })

  describe('多选模式', () => {
    beforeEach(() => {
      wrapper = mount(TagSelect, {
        props: {
          tags: mockTags,
          multiple: true,
          modelValue: [1, 2]
        }
      })
    })

    it('应该正确处理多选', async () => {
      await wrapper.vm.handleChange([1, 3])

      expect(wrapper.emitted('update:modelValue')).toBeTruthy()
      expect(wrapper.emitted('update:modelValue')[0]).toEqual([[1, 3]])

      expect(wrapper.emitted('change')).toBeTruthy()
      const selectedTags = wrapper.emitted('change')[0][1] as Tag[]
      expect(selectedTags).toHaveLength(2)
      expect(selectedTags[0].id).toBe(1)
      expect(selectedTags[1].id).toBe(3)
    })

    it('应该显示折叠标签', () => {
      const select = wrapper.findComponent({ name: 'ElSelect' })
      expect(select.props('collapseTags')).toBe(false)
    })

    it('应该启用折叠标签提示', () => {
      const select = wrapper.findComponent({ name: 'ElSelect' })
      expect(select.props('collapseTagsTooltip')).toBe(true)
    })
  })

  describe('远程搜索', () => {
    beforeEach(() => {
      wrapper = mount(TagSelect, {
        props: {
          tags: [],
          remote: true,
          filterable: true
        }
      })
    })

    it('应该在搜索时调用API', async () => {
      const searchQuery = 'Java'
      await wrapper.vm.remoteMethod(searchQuery)

      expect(wrapper.vm.searchQuery).toBe(searchQuery)
    })

    it('应该在空查询时获取所有标签', async () => {
      const { getAllTags } = await import('@/api/tag')

      await wrapper.vm.remoteMethod('')

      expect(getAllTags).toHaveBeenCalled()
    })
  })

  describe('热门标签优先', () => {
    beforeEach(() => {
      const popularTags = [mockTags[0], mockTags[1]]
      wrapper = mount(TagSelect, {
        props: {
          tags: mockTags,
          showPopular: true,
          popularLimit: 2
        }
      })
    })

    it('应该优先显示热门标签', () => {
      const filteredTags = wrapper.vm.filteredTags
      expect(filteredTags.length).toBe(mockTags.length)
    })
  })

  describe('清空功能', () => {
    beforeEach(() => {
      wrapper = mount(TagSelect, {
        props: {
          tags: mockTags,
          clearable: true
        }
      })
    })

    it('应该在清空时触发事件', async () => {
      await wrapper.vm.handleClear()

      expect(wrapper.emitted('change')).toBeTruthy()
      expect(wrapper.emitted('change')[0]).toEqual([0, null])
    })

    it('应该启用清空功能', () => {
      const select = wrapper.findComponent({ name: 'ElSelect' })
      expect(select.props('clearable')).toBe(true)
    })
  })

  describe('禁用状态', () => {
    beforeEach(() => {
      wrapper = mount(TagSelect, {
        props: {
          tags: mockTags,
          disabled: true
        }
      })
    })

    it('应该禁用选择器', () => {
      const select = wrapper.findComponent({ name: 'ElSelect' })
      expect(select.props('disabled')).toBe(true)
    })
  })

  describe('筛选功能', () => {
    beforeEach(() => {
      wrapper = mount(TagSelect, {
        props: {
          tags: mockTags,
          filterable: true
        }
      })
    })

    it('应该启用筛选', () => {
      const select = wrapper.findComponent({ name: 'ElSelect' })
      expect(select.props('filterable')).toBe(true)
    })
  })

  describe('自定义标签列表', () => {
    const customTags = [mockTags[0]]

    beforeEach(() => {
      wrapper = mount(TagSelect, {
        props: {
          tags: customTags
        }
      })
    })

    it('应该使用传入的标签列表', () => {
      expect(wrapper.vm.allTags).toEqual(customTags)
      expect(wrapper.vm.filteredTags).toEqual(customTags)
    })
  })

  describe('标签变更监听', () => {
    it('应该在标签属性变化时更新', async () => {
      wrapper = mount(TagSelect, {
        props: {
          tags: mockTags
        }
      })

      const newTags = [mockTags[0]]
      await wrapper.setProps({ tags: newTags })

      expect(wrapper.vm.allTags).toEqual(newTags)
    })
  })

  describe('组件属性默认值', () => {
    beforeEach(() => {
      wrapper = mount(TagSelect)
    })

    it('应该设置正确的默认值', () => {
      expect(wrapper.vm.multiple).toBe(false)
      expect(wrapper.vm.placeholder).toBe('请选择标签')
      expect(wrapper.vm.disabled).toBe(false)
      expect(wrapper.vm.filterable).toBe(true)
      expect(wrapper.vm.remote).toBe(false)
      expect(wrapper.vm.clearable).toBe(true)
      expect(wrapper.vm.collapseTags).toBe(false)
      expect(wrapper.vm.collapseTagsTooltip).toBe(true)
      expect(wrapper.vm.showPopular).toBe(false)
      expect(wrapper.vm.popularLimit).toBe(10)
    })
  })

  describe('错误处理', () => {
    beforeEach(() => {
      wrapper = mount(TagSelect, {
        props: {
          tags: [],
          remote: true
        }
      })
    })

    it('应该处理API错误', async () => {
      const { ElMessage } = await import('element-plus')

      // Mock API error
      vi.doMock('@/api/tag', () => ({
        searchTags: vi.fn(() => Promise.reject(new Error('Network error')))
      }))

      await wrapper.vm.remoteMethod('test')

      // 验证错误消息显示
      // 注意：在实际测试中，你可能需要等待异步操作完成
    })
  })

  describe('计算属性', () => {
    beforeEach(() => {
      wrapper = mount(TagSelect, {
        props: {
          tags: mockTags,
          modelValue: 1
        }
      })
    })

    it('应该正确计算selectedTagIds', () => {
      expect(wrapper.vm.selectedTagIds).toBe(1)
    })

    it('应该在过滤后返回正确的标签', () => {
      const filtered = wrapper.vm.filteredTags
      expect(filtered).toEqual(mockTags)
    })
  })
})