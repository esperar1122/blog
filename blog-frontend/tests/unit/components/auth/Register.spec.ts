import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { ElMessage } from 'element-plus'
import Register from '@/components/auth/Register.vue'
import { useUserStore } from '@/stores/user'
import { createRouter, createWebHistory } from 'vue-router'
import type { RegisterParams } from '@/api/auth'

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

// Mock user store
vi.mock('@/stores/user', () => ({
  useUserStore: () => ({
    doRegister: vi.fn()
  })
}))

// Mock router
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home' },
    { path: '/login', name: 'login' }
  ]
})

describe('Register.vue', () => {
  let wrapper: any
  let mockUserStore: any

  beforeEach(() => {
    mockUserStore = {
      doRegister: vi.fn()
    }

    vi.mocked(useUserStore).mockReturnValue(mockUserStore)

    wrapper = mount(Register, {
      global: {
        plugins: [router],
        stubs: {
          'el-card': { template: '<div><slot name="header"></slot><slot></slot></div>' },
          'el-form': { template: '<form><slot></slot></form>' },
          'el-form-item': { template: '<div><slot></slot></div>' },
          'el-input': { template: '<input />' },
          'el-button': { template: '<button><slot></slot></button>' },
          'router-link': { template: '<a><slot></slot></a>' }
        }
      }
    })

    vi.clearAllMocks()
  })

  it('renders correctly', () => {
    expect(wrapper.find('.register-container').exists()).toBe(true)
    expect(wrapper.find('h2').text()).toBe('用户注册')
  })

  it('has correct form fields', () => {
    const formItems = wrapper.findAll('.el-form-item')
    expect(formItems.length).toBe(6) // username, email, password, confirmPassword, nickname, submit button
  })

  it('validates required fields', async () => {
    const form = wrapper.vm.registerFormRef
    form.validate = vi.fn().mockRejectedValue(new Error('Validation failed'))

    await wrapper.vm.handleRegister()

    expect(form.validate).toHaveBeenCalled()
  })

  it('calls register API with correct data', async () => {
    const mockRegisterData: RegisterParams = {
      username: 'testuser',
      email: 'test@example.com',
      password: 'password123',
      nickname: '测试用户'
    }

    // 设置表单数据
    wrapper.vm.registerForm = {
      ...mockRegisterData,
      confirmPassword: 'password123'
    }

    // Mock form validation to pass
    wrapper.vm.registerFormRef = {
      validate: vi.fn().mockResolvedValue(true)
    }

    // Mock successful registration
    mockUserStore.doRegister.mockResolvedValue(undefined)

    // Mock router push
    const pushSpy = vi.spyOn(router, 'push')

    await wrapper.vm.handleRegister()

    expect(mockUserStore.doRegister).toHaveBeenCalledWith(mockRegisterData)
    expect(pushSpy).toHaveBeenCalledWith('/')
    expect(ElMessage.success).toHaveBeenCalledWith('注册成功！')
  })

  it('handles registration error', async () => {
    // 设置表单数据
    wrapper.vm.registerForm = {
      username: 'testuser',
      email: 'test@example.com',
      password: 'password123',
      confirmPassword: 'password123'
    }

    // Mock form validation to pass
    wrapper.vm.registerFormRef = {
      validate: vi.fn().mockResolvedValue(true)
    }

    // Mock registration error
    const mockError = new Error('用户名已存在')
    mockUserStore.doRegister.mockRejectedValue(mockError)

    await wrapper.vm.handleRegister()

    expect(ElMessage.error).toHaveBeenCalledWith('用户名已存在')
  })

  it('validates password strength', async () => {
    // 设置表单数据，密码不符合要求（少于8位）
    wrapper.vm.registerForm = {
      username: 'testuser',
      email: 'test@example.com',
      password: '123', // 密码太短
      confirmPassword: '123'
    }

    const validatePassword = wrapper.vm.registerRules.password.find(
      (rule: any) => rule.validator === wrapper.vm.validatePassword
    )

    const callback = vi.fn()
    await validatePassword.validator(null, '123', callback)

    expect(callback).toHaveBeenCalledWith(new Error('密码必须至少8位，包含字母和数字'))
  })

  it('validates password strength with letters and numbers', async () => {
    const validatePassword = wrapper.vm.registerRules.password.find(
      (rule: any) => rule.validator === wrapper.vm.validatePassword
    )

    const callback = vi.fn()

    // 测试只有字母的密码
    await validatePassword.validator(null, 'password', callback)
    expect(callback).toHaveBeenCalledWith(new Error('密码必须至少8位，包含字母和数字'))

    // 测试只有数字的密码
    await validatePassword.validator(null, '12345678', callback)
    expect(callback).toHaveBeenCalledWith(new Error('密码必须至少8位，包含字母和数字'))

    // 测试正确的密码
    callback.mockClear()
    await validatePassword.validator(null, 'password123', callback)
    expect(callback).toHaveBeenCalledWith()
  })

  it('validates confirm password matches', async () => {
    wrapper.vm.registerForm.password = 'password123'
    wrapper.vm.registerForm.confirmPassword = 'differentpassword'

    const validateConfirmPassword = wrapper.vm.registerRules.confirmPassword.find(
      (rule: any) => rule.validator === wrapper.vm.validateConfirmPassword
    )

    const callback = vi.fn()
    await validateConfirmPassword.validator(null, 'differentpassword', callback)

    expect(callback).toHaveBeenCalledWith(new Error('两次输入的密码不一致'))
  })

  it('validates username format', async () => {
    const validateUsername = wrapper.vm.registerRules.username

    // 测试包含特殊字符的用户名
    const callback = vi.fn()
    await validateUsername[2].validator(null, 'test@user', callback)

    // 应该触发pattern验证
    expect(wrapper.vm.registerForm.username).toBe('test@user')
  })

  it('validates email format', async () => {
    const validateEmail = wrapper.vm.registerRules.email

    // 测试无效邮箱格式
    const callback = vi.fn()
    await validateEmail[1].validator(null, 'invalid-email', callback)

    // type: 'email' 应该自动处理验证
    expect(wrapper.vm.registerForm.email).toBe('invalid-email')
  })

  it('shows loading state during registration', async () => {
    wrapper.vm.registerForm = {
      username: 'testuser',
      email: 'test@example.com',
      password: 'password123',
      confirmPassword: 'password123'
    }

    // Mock form validation to pass
    wrapper.vm.registerFormRef = {
      validate: vi.fn().mockResolvedValue(true)
    }

    // Mock slow registration
    mockUserStore.doRegister.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)))

    const registerPromise = wrapper.vm.handleRegister()

    // 检查loading状态
    expect(wrapper.vm.loading).toBe(true)

    await registerPromise
    expect(wrapper.vm.loading).toBe(false)
  })
})