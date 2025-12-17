import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElMessage, ElMessageBox } from 'element-plus'
import ContentManagement from '@/components/admin/ContentManagement.vue'
import ArticleReviewDialog from '@/components/admin/ArticleReviewDialog.vue'
import ExportDialog from '@/components/admin/ExportDialog.vue'
import adminContentService from '@/services/adminContentService'

// Mock Element Plus components
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

// Mock adminContentService
vi.mock('@/services/adminContentService', () => ({
  default: {
    getArticles: vi.fn(),
    getComments: vi.fn(),
    reviewArticle: vi.fn(),
    batchOperateArticles: vi.fn(),
    batchOperateComments: vi.fn(),
    exportArticles: vi.fn(),
    exportComments: vi.fn(),
    checkContent: vi.fn(),
    filterContent: vi.fn(),
    downloadFile: vi.fn().mockReturnValue('/admin/download/test.json')
  }
}))

describe('ContentManagement', () => {
  let wrapper: any

  const mockArticles = {
    data: {
      records: [
        {
          id: 1,
          title: '测试文章1',
          content: '测试内容1',
          status: 'PUBLISHED',
          authorName: '作者1',
          categoryName: '分类1',
          viewCount: 100,
          likeCount: 10,
          commentCount: 5,
          createTime: '2023-12-16T10:00:00'
        }
      ],
      total: 1
    }
  }

  const mockComments = {
    data: {
      records: [
        {
          id: 1,
          content: '测试评论1',
          articleId: 1,
          userName: '用户1',
          status: 'NORMAL',
          likeCount: 3,
          createTime: '2023-12-16T10:00:00'
        }
      ],
      total: 1
    }
  }

  beforeEach(() => {
    // Reset mocks
    vi.clearAllMocks()

    // Mock successful API responses
    vi.mocked(adminContentService.getArticles).mockResolvedValue(mockArticles)
    vi.mocked(adminContentService.getComments).mockResolvedValue(mockComments)
    vi.mocked(adminContentService.reviewArticle).mockResolvedValue({ data: mockArticles.data.records[0] })
    vi.mocked(adminContentService.batchOperateArticles).mockResolvedValue({ data: {} })
    vi.mocked(adminContentService.batchOperateComments).mockResolvedValue({ data: {} })
    vi.mocked(adminContentService.exportArticles).mockResolvedValue({
      data: {
        fileName: 'articles_export.json',
        downloadUrl: '/admin/download/articles_export.json',
        fileSize: 1024,
        format: 'json',
        recordCount: 1,
        exportTime: '2023-12-16 10:00:00'
      }
    })
    vi.mocked(ElMessageBox.confirm).mockResolvedValue(true)

    wrapper = mount(ContentManagement, {
      global: {
        components: {
          ArticleReviewDialog,
          ExportDialog
        },
        stubs: {
          'el-card': true,
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-date-picker': true,
          'el-button': true,
          'el-alert': true,
          'el-table': true,
          'el-table-column': true,
          'el-pagination': true,
          'el-dialog': true,
          'el-radio-group': true,
          'el-radio-button': true,
          'el-tag': true
        }
      }
    })
  })

  it('renders correctly', () => {
    expect(wrapper.find('.content-management').exists()).toBe(true)
    expect(wrapper.find('.card-header').exists()).toBe(true)
    expect(wrapper.find('.search-form').exists()).toBe(true)
  })

  it('initializes with correct default values', () => {
    expect(wrapper.vm.activeTab).toBe('articles')
    expect(wrapper.vm.loading).toBe(false)
    expect(wrapper.vm.exporting).toBe(false)
    expect(wrapper.vm.batchOperating).toBe(false)
    expect(wrapper.vm.tableData).toEqual([])
    expect(wrapper.vm.total).toBe(0)
    expect(wrapper.vm.selectedRows).toEqual([])
  })

  it('loads articles data on mount', async () => {
    await wrapper.vm.$nextTick()

    expect(adminContentService.getArticles).toHaveBeenCalled()
  })

  it('switches tab correctly', async () => {
    const radioButtons = wrapper.findAllComponents({ name: 'ElRadioButton' })

    // Switch to comments tab
    await wrapper.vm.handleTabChange()
    expect(wrapper.vm.activeTab).toBe('comments')
  })

  it('handles search functionality', async () => {
    wrapper.vm.queryParams.keyword = 'test'
    wrapper.vm.queryParams.status = 'PUBLISHED'

    await wrapper.vm.handleSearch()

    expect(adminContentService.getArticles).toHaveBeenCalledWith(
      expect.objectContaining({
        keyword: 'test',
        status: 'PUBLISHED',
        page: 1
      })
    )
  })

  it('resets query correctly', async () => {
    // Set some values
    wrapper.vm.queryParams.keyword = 'test'
    wrapper.vm.queryParams.status = 'PUBLISHED'
    wrapper.vm.dateRange = ['2023-12-01', '2023-12-31']

    await wrapper.vm.resetQuery()

    expect(wrapper.vm.queryParams.keyword).toBe('')
    expect(wrapper.vm.queryParams.status).toBe('')
    expect(wrapper.vm.dateRange).toBeNull()
    expect(adminContentService.getArticles).toHaveBeenCalled()
  })

  it('handles article selection', async () => {
    const mockSelectedRows = [{ id: 1 }, { id: 2 }]
    await wrapper.vm.handleSelectionChange(mockSelectedRows)

    expect(wrapper.vm.selectedRows).toEqual(mockSelectedRows)
  })

  it('opens review dialog', async () => {
    const mockArticle = { id: 1, title: '测试文章', content: '测试内容' }

    await wrapper.vm.handleReview(mockArticle)

    expect(wrapper.vm.reviewDialogVisible).toBe(true)
    expect(wrapper.vm.currentArticle).toEqual(mockArticle)
  })

  it('handles successful article review', async () => {
    wrapper.vm.reviewDialogVisible = true

    await wrapper.vm.handleReviewSuccess()

    expect(wrapper.vm.reviewDialogVisible).toBe(false)
    expect(adminContentService.getArticles).toHaveBeenCalled()
  })

  it('handles article deletion', async () => {
    const mockArticle = { id: 1, title: '测试文章' }

    await wrapper.vm.handleDelete(mockArticle)

    expect(ElMessageBox.confirm).toHaveBeenCalledWith(
      expect.stringContaining('确定要删除这条记录吗？'),
      expect.stringContaining('确认删除'),
      expect.any(Object)
    )
  })

  it('handles batch operations', async () => {
    wrapper.vm.selectedRows = [{ id: 1 }, { id: 2 }]

    await wrapper.vm.handleBatchDelete()

    expect(ElMessageBox.confirm).toHaveBeenCalledWith(
      expect.stringContaining('确定要删除选中的 2 条记录吗？'),
      expect.stringContaining('确认批量删除'),
      expect.any(Object)
    )
  })

  it('handles batch publish operation', async () => {
    wrapper.vm.selectedRows = [{ id: 1 }, { id: 2 }]

    await wrapper.vm.handleBatchPublish()

    expect(adminContentService.batchOperateArticles).toHaveBeenCalledWith({
      operationType: 'publish',
      ids: [1, 2]
    })
  })

  it('handles batch unpublish operation', async () => {
    wrapper.vm.selectedRows = [{ id: 1 }, { id: 2 }]

    await wrapper.vm.handleBatchUnpublish()

    expect(adminContentService.batchOperateArticles).toHaveBeenCalledWith({
      operationType: 'unpublish',
      ids: [1, 2]
    })
  })

  it('opens export dialog', async () => {
    await wrapper.vm.handleExport()

    expect(wrapper.vm.exportDialogVisible).toBe(true)
  })

  it('handles export operation', async () => {
    wrapper.vm.exportDialogVisible = true

    // Mock window.open for file download
    const mockOpen = vi.fn()
    Object.defineProperty(window, 'open', {
      value: mockOpen,
      writable: true
    })

    await wrapper.vm.handleDoExport('json')

    expect(adminContentService.exportArticles).toHaveBeenCalled()
    expect(mockOpen).toHaveBeenCalled()
  })

  it('formats date time correctly', () => {
    const testDate = '2023-12-16T10:00:00'
    const result = wrapper.vm.formatDateTime(testDate)

    expect(result).toMatch(/2023/)
    expect(result).toMatch(/12/)
    expect(result).toMatch(/16/)
  })

  it('gets correct status type for articles', () => {
    expect(wrapper.vm.getStatusType('PUBLISHED')).toBe('success')
    expect(wrapper.vm.getStatusType('DRAFT')).toBe('warning')
    expect(wrapper.vm.getStatusType('DELETED')).toBe('danger')
    expect(wrapper.vm.getStatusType('UNKNOWN')).toBe('info')
  })

  it('gets correct status text for articles', () => {
    expect(wrapper.vm.getStatusText('PUBLISHED')).toBe('已发布')
    expect(wrapper.vm.getStatusText('DRAFT')).toBe('草稿')
    expect(wrapper.vm.getStatusText('DELETED')).toBe('已删除')
    expect(wrapper.vm.getStatusText('NORMAL')).toBe('正常')
    expect(wrapper.vm.getStatusText('UNKNOWN')).toBe('UNKNOWN')
  })

  it('handles date range change', async () => {
    const testDates = ['2023-12-01', '2023-12-31']

    await wrapper.vm.handleDateChange(testDates)

    expect(wrapper.vm.queryParams.startTime).toBe('2023-12-01')
    expect(wrapper.vm.queryParams.endTime).toBe('2023-12-31')
  })

  it('handles empty date range', async () => {
    await wrapper.vm.handleDateChange(null)

    expect(wrapper.vm.queryParams.startTime).toBe('')
    expect(wrapper.vm.queryParams.endTime).toBe('')
  })

  it('handles export dialog close on success', async () => {
    wrapper.vm.exportDialogVisible = true

    await wrapper.vm.handleDoExport('json')

    expect(wrapper.vm.exportDialogVisible).toBe(false)
  })

  it('correctly identifies article vs comment content type', () => {
    wrapper.vm.activeTab = 'articles'
    expect(wrapper.vm.activeTab).toBe('articles')

    wrapper.vm.activeTab = 'comments'
    expect(wrapper.vm.activeTab).toBe('comments')
  })
})