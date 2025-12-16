import { describe, it, expect, vi, beforeEach } from 'vitest'
import { nextTick } from 'vue'
import ShareButtons from '@/components/common/ShareButtons.vue'
import { createTestWrapper } from '@/test/utils'

// Mock navigator clipboard API
const mockWriteText = vi.fn()
Object.assign(navigator, {
  clipboard: {
    writeText: mockWriteText,
  },
})

// Mock window.open
const mockWindowOpen = vi.fn()
Object.assign(window, {
  open: mockWindowOpen,
})

describe('ShareButtons.vue', () => {
  const defaultProps = {
    url: 'https://example.com/article/1',
    title: '测试文章标题',
    description: '这是测试文章的描述',
  }

  beforeEach(() => {
    mockWriteText.mockClear()
    mockWindowOpen.mockClear()
  })

  it('应该正确渲染分享按钮', async () => {
    const wrapper = createTestWrapper(ShareButtons, {
      props: defaultProps,
    })

    await nextTick()

    // 检查各个分享按钮是否存在
    expect(wrapper.find('.fa-weibo').exists()).toBe(true)
    expect(wrapper.find('.fa-weixin').exists()).toBe(true)
    expect(wrapper.find('.fa-qq').exists()).toBe(true)
    expect(wrapper.find('.fa-link').exists()).toBe(true)
    expect(wrapper.find('.fa-qrcode').exists()).toBe(true)
  })

  it('应该在点击复制链接时复制URL到剪贴板', async () => {
    mockWriteText.mockResolvedValue(undefined)

    const wrapper = createTestWrapper(ShareButtons, {
      props: defaultProps,
    })

    await nextTick()

    const copyButton = wrapper.find('button:has(.fa-link)')
    await copyButton.trigger('click')

    expect(mockWriteText).toHaveBeenCalledWith(defaultProps.url)
  })

  it('应该在复制链接成功时显示成功提示', async () => {
    mockWriteText.mockResolvedValue(undefined)

    const wrapper = createTestWrapper(ShareButtons, {
      props: defaultProps,
    })

    await nextTick()

    const copyButton = wrapper.find('button:has(.fa-link)')
    await copyButton.trigger('click')
    await nextTick()

    expect(wrapper.text()).toContain('链接已复制')
  })

  it('应该在复制链接失败时显示错误提示', async () => {
    mockWriteText.mockRejectedValue(new Error('复制失败'))

    const wrapper = createTestWrapper(ShareButtons, {
      props: defaultProps,
    })

    await nextTick()

    const copyButton = wrapper.find('button:has(.fa-link)')
    await copyButton.trigger('click')
    await nextTick()

    expect(wrapper.text()).toContain('复制失败')
  })

  it('应该在点击微博分享时打开微博分享窗口', async () => {
    const wrapper = createTestWrapper(ShareButtons, {
      props: defaultProps,
    })

    await nextTick()

    const weiboButton = wrapper.find('button:has(.fa-weibo)')
    await weiboButton.trigger('click')

    expect(mockWindowOpen).toHaveBeenCalled()
    const shareUrl = mockWindowOpen.mock.calls[0][0]
    expect(shareUrl).toContain('service.weibo.com')
    expect(shareUrl).toContain(encodeURIComponent(defaultProps.title))
    expect(shareUrl).toContain(encodeURIComponent(defaultProps.url))
  })

  it('应该在点击微信分享时显示二维码', async () => {
    const wrapper = createTestWrapper(ShareButtons, {
      props: defaultProps,
    })

    await nextTick()

    const wechatButton = wrapper.find('button:has(.fa-weixin)')
    await wechatButton.trigger('click')
    await nextTick()

    expect(wrapper.find('.qrcode-modal').exists()).toBe(true)
    expect(wrapper.find('.qrcode-container').exists()).toBe(true)
  })

  it('应该在点击QQ分享时打开QQ分享窗口', async () => {
    const wrapper = createTestWrapper(ShareButtons, {
      props: defaultProps,
    })

    await nextTick()

    const qqButton = wrapper.find('button:has(.fa-qq)')
    await qqButton.trigger('click')

    expect(mockWindowOpen).toHaveBeenCalled()
    const shareUrl = mockWindowOpen.mock.calls[0][0]
    expect(shareUrl).toContain('connect.qq.com')
    expect(shareUrl).toContain(encodeURIComponent(defaultProps.title))
    expect(shareUrl).toContain(encodeURIComponent(defaultProps.url))
  })

  it('应该在点击二维码按钮时显示二维码模态框', async () => {
    const wrapper = createTestWrapper(ShareButtons, {
      props: defaultProps,
    })

    await nextTick()

    const qrButton = wrapper.find('button:has(.fa-qrcode)')
    await qrButton.trigger('click')
    await nextTick()

    expect(wrapper.find('.qrcode-modal').exists()).toBe(true)
  })

  it('应该能关闭二维码模态框', async () => {
    const wrapper = createTestWrapper(ShareButtons, {
      props: defaultProps,
    })

    await nextTick()

    // 打开模态框
    const qrButton = wrapper.find('button:has(.fa-qrcode)')
    await qrButton.trigger('click')
    await nextTick()

    expect(wrapper.find('.qrcode-modal').exists()).toBe(true)

    // 点击关闭按钮
    const closeButton = wrapper.find('.qrcode-modal button:has(.fa-times)')
    await closeButton.trigger('click')
    await nextTick()

    expect(wrapper.find('.qrcode-modal').exists()).toBe(false)
  })

  it('应该在点击模态框背景时关闭模态框', async () => {
    const wrapper = createTestWrapper(ShareButtons, {
      props: defaultProps,
    })

    await nextTick()

    // 打开模态框
    const qrButton = wrapper.find('button:has(.fa-qrcode)')
    await qrButton.trigger('click')
    await nextTick()

    // 点击背景
    const modal = wrapper.find('.qrcode-modal')
    await modal.trigger('click')
    await nextTick()

    expect(wrapper.find('.qrcode-modal').exists()).toBe(false)
  })

  it('应该支持自定义分享平台', async () => {
    const customPlatforms = ['weibo', 'qq']
    const wrapper = createTestWrapper(ShareButtons, {
      props: {
        ...defaultProps,
        platforms: customPlatforms,
      },
    })

    await nextTick()

    expect(wrapper.find('.fa-weibo').exists()).toBe(true)
    expect(wrapper.find('.fa-qq').exists()).toBe(true)
    expect(wrapper.find('.fa-weixin').exists()).toBe(false)
    expect(wrapper.find('.fa-link').exists()).toBe(false)
  })

  it('应该支持禁用某些平台', async () => {
    const wrapper = createTestWrapper(ShareButtons, {
      props: {
        ...defaultProps,
        disabledPlatforms: ['weixin'],
      },
    })

    await nextTick()

    expect(wrapper.find('.fa-weixin').exists()).toBe(false)
    expect(wrapper.find('.fa-weibo').exists()).toBe(true)
  })

  it('应该支持自定义图标大小', async () => {
    const wrapper = createTestWrapper(ShareButtons, {
      props: {
        ...defaultProps,
        size: 'large',
      },
    })

    await nextTick()

    const buttons = wrapper.findAll('button')
    buttons.forEach(button => {
      expect(button.classes()).toContain('text-xl')
    })
  })

  it('应该支持自定义主题颜色', async () => {
    const wrapper = createTestWrapper(ShareButtons, {
      props: {
        ...defaultProps,
        theme: 'dark',
      },
    })

    await nextTick()

    const buttons = wrapper.findAll('button')
    buttons.forEach(button => {
      expect(button.classes()).toContain('bg-gray-800')
      expect(button.classes()).toContain('text-white')
    })
  })

  it('应该支持显示分享统计', async () => {
    const shareStats = {
      weibo: 10,
      qq: 5,
      weixin: 15,
    }

    const wrapper = createTestWrapper(ShareButtons, {
      props: {
        ...defaultProps,
        showStats: true,
        shareStats,
      },
    })

    await nextTick()

    expect(wrapper.text()).toContain('10')
    expect(wrapper.text()).toContain('5')
    expect(wrapper.text()).toContain('15')
  })

  it('应该支持自定义分享文本模板', async () => {
    const customTemplate = '快来看看这篇文章：{title} {url}'

    const wrapper = createTestWrapper(ShareButtons, {
      props: {
        ...defaultProps,
        template: customTemplate,
      },
    })

    await nextTick()

    const weiboButton = wrapper.find('button:has(.fa-weibo)')
    await weiboButton.trigger('click')

    const shareUrl = mockWindowOpen.mock.calls[0][0]
    expect(shareUrl).toContain(
      encodeURIComponent(`快来看看这篇文章：${defaultProps.title} ${defaultProps.url}`)
    )
  })

  it('应该在URL为空时不显示分享按钮', async () => {
    const wrapper = createTestWrapper(ShareButtons, {
      props: {
        ...defaultProps,
        url: '',
      },
    })

    await nextTick()

    expect(wrapper.find('.share-buttons').exists()).toBe(false)
  })

  it('应该支持回调函数', async () => {
    const onShare = vi.fn()

    const wrapper = createTestWrapper(ShareButtons, {
      props: {
        ...defaultProps,
        onShare,
      },
    })

    await nextTick()

    const weiboButton = wrapper.find('button:has(.fa-weibo)')
    await weiboButton.trigger('click')

    expect(onShare).toHaveBeenCalledWith({
      platform: 'weibo',
      url: defaultProps.url,
      title: defaultProps.title,
    })
  })

  it('应该正确处理键盘事件', async () => {
    const wrapper = createTestWrapper(ShareButtons, {
      props: defaultProps,
    })

    await nextTick()

    const copyButton = wrapper.find('button:has(.fa-link)')
    await copyButton.trigger('keydown.enter')

    expect(mockWriteText).toHaveBeenCalledWith(defaultProps.url)
  })

  it('应该支持加载状态', async () => {
    const wrapper = createTestWrapper(ShareButtons, {
      props: {
        ...defaultProps,
        loading: true,
      },
    })

    await nextTick()

    expect(wrapper.find('.fa-spinner').exists()).toBe(true)

    const buttons = wrapper.findAll('button')
    buttons.forEach(button => {
      expect(button.attributes('disabled')).toBeDefined()
    })
  })

  it('应该支持禁用状态', async () => {
    const wrapper = createTestWrapper(ShareButtons, {
      props: {
        ...defaultProps,
        disabled: true,
      },
    })

    await nextTick()

    const buttons = wrapper.findAll('button')
    buttons.forEach(button => {
      expect(button.classes()).toContain('opacity-50')
      expect(button.classes()).toContain('cursor-not-allowed')
    })
  })
})