import request from './index'
import type {
  Category,
  CategoryTreeItem,
  CreateCategoryRequest,
  UpdateCategoryRequest
} from '@blog/shared/types'

/**
 * 分类相关API
 */

// 获取所有分类列表
export function getAllCategories() {
  return request<Category[]>({
    url: '/categories',
    method: 'GET'
  })
}

// 获取分类树形结构
export function getCategoryTree() {
  return request<CategoryTreeItem[]>({
    url: '/categories/tree',
    method: 'GET'
  })
}

// 根据ID获取分类详情
export function getCategoryById(id: number) {
  return request<Category>({
    url: `/categories/${id}`,
    method: 'GET'
  })
}

// 创建分类
export function createCategory(data: CreateCategoryRequest) {
  return request<Category>({
    url: '/categories',
    method: 'POST',
    data
  })
}

// 更新分类
export function updateCategory(id: number, data: UpdateCategoryRequest) {
  return request<Category>({
    url: `/categories/${id}`,
    method: 'PUT',
    data
  })
}

// 删除分类
export function deleteCategory(id: number) {
  return request<string>({
    url: `/categories/${id}`,
    method: 'DELETE'
  })
}

// 更新分类排序
export function updateCategorySort(categories: Category[]) {
  return request<string>({
    url: '/categories/sort',
    method: 'PUT',
    data: categories
  })
}

// 获取分类下的文章列表
export function getArticlesByCategory(id: number, page = 1, size = 10) {
  return request({
    url: `/categories/${id}/articles`,
    method: 'GET',
    params: { page, size }
  })
}

// 检查分类名称是否存在
export function checkNameExists(name: string, excludeId?: number) {
  return request<boolean>({
    url: '/categories/check/name',
    method: 'GET',
    params: { name, excludeId }
  })
}

// 获取根分类列表
export function getRootCategories() {
  return request<Category[]>({
    url: '/categories/root',
    method: 'GET'
  })
}

// 获取子分类列表
export function getChildCategories(parentId: number) {
  return request<Category[]>({
    url: `/categories/children/${parentId}`,
    method: 'GET'
  })
}