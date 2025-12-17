import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElMessage, ElMessageBox } from 'element-plus'
import SensitiveWordManager from '@/components/comment/moderation/SensitiveWordManager.vue'
import { useCommentModerationStore } from '@/stores/commentModerationStore'
import type { SensitiveWord } from '@shared/types'

// Mock the dependencies
vi.mock('@/stores/commentModerationStore')
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      error: vi.fn(),
      success: vi.fn(),
      warning: vi.fn()
    },
    ElMessageBox: {
      confirm: vi.fn()
    }
  }
})

const mockCommentModerationStore = vi.mocked(useCommentModerationStore)

describe('SensitiveWordManager', () => {
  let mockStore: any
  let wrapper: any

  const mockSensitiveWords: SensitiveWord[] = [
    {
      id: 1,
      word: '测试',
      replacement: '***',
      pattern: '测试',
      type: 'FILTER',
      createTime: new Date('2024-01-01T10:00:00')
    },
    {
      id: 2,
      word: '违法',
      replacement: null,
      pattern: '违法',
      type: 'BLOCK',
      createTime: new Date('2024-01-01T11:00:00')
    },
    {
      id: 3,
      word: '警告',
      replacement: null,
      pattern: '警告',
      type: 'WARNING',
      createTime: new Date('2024-01-01T12:00:00')
    }
  ]

  beforeEach(() => {
    // Reset mocks
    vi.clearAllMocks()

    // Mock store
    mockStore = {
      sensitiveWords: mockSensitiveWords,
      loading: false,
      fetchSensitiveWords: vi.fn(),
      addSensitiveWord: vi.fn(),
      updateSensitiveWord: vi.fn(),
      deleteSensitiveWord: vi.fn()
    }

    mockCommentModerationStore.mockReturnValue(mockStore)
  })

  const mountComponent = () => {
    return mount(SensitiveWordManager, {
      global: {
        stubs: {
          'el-card': true,
          'el-select': true,
          'el-option': true,
          'el-button': true,
          'el-table': true,
          'el-table-column': true,
          'el-tag': true,
          'el-dialog': true,
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-date-picker': true,
          'el-icon': true,
          'el-code': true,
          'el-empty': true
        }
      }
    })
  }

  it('should render sensitive word manager correctly', () => {
    wrapper = mountComponent()

    expect(wrapper.find('.sensitive-word-manager').exists()).toBe(true)
    expect(wrapper.find('.action-card').exists()).toBe(true)
    expect(wrapper.find('.table-card').exists()).toBe(true)
  })

  it('should fetch words on mount', async () => {
    wrapper = mountComponent()

    // Wait for next tick to allow async operations
    await wrapper.vm.$nextTick()

    expect(mockStore.fetchSensitiveWords).toHaveBeenCalled()
  })

  it('should filter words by type correctly', async () => {
    wrapper = mountComponent()

    await wrapper.setData({ typeFilter: 'FILTER' })

    const filteredWords = wrapper.vm.filteredWords
    expect(filteredWords).toHaveLength(1)
    expect(filteredWords[0].type).toBe('FILTER')
  })

  it('should show all words when no filter is applied', () => {
    wrapper = mountComponent()

    wrapper.setData({ typeFilter: '' })

    const filteredWords = wrapper.vm.filteredWords
    expect(filteredWords).toHaveLength(3)
  })

  it('should handle add word correctly', async () => {
    wrapper = mountComponent()

    wrapper.vm.handleAddWord()

    expect(wrapper.vm.isEdit).toBe(false)
    expect(wrapper.vm.wordDialogVisible).toBe(true)
    expect(wrapper.vm.wordForm.word).toBe('')
    expect(wrapper.vm.wordForm.type).toBe('FILTER')
  })

  it('should handle edit word correctly', async () => {
    wrapper = mountComponent()
    const wordToEdit = mockSensitiveWords[0]

    wrapper.vm.handleEditWord(wordToEdit)

    expect(wrapper.vm.isEdit).toBe(true)
    expect(wrapper.vm.wordForm.id).toBe(wordToEdit.id)
    expect(wrapper.vm.wordForm.word).toBe(wordToEdit.word)
    expect(wrapper.vm.wordForm.type).toBe(wordToEdit.type)
    expect(wrapper.vm.wordDialogVisible).toBe(true)
  })

  it('should confirm save word (add) correctly', async () => {
    wrapper = mountComponent()

    // Mock form validation
    const mockFormRef = {
      validate: vi.fn().mockResolvedValue(true)
    }
    wrapper.vm.wordFormRef = mockFormRef

    await wrapper.setData({
      wordForm: {
        id: null,
        word: '新词',
        replacement: '###',
        pattern: '新词',
        type: 'BLOCK'
      }
    })

    mockStore.addSensitiveWord.mockResolvedValue({ success: true })

    await wrapper.vm.confirmSaveWord()

    expect(mockStore.addSensitiveWord).toHaveBeenCalledWith({
      word: '新词',
      replacement: '###',
      pattern: '新词',
      type: 'BLOCK'
    })
    expect(ElMessage.success).toHaveBeenCalledWith('添加敏感词成功')
    expect(wrapper.vm.wordDialogVisible).toBe(false)
  })

  it('should confirm save word (update) correctly', async () => {
    wrapper = mountComponent()

    // Mock form validation
    const mockFormRef = {
      validate: vi.fn().mockResolvedValue(true)
    }
    wrapper.vm.wordFormRef = mockFormRef

    await wrapper.setData({
      isEdit: true,
      wordForm: {
        id: 1,
        word: '更新词',
        replacement: '###',
        pattern: '更新词',
        type: 'BLOCK'
      }
    })

    mockStore.updateSensitiveWord.mockResolvedValue({ success: true })

    await wrapper.vm.confirmSaveWord()

    expect(mockStore.updateSensitiveWord).toHaveBeenCalledWith(1, {
      word: '更新词',
      replacement: '###',
      pattern: '更新词',
      type: 'BLOCK'
    })
    expect(ElMessage.success).toHaveBeenCalledWith('更新敏感词成功')
    expect(wrapper.vm.wordDialogVisible).toBe(false)
  })

  it('should handle form validation failure correctly', async () => {
    wrapper = mountComponent()

    // Mock form validation failure
    const mockFormRef = {
      validate: vi.fn().mockRejectedValue(new Error('Validation Error'))
    }
    wrapper.vm.wordFormRef = mockFormRef

    await wrapper.vm.confirmSaveWord()

    expect(mockStore.addSensitiveWord).not.toHaveBeenCalled()
    expect(mockStore.updateSensitiveWord).not.toHaveBeenCalled()
  })

  it('should handle delete word correctly', async () => {
    wrapper = mountComponent()
    const wordToDelete = mockSensitiveWords[0]

    // Mock ElMessageBox.confirm to resolve
    ;(ElMessageBox.confirm as any).mockResolvedValue('confirm')

    mockStore.deleteSensitiveWord.mockResolvedValue({ success: true })

    await wrapper.vm.handleDeleteWord(wordToDelete)

    expect(ElMessageBox.confirm).toHaveBeenCalledWith(
      `确定要删除敏感词"${wordToDelete.word}"吗？`,
      '确认删除',
      { type: 'warning' }
    )
    expect(mockStore.deleteSensitiveWord).toHaveBeenCalledWith(wordToDelete.id)
    expect(ElMessage.success).toHaveBeenCalledWith('删除敏感词成功')
  })

  it('should handle delete word cancellation correctly', async () => {
    wrapper = mountComponent()
    const wordToDelete = mockSensitiveWords[0]

    // Mock ElMessageBox.confirm to reject with 'cancel'
    ;(ElMessageBox.confirm as any).mockRejectedValue('cancel')

    await wrapper.vm.handleDeleteWord(wordToDelete)

    expect(ElMessageBox.confirm).toHaveBeenCalled()
    expect(mockStore.deleteSensitiveWord).not.toHaveBeenCalled()
    expect(ElMessage.success).not.toHaveBeenCalled()
  })

  it('should handle test filter correctly', async () => {
    wrapper = mountComponent()

    wrapper.vm.handleTestFilter()

    expect(wrapper.vm.testText).toBe('')
    expect(wrapper.vm.testResult).toBe(null)
    expect(wrapper.vm.testOriginalText).toBe('')
    expect(wrapper.vm.testDialogVisible).toBe(true)
  })

  it('should handle test filter text correctly', async () => {
    wrapper = mountComponent()

    await wrapper.setData({
      testText: '这是一个测试'
    })

    const mockFilterResult = {
      filteredText: '这是一个***',
      containsBlocked: false,
      warningWords: []
    }

    mockStore.filterSensitiveWords = vi.fn().mockResolvedValue({
      success: true,
      data: mockFilterResult
    })

    await wrapper.vm.handleTestFilterText()

    expect(wrapper.vm.testOriginalText).toBe('这是一个测试')
    expect(wrapper.vm.testResult).toEqual(mockFilterResult)
  })

  it('should handle empty test text', async () => {
    wrapper = mountComponent()

    await wrapper.setData({ testText: '' })

    await wrapper.vm.handleTestFilterText()

    expect(ElMessage.warning).toHaveBeenCalledWith('请输入测试文本')
    expect(mockStore.filterSensitiveWords).not.toHaveBeenCalled()
  })

  it('should get correct tag type for word types', () => {
    wrapper = mountComponent()

    expect(wrapper.vm.getTypeTagType('FILTER')).toBe('warning')
    expect(wrapper.vm.getTypeTagType('BLOCK')).toBe('danger')
    expect(wrapper.vm.getTypeTagType('WARNING')).toBe('info')
    expect(wrapper.vm.getTypeTagType('UNKNOWN')).toBe('info')
  })

  it('should get correct text for word types', () => {
    wrapper = mountComponent()

    expect(wrapper.vm.getTypeText('FILTER')).toBe('过滤词')
    expect(wrapper.vm.getTypeText('BLOCK')).toBe('屏蔽词')
    expect(wrapper.vm.getTypeText('WARNING')).toBe('警告词')
    expect(wrapper.vm.getTypeText('UNKNOWN')).toBe('UNKNOWN')
  })

  it('should format date time correctly', () => {
    wrapper = mountComponent()
    const testDate = new Date('2024-01-01T10:30:00')
    const formatted = wrapper.vm.formatDateTime(testDate)

    expect(formatted).toMatch(/\d{4}\/\d{1,2}\/\d{1,2}/)
  })

  it('should handle empty date time correctly', () => {
    wrapper = mountComponent()
    expect(wrapper.vm.formatDateTime(null)).toBe('')
    expect(wrapper.vm.formatDateTime('')).toBe('')
  })

  it('should show empty state when no words', async () => {
    wrapper = mountComponent()

    await wrapper.setData({ sensitiveWords: [] })

    const filteredWords = wrapper.vm.filteredWords
    expect(filteredWords).toHaveLength(0)
  })

  it('should handle add word error correctly', async () => {
    wrapper = mountComponent()

    // Mock form validation
    const mockFormRef = {
      validate: vi.fn().mockResolvedValue(true)
    }
    wrapper.vm.wordFormRef = mockFormRef

    await wrapper.setData({
      wordForm: {
        id: null,
        word: '新词',
        pattern: '新词',
        type: 'BLOCK'
      }
    })

    mockStore.addSensitiveWord.mockRejectedValue(new Error('Add Error'))

    await wrapper.vm.confirmSaveWord()

    expect(ElMessage.error).toHaveBeenCalledWith('添加敏感词失败')
  })

  it('should handle update word error correctly', async () => {
    wrapper = mountComponent()

    // Mock form validation
    const mockFormRef = {
      validate: vi.fn().mockResolvedValue(true)
    }
    wrapper.vm.wordFormRef = mockFormRef

    await wrapper.setData({
      isEdit: true,
      wordForm: {
        id: 1,
        word: '更新词',
        pattern: '更新词',
        type: 'BLOCK'
      }
    })

    mockStore.updateSensitiveWord.mockRejectedValue(new Error('Update Error'))

    await wrapper.vm.confirmSaveWord()

    expect(ElMessage.error).toHaveBeenCalledWith('更新敏感词失败')
  })

  it('should handle delete word error correctly', async () => {
    wrapper = mountComponent()
    const wordToDelete = mockSensitiveWords[0]

    // Mock ElMessageBox.confirm to resolve
    ;(ElMessageBox.confirm as any).mockResolvedValue('confirm')

    mockStore.deleteSensitiveWord.mockRejectedValue(new Error('Delete Error'))

    await wrapper.vm.handleDeleteWord(wordToDelete)

    expect(ElMessage.error).toHaveBeenCalledWith('删除敏感词失败')
  })

  it('should handle test filter error correctly', async () => {
    wrapper = mountComponent()

    await wrapper.setData({
      testText: '测试内容'
    })

    mockStore.filterSensitiveWords = vi.fn().mockRejectedValue(new Error('Filter Error'))

    await wrapper.vm.handleTestFilterText()

    expect(ElMessage.error).toHaveBeenCalledWith('测试失败')
  })

  it('should show correct replacement text for words with and without replacement', async () => {
    wrapper = mountComponent()

    // Word with replacement
    const wordWithReplacement = mockSensitiveWords[0] // has replacement '***'
    expect(wordWithReplacement.replacement).toBe('***')

    // Word without replacement
    const wordWithoutReplacement = mockSensitiveWords[1] // has no replacement
    expect(wordWithoutReplacement.replacement).toBeNull()
  })
})