// User related types
export interface User {
  id: number
  username: string
  email: string
  nickname?: string
  avatar?: string
  role: UserRole
  status: UserStatus
  createdAt: Date
  updatedAt: Date
}

export enum UserRole {
  ADMIN = 'admin',
  USER = 'user'
}

export enum UserStatus {
  ACTIVE = 'active',
  INACTIVE = 'inactive',
  BANNED = 'banned'
}

// Article related types
export interface Article {
  id: number
  title: string
  content: string
  summary?: string
  coverImage?: string
  authorId: number
  author?: User
  categoryId: number
  category?: Category
  tags?: Tag[]
  status: ArticleStatus
  viewCount: number
  createdAt: Date
  updatedAt: Date
}

export enum ArticleStatus {
  DRAFT = 'draft',
  PUBLISHED = 'published',
  ARCHIVED = 'archived'
}

// Category related types
export interface Category {
  id: number
  name: string
  description?: string
  parentId?: number
  parent?: Category
  children?: Category[]
  articleCount: number
  createdAt: Date
  updatedAt: Date
}

// Tag related types
export interface Tag {
  id: number
  name: string
  description?: string
  color?: string
  articleCount: number
  createdAt: Date
  updatedAt: Date
}

// Comment related types
export interface Comment {
  id: number
  content: string
  articleId: number
  userId: number
  user?: User
  userName?: string
  userAvatar?: string
  parentId?: number
  level: number
  likeCount: number
  isEdited: boolean
  editedTime?: Date
  status: CommentStatus
  createTime: Date
  updateTime: Date
  replies?: Comment[]
}

export interface CreateCommentRequest {
  content: string
  articleId: number
  parentId?: number
}

export interface UpdateCommentRequest {
  content: string
}

export interface CommentQuery {
  articleId: number
  page?: number
  size?: number
  sortBy?: 'createTime' | 'likeCount'
  sortOrder?: 'asc' | 'desc'
}

export enum CommentStatus {
  NORMAL = 'NORMAL',
  DELETED = 'DELETED'
}

// API Response types
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  success: boolean
}

export interface PaginationParams {
  page: number
  size: number
  sort?: string
  order?: 'asc' | 'desc'
}

export interface PaginationResult<T> {
  list: T[]
  total: number
  page: number
  size: number
  totalPages: number
}

// Export moderation related types
export * from './moderation'

// Export search related types
export * from './search'