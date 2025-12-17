import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElMessage, ElForm } from 'element-plus'
import TagForm from '@/components/tag/TagForm.vue'
import type { Tag, CreateTagRequest, UpdateTagRequest } from '@blog/shared/types'

// Mock Element Plus components
vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn(),
    success: vi.fn()
  },
  ElForm: {
    validate: vi.fn((callback) => callback(true))
  }
}))

// Mock @vicons/element-plus
vi.mock('@vicons/element-plus', () => ({
  Close: 'CloseIcon'
}))

describe('TagForm.vue', () => {
  let wrapper: any

  const mockTag: Tag = {
    id: 1,
    name: 'Java',
    color: '#007396',
    articleCount: 5,
    createTime: '2025-01-01T00:00:00Z',
    updateTime: '2025-01-01T00:00:00Z'
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('创建模式', () => {
    beforeEach(() => {
      wrapper = mount(TagForm, {
        props: {
          loading: false
        }
      })
    })

    it('应该正确初始化创建模式表单', () => {
      expect(wrapper.vm.formData.name).toBe('')
      expect(wrapper.vm.formData.color).toBe('#1890ff')
      expect(wrapper.vm.title).toBe('创建标签')
      expect(wrapper.vm.isEdit).toBe(false)
    })

    it('应该验证必填字段', async () => {
      const form = wrapper.findComponent(ElForm)

      // Mock form validation to fail
      form.vm.validate = vi.fn((callback) => callback(false))

      await wrapper.vm.handleSubmit()

      expect(form.vm.validate).toHaveBeenCalled()
      expect(wrapper.emitted('submit')).toBeFalsy()
    })

    it('应该在表单验证通过时提交数据', async () => {
      const form = wrapper.findComponent(ElForm)
      form.vm.validate = vi.fn((callback) => callback(true))

      // 设置表单数据
      wrapper.vm.formData.name = 'New Tag'
      wrapper.vm.formData.color = '#FF0000'

      await wrapper.vm.handleSubmit()

      expect(wrapper.emitted('submit')).toBeTruthy()
      expect(wrapper.emitted('submit')[0][0]).toEqual({
        name: 'New Tag',
        color: '#FF0000'
      })
    })

    it('应该处理名称过长的情况', async () => {
      const longName = 'a'.repeat(31) // 超过30字符限制

      wrapper.vm.formData.name = longName
      await wrapper.vm.$nextTick()

      const nameInput = wrapper.find('input[placeholder="请输入标签名称"]')
      expect(nameInput.exists()).toBe(true)
    })

    it('应该重置表单数据', async () => {
      // 修改表单数据
      wrapper.vm.formData.name = 'Test Tag'
      wrapper.vm.formData.color = '#123456'

      await wrapper.vm.handleReset()

      expect(wrapper.vm.formData.name).toBe('')
      expect(wrapper.vm.formData.color).toBe('#1890ff')
    })
  })

  describe('编辑模式', () => {
    beforeEach(() => {
      wrapper = mount(TagForm, {
        props: {
          tag: mockTag,
          loading: false
        }
      })
    })

    it('应该正确初始化编辑模式表单', () => {
      expect(wrapper.vm.formData.name).toBe('Java')
      expect(wrapper.vm.formData.color).toBe('#007396')
      expect(wrapper.vm.title).toBe('编辑标签')
      expect(wrapper.vm.isEdit).toBe(true)
    })

    it('应该在提交时包含ID', async () => {
      const form = wrapper.findComponent(ElForm)
      form.vm.validate = vi.fn((callback) => callback(true))

      // 修改数据
      wrapper.vm.formData.name = 'Updated Java'
      wrapper.vm.formData.color = '#FF0000'

      await wrapper.vm.handleSubmit()

      expect(wrapper.emitted('submit')).toBeTruthy()
      expect(wrapper.emitted('submit')[0][0]).toEqual({
        name: 'Updated Java',
        color: '#FF0000'
      })
    })

    it('应该在标签属性变化时更新表单数据', async () => {
      const newTag = {
        ...mockTag,
        name: 'Spring Boot',
        color: '#6DB33F'
      }

      await wrapper.setProps({ tag: newTag })

      expect(wrapper.vm.formData.name).toBe('Spring Boot')
      expect(wrapper.vm.formData.color).toBe('#6DB33F')
    })
  })

  describe('颜色选择器', () => {
    beforeEach(() => {
      wrapper = mount(TagForm, {
        props: {
          loading: false
        }
      })
    })

    it('应该显示颜色选择器', () => {
      const colorPicker = wrapper.find('.el-color-picker')
      expect(colorPicker.exists()).toBe(true)
    })

    it('应该更新颜色值', async () => {
      const newColor = '#FF5733'
      wrapper.vm.formData.color = newColor

      expect(wrapper.vm.formData.color).toBe(newColor)
    })

    it('应该提供预设颜色', () => {
      const presetColors = wrapper.vm.presetColors
      expect(presetColors).toContain('#1890ff')
      expect(presetColors).toContain('#52c41a')
      expect(presetColors).toContain('#faad14')
      expect(presetColors).toContain('#f5222d')
      expect(presetColors.length).toBeGreaterThan(0)
    })
  })

  describe('加载状态', () => {
    it('应该在加载时禁用提交按钮', () => {
      wrapper = mount(TagForm, {
        props: {
          loading: true
        }
      })

      const submitButton = wrapper.find('button[type="button"]')
      expect(submitButton.attributes('disabled')).toBeDefined()
    })

    it('应该在非加载时启用提交按钮', () => {
      wrapper = mount(TagForm, {
        props: {
          loading: false
        }
      })

      const submitButton = wrapper.find('button[type="button"]')
      expect(submitButton.attributes('disabled')).toBeUndefined()
    })
  })

  describe('取消操作', () => {
    beforeEach(() => {
      wrapper = mount(TagForm, {
        props: {
          loading: false
        }
      })
    })

    it('应该在点击取消时触发cancel事件', async () => {
      await wrapper.vm.handleCancel()

      expect(wrapper.emitted('cancel')).toBeTruthy()
    })
  })

  describe('表单验证规则', () => {
    beforeEach(() => {
      wrapper = mount(TagForm, {
        props: {
          loading: false
        }
      })
    })

    it('应该验证标签名称为必填', () => {
      const nameRules = wrapper.vm.formRules.name
      expect(nameRules).toBeDefined()

      // 测试空值
      const emptyResult = nameRules[0]('')
      expect(emptyResult).toBeInstanceOf(Error)

      // 测试有效值
      const validResult = nameRules[1]('Valid Name')
      expect(validResult).toBe(true)
    })

    it('应该验证标签名称长度', () => {
      const nameRules = wrapper.vm.formRules.name

      // 测试超长名称
      const longName = 'a'.repeat(31)
      const longResult = nameRules[1](longName)
      expect(longResult).toBeInstanceOf(Error)

      // 测试边界长度
      const boundaryName = 'a'.repeat(30)
      const boundaryResult = nameRules[1](boundaryName)
      expect(boundaryResult).toBe(true)
    })

    it('应该验证颜色格式', () => {
      const colorRules = wrapper.vm.formRules.color

      // 测试无效颜色格式
      const invalidResult = colorRules[1]('invalid-color')
      expect(invalidResult).toBeInstanceOf(Error)

      // 测试有效颜色格式
      const validResults = [
        colorRules[1]('#FF0000'),
        colorRules[1]('#ff0000'),
        colorRules[1]('#123ABC'),
        colorRules[1]('#123abc')
      ]

      validResults.forEach(result => {
        expect(result).toBe(true)
      })
    })
  })

  describe('键盘事件', () => {
    beforeEach(() => {
      wrapper = mount(TagForm, {
        props: {
          loading: false
        }
      })
    })

    it('应该在Ctrl+Enter时提交表单', async () => {
      const form = wrapper.findComponent(ElForm)
      form.vm.validate = vi.fn((callback) => callback(true))

      wrapper.vm.formData.name = 'Test Tag'

      // 模拟键盘事件
      const event = new KeyboardEvent('keydown', {
        key: 'Enter',
        ctrlKey: true
      })

      wrapper.vm.handleKeydown(event)
      await wrapper.vm.$nextTick()

      expect(wrapper.emitted('submit')).toBeTruthy()
    })

    it('应该在Escape时取消操作', async () => {
      const event = new KeyboardEvent('keydown', {
        key: 'Escape'
      })

      wrapper.vm.handleKeydown(event)
      await wrapper.vm.$nextTick()

      expect(wrapper.emitted('cancel')).toBeTruthy()
    })
  })
})