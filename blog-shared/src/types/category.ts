/**
 * 分类相关类型定义
 */

export interface Category {
  id: number
  name: string
  description?: string
  icon?: string
  parentId?: number | null
  sortOrder: number
  articleCount: number
  createTime: string
  updateTime: string
  children?: Category[]
}

export interface CategoryTreeItem extends Category {
  children: CategoryTreeItem[]
}

export interface CreateCategoryRequest {
  name: string
  description?: string
  icon?: string
  parentId?: number | null
  sortOrder?: number
}

export interface UpdateCategoryRequest {
  name: string
  description?: string
  icon?: string
  parentId?: number | null
  sortOrder?: number
}