import type { Directive, DirectiveBinding } from 'vue'
import { usePermission } from '@/composables/usePermission'
import type { UserRole } from '@/types/permission'

/**
 * 权限指令 v-permission
 *
 * 使用方式：
 * v-permission="'ADMIN'" - 需要管理员权限
 * v-permission="['ADMIN', 'USER']" - 需要管理员或用户权限
 * v-permission="{ roles: ['ADMIN'], operator: 'AND' }" - 需要所有指定角色
 * v-permission="{ roles: ['ADMIN'], operator: 'OR' }" - 需要任一指定角色
 */

interface PermissionBinding {
  roles?: UserRole | UserRole[]
  operator?: 'AND' | 'OR'
}

export const permissionDirective: Directive<HTMLElement, UserRole | UserRole[] | PermissionBinding> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<UserRole | UserRole[] | PermissionBinding>) {
    checkPermission(el, binding)
  },
  updated(el: HTMLElement, binding: DirectiveBinding<UserRole | UserRole[] | PermissionBinding>) {
    checkPermission(el, binding)
  }
}

function checkPermission(el: HTMLElement, binding: DirectiveBinding<UserRole | UserRole[] | PermissionBinding>) {
  const { hasRole, hasAnyRole, hasAllRoles } = usePermission()

  let hasPermission = false

  if (!binding.value) {
    hasPermission = true // 没有指定权限要求，默认允许
  } else if (typeof binding.value === 'string') {
    // 单个角色字符串
    hasPermission = hasRole(binding.value)
  } else if (Array.isArray(binding.value)) {
    // 角色数组
    hasPermission = hasAnyRole(binding.value)
  } else if (typeof binding.value === 'object' && binding.value.roles) {
    // 对象形式，包含角色数组和操作符
    const { roles, operator = 'OR' } = binding.value as PermissionBinding

    if (Array.isArray(roles)) {
      if (operator === 'AND') {
        hasPermission = roles.every(role => hasRole(role))
      } else {
        hasPermission = hasAnyRole(roles)
      }
    } else if (typeof roles === 'string') {
      hasPermission = hasRole(roles)
    }
  }

  if (!hasPermission) {
    // 隐藏元素
    el.style.display = 'none'
    // 或者移除元素
    // el.parentNode?.removeChild(el)
  } else {
    // 显示元素
    el.style.display = ''
  }
}

/**
 * 管理员权限指令 v-admin
 * 简化版，专门用于检查管理员权限
 */
export const adminDirective: Directive<HTMLElement> = {
  mounted(el: HTMLElement) {
    checkAdminPermission(el)
  },
  updated(el: HTMLElement) {
    checkAdminPermission(el)
  }
}

function checkAdminPermission(el: HTMLElement) {
  const { isAdmin } = usePermission()

  if (!isAdmin()) {
    el.style.display = 'none'
  } else {
    el.style.display = ''
  }
}

/**
 * 注册权限指令
 */
export function registerPermissionDirectives(app: any) {
  app.directive('permission', permissionDirective)
  app.directive('admin', adminDirective)
}