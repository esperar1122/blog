import { describe, it, expect, vi } from 'vitest'
import { nextTick } from 'vue'
import Pagination from '@/components/common/Pagination.vue'
import { createTestWrapper } from '@/test/utils'

describe('Pagination.vue', () => {
  const defaultProps = {
    currentPage: 1,
    totalPages: 10,
    totalElements: 100,
    pageSize: 10,
  }

  it('应该正确渲染分页基本信息', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: defaultProps,
    })

    await nextTick()

    expect(wrapper.text()).toContain('第 1 页，共 10 页')
    expect(wrapper.text()).toContain('总共 100 条记录')
  })

  it('应该正确渲染页码按钮', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: defaultProps,
    })

    await nextTick()

    const pageButtons = wrapper.findAll('.page-btn')
    expect(pageButtons.length).toBeGreaterThan(0)

    // 检查当前页按钮状态
    const currentPageBtn = wrapper.find('.page-btn.active')
    expect(currentPageBtn.exists()).toBe(true)
    expect(currentPageBtn.text()).toBe('1')
  })

  it('应该正确处理第一页状态', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        currentPage: 1,
      },
    })

    await nextTick()

    const prevBtn = wrapper.find('.pagination-prev')
    expect(prevBtn.classes()).toContain('disabled')
  })

  it('应该正确处理最后一页状态', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        currentPage: 10,
      },
    })

    await nextTick()

    const nextBtn = wrapper.find('.pagination-next')
    expect(nextBtn.classes()).toContain('disabled')
  })

  it('应该在点击上一页时触发事件', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        currentPage: 5,
      },
    })

    await nextTick()

    const prevBtn = wrapper.find('.pagination-prev')
    await prevBtn.trigger('click')

    expect(wrapper.emitted('page-change')).toBeTruthy()
    expect(wrapper.emitted('page-change')[0]).toEqual([4])
  })

  it('应该在点击下一页时触发事件', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        currentPage: 5,
      },
    })

    await nextTick()

    const nextBtn = wrapper.find('.pagination-next')
    await nextBtn.trigger('click')

    expect(wrapper.emitted('page-change')).toBeTruthy()
    expect(wrapper.emitted('page-change')[0]).toEqual([6])
  })

  it('应该在点击页码时触发事件', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: defaultProps,
    })

    await nextTick()

    const pageBtns = wrapper.findAll('.page-btn')
    const page3Btn = pageBtns.find(btn => btn.text() === '3')

    if (page3Btn) {
      await page3Btn.trigger('click')
      expect(wrapper.emitted('page-change')).toBeTruthy()
      expect(wrapper.emitted('page-change')[0]).toEqual([3])
    }
  })

  it('应该正确处理首页按钮点击', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        currentPage: 5,
      },
    })

    await nextTick()

    const firstPageBtn = wrapper.find('.pagination-first')
    if (firstPageBtn.exists()) {
      await firstPageBtn.trigger('click')
      expect(wrapper.emitted('page-change')).toBeTruthy()
      expect(wrapper.emitted('page-change')[0]).toEqual([1])
    }
  })

  it('应该正确处理末页按钮点击', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        currentPage: 5,
      },
    })

    await nextTick()

    const lastPageBtn = wrapper.find('.pagination-last')
    if (lastPageBtn.exists()) {
      await lastPageBtn.trigger('click')
      expect(wrapper.emitted('page-change')).toBeTruthy()
      expect(wrapper.emitted('page-change')[0]).toEqual([10])
    }
  })

  it('应该正确显示省略号', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        currentPage: 5,
        totalPages: 20,
      },
    })

    await nextTick()

    expect(wrapper.text()).toContain('...')
  })

  it('应该正确处理只有一页的情况', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        currentPage: 1,
        totalPages: 1,
        totalElements: 5,
        pageSize: 10,
      },
    })

    await nextTick()

    expect(wrapper.text()).toContain('第 1 页，共 1 页')
    const prevBtn = wrapper.find('.pagination-prev')
    const nextBtn = wrapper.find('.pagination-next')
    expect(prevBtn.classes()).toContain('disabled')
    expect(nextBtn.classes()).toContain('disabled')
  })

  it('应该正确处理页码变化', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: defaultProps,
    })

    await nextTick()

    // 改变当前页
    await wrapper.setProps({ currentPage: 3 })
    await nextTick()

    const currentPageBtn = wrapper.find('.page-btn.active')
    expect(currentPageBtn.text()).toBe('3')
  })

  it('应该支持自定义每页显示数量', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        showSizeChanger: true,
        pageSizeOptions: [10, 20, 50, 100],
      },
    })

    await nextTick()

    const sizeChanger = wrapper.find('.page-size-changer')
    expect(sizeChanger.exists()).toBe(true)
  })

  it('应该在改变每页大小时触发事件', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        showSizeChanger: true,
        pageSizeOptions: [10, 20, 50, 100],
      },
    })

    await nextTick()

    const sizeSelect = wrapper.find('select')
    await sizeSelect.setValue('20')

    expect(wrapper.emitted('size-change')).toBeTruthy()
    expect(wrapper.emitted('size-change')[0]).toEqual([20])
  })

  it('应该正确显示页码范围', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        showTotal: true,
        showRange: true,
      },
    })

    await nextTick()

    // 第一页显示 1-10
    expect(wrapper.text()).toContain('1-10')

    // 切换到第2页
    await wrapper.setProps({ currentPage: 2 })
    await nextTick()

    expect(wrapper.text()).toContain('11-20')
  })

  it('应该正确处理快速跳转', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        showQuickJumper: true,
      },
    })

    await nextTick()

    const jumper = wrapper.find('.pagination-jumper')
    expect(jumper.exists()).toBe(true)

    const jumperInput = wrapper.find('.pagination-jumper input')
    await jumperInput.setValue('5')
    await jumperInput.trigger('keydown.enter')

    expect(wrapper.emitted('page-change')).toBeTruthy()
    expect(wrapper.emitted('page-change')[0]).toEqual([5])
  })

  it('应该支持简化的分页模式', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        simple: true,
      },
    })

    await nextTick()

    expect(wrapper.find('.pagination-simple').exists()).toBe(true)
    expect(wrapper.text()).toContain('1 / 10')
  })

  it('应该正确处理小型分页样式', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        size: 'small',
      },
    })

    await nextTick()

    expect(wrapper.find('.pagination-small').exists()).toBe(true)
  })

  it('应该正确处理禁用状态', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        disabled: true,
      },
    })

    await nextTick()

    expect(wrapper.find('.pagination-disabled').exists()).toBe(true)

    const pageButtons = wrapper.findAll('.page-btn')
    pageButtons.forEach(btn => {
      expect(btn.classes()).toContain('disabled')
    })
  })

  it('应该正确响应外部totalElements变化', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: defaultProps,
    })

    await nextTick()

    expect(wrapper.text()).toContain('总共 100 条记录')

    await wrapper.setProps({ totalElements: 200, totalPages: 20 })
    await nextTick()

    expect(wrapper.text()).toContain('总共 200 条记录')
    expect(wrapper.text()).toContain('共 20 页')
  })

  it('应该正确处理边界情况 - 当前页大于总页数', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        currentPage: 15,
        totalPages: 10,
        totalElements: 100,
        pageSize: 10,
      },
    })

    await nextTick()

    // 应该自动调整到最后一页
    expect(wrapper.emitted('page-change')).toBeTruthy()
    expect(wrapper.emitted('page-change')[0]).toEqual([10])
  })

  it('应该支持自定义显示文本', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        prevText: '上一页',
        nextText: '下一页',
        firstText: '首页',
        lastText: '末页',
      },
    })

    await nextTick()

    expect(wrapper.text()).toContain('上一页')
    expect(wrapper.text()).toContain('下一页')
  })

  it('应该正确处理页码按钮数量限制', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        currentPage: 50,
        totalPages: 100,
        maxPageButtons: 5,
      },
    })

    await nextTick()

    const pageButtons = wrapper.findAll('.page-btn:not(.ellipsis)')
    expect(pageButtons.length).toBeLessThanOrEqual(5)
  })

  it('应该支持右对齐', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        align: 'right',
      },
    })

    await nextTick()

    expect(wrapper.find('.justify-end').exists()).toBe(true)
  })

  it('应该支持居中对齐', async () => {
    const wrapper = createTestWrapper(Pagination, {
      props: {
        ...defaultProps,
        align: 'center',
      },
    })

    await nextTick()

    expect(wrapper.find('.justify-center').exists()).toBe(true)
  })
})