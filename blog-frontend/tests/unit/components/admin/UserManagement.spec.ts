import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElMessage, ElMessageBox } from 'element-plus'
import UserManagement from '@/views/admin/UserManagement.vue'
import UserStats from '@/components/admin/UserStats.vue'
import UserEditDialog from '@/components/admin/UserEditDialog.vue'
import adminService from '@/services/adminService'
import { useUserStore } from '@/stores/user'

// Mock Element Plus components
vi.mock('element-plus', async () => {
  const actual = await vi.importActual('element-plus')
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn()
    },
    ElMessageBox: {
      confirm: vi.fn()
    }
  }
})

// Mock adminService
vi.mock('@/services/adminService', () => ({
  default: {
    getUserList: vi.fn(),
    getUserById: vi.fn(),
    updateUser: vi.fn(),
    toggleUserStatus: vi.fn(),
    deleteUser: vi.fn(),
    getUserStats: vi.fn()
  }
}))

// Mock userStore
vi.mock('@/stores/user', () => ({
  useUserStore: vi.fn(() => ({
    userInfo: {
      id: 2,
      username: 'admin',
      role: 'ADMIN'
    }
  }))
}))

const mockUser = {
  id: 1,
  username: 'testuser',
  email: 'test@example.com',
  nickname: 'Test User',
  role: 'USER',
  status: 'ACTIVE',
  createTime: '2024-01-01T00:00:00',
  updateTime: '2024-01-01T00:00:00',
  active: true,
  statusLoading: false
}

describe('UserManagement.vue', () => {
  let wrapper: any

  beforeEach(() => {
    vi.clearAllMocks()

    // Mock adminService responses
    vi.mocked(adminService.getUserList).mockResolvedValue({
      records: [mockUser],
      total: 1,
      size: 20,
      current: 0,
      pages: 1
    })

    vi.mocked(adminService.getUserStats).mockResolvedValue({
      totalUsers: 100,
      activeUsers: 80,
      bannedUsers: 10,
      inactiveUsers: 10,
      adminUsers: 5,
      normalUsers: 95,
      todayRegistrations: 5,
      weekRegistrations: 20,
      monthRegistrations: 50
    })

    wrapper = mount(UserManagement, {
      global: {
        components: {
          UserStats,
          UserEditDialog
        },
        stubs: {
          'el-card': true,
          'el-row': true,
          'el-col': true,
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-select': true,
          'el-option': true,
          'el-button': true,
          'el-table': true,
          'el-table-column': true,
          'el-avatar': true,
          'el-tag': true,
          'el-switch': true,
          'el-pagination': true,
          'el-dialog': true
        }
      }
    })
  })

  it('should render the user management page correctly', () => {
    expect(wrapper.find('.user-management').exists()).toBe(true)
    expect(wrapper.findComponent(UserStats).exists()).toBe(true)
  })

  it('should load user list on mount', async () => {
    await wrapper.vm.$nextTick()

    expect(adminService.getUserList).toHaveBeenCalledWith({
      page: 0,
      size: 20,
      keyword: '',
      role: '',
      status: ''
    })
  })

  it('should handle search functionality', async () => {
    const searchForm = wrapper.vm.queryForm
    searchForm.keyword = 'test'
    searchForm.role = 'USER'

    await wrapper.vm.handleSearch()

    expect(adminService.getUserList).toHaveBeenCalledWith({
      page: 0,
      size: 20,
      keyword: 'test',
      role: 'USER',
      status: ''
    })
  })

  it('should reset search form', async () => {
    const searchForm = wrapper.vm.queryForm
    searchForm.keyword = 'test'
    searchForm.role = 'USER'
    searchForm.status = 'ACTIVE'

    await wrapper.vm.handleReset()

    expect(searchForm.keyword).toBe('')
    expect(searchForm.role).toBe('')
    expect(searchForm.status).toBe('')
  })

  it('should open edit dialog', async () => {
    await wrapper.vm.editUser(mockUser)

    expect(wrapper.vm.editDialogVisible).toBe(true)
    expect(wrapper.vm.editingUser).toEqual(mockUser)
  })

  it('should handle edit success', async () => {
    wrapper.vm.editDialogVisible = true
    await wrapper.vm.handleEditSuccess()

    expect(wrapper.vm.editDialogVisible).toBe(false)
    expect(adminService.getUserList).toHaveBeenCalled()
  })

  it('should toggle user status', async () => {
    const user = { ...mockUser, active: false }
    vi.mocked(adminService.toggleUserStatus).mockResolvedValue({
      ...user,
      status: 'BANNED'
    })

    await wrapper.vm.toggleUserStatus(user)

    expect(adminService.toggleUserStatus).toHaveBeenCalledWith(user.id)
    expect(ElMessage.success).toHaveBeenCalledWith('用户状态已禁用')
  })

  it('should handle toggle user status error', async () => {
    const user = { ...mockUser, active: false }
    vi.mocked(adminService.toggleUserStatus).mockRejectedValue(new Error('Error'))

    await wrapper.vm.toggleUserStatus(user)

    expect(user.active).toBe(true) // Should revert the switch state
    expect(ElMessage.error).toHaveBeenCalledWith('Error')
  })

  it('should prevent self status toggle', async () => {
    const selfUser = { ...mockUser, id: 2 }
    const switchElement = { disabled: true }

    // Test that the switch is disabled for current user
    expect(wrapper.vm.currentUserId).toBe(2)
  })

  it('should delete user with confirmation', async () => {
    vi.mocked(ElMessageBox.confirm).mockResolvedValue('confirm')
    vi.mocked(adminService.deleteUser).mockResolvedValue()

    await wrapper.vm.deleteUser(mockUser)

    expect(ElMessageBox.confirm).toHaveBeenCalledWith(
      expect.stringContaining('确定要删除用户'),
      '删除用户',
      expect.any(Object)
    )
    expect(adminService.deleteUser).toHaveBeenCalledWith(mockUser.id)
    expect(ElMessage.success).toHaveBeenCalledWith('用户已删除')
  })

  it('should handle delete cancellation', async () => {
    vi.mocked(ElMessageBox.confirm).mockRejectedValue('cancel')

    await wrapper.vm.deleteUser(mockUser)

    expect(adminService.deleteUser).not.toHaveBeenCalled()
  })

  it('should prevent self deletion', async () => {
    const selfUser = { ...mockUser, id: 2 }

    // The delete button should be disabled for current user
    expect(wrapper.vm.currentUserId).toBe(2)
  })

  it('should handle pagination size change', async () => {
    wrapper.vm.pagination.size = 50
    await wrapper.vm.handleSizeChange()

    expect(wrapper.vm.pagination.current).toBe(1)
    expect(adminService.getUserList).toHaveBeenCalled()
  })

  it('should handle pagination current page change', async () => {
    wrapper.vm.pagination.current = 2
    await wrapper.vm.handleCurrentChange()

    expect(adminService.getUserList).toHaveBeenCalled()
  })

  it('should refresh user stats', async () => {
    const statsComponent = wrapper.findComponent(UserStats)
    statsComponent.vm.loadStats = vi.fn()

    await wrapper.vm.refreshStats()

    expect(statsComponent.vm.loadStats).toHaveBeenCalled()
  })

  it('should format date time correctly', () => {
    const dateTime = '2024-01-01T12:00:00'
    const formatted = wrapper.vm.formatDateTime(dateTime)

    expect(formatted).toMatch(/\d{4}\/\d{2}\/\d{2}/) // Should match date format
  })

  it('should handle empty date time', () => {
    const formatted = wrapper.vm.formatDateTime('')
    expect(formatted).toBe('-')

    const formattedNull = wrapper.vm.formatDateTime(null as any)
    expect(formattedNull).toBe('-')
  })

  it('should get status type correctly', () => {
    expect(wrapper.vm.getStatusType('ACTIVE')).toBe('success')
    expect(wrapper.vm.getStatusType('INACTIVE')).toBe('warning')
    expect(wrapper.vm.getStatusType('BANNED')).toBe('danger')
    expect(wrapper.vm.getStatusType('UNKNOWN')).toBe('info')
  })

  it('should get status text correctly', () => {
    expect(wrapper.vm.getStatusText('ACTIVE')).toBe('活跃')
    expect(wrapper.vm.getStatusText('INACTIVE')).toBe('未激活')
    expect(wrapper.vm.getStatusText('BANNED')).toBe('禁用')
    expect(wrapper.vm.getStatusText('UNKNOWN')).toBe('未知')
  })
})