// API Status Codes
export const API_CODE = {
  SUCCESS: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  INTERNAL_SERVER_ERROR: 500
} as const

// API Messages
export const API_MESSAGE = {
  SUCCESS: '操作成功',
  BAD_REQUEST: '请求参数错误',
  UNAUTHORIZED: '未授权访问',
  FORBIDDEN: '禁止访问',
  NOT_FOUND: '资源不存在',
  INTERNAL_SERVER_ERROR: '服务器内部错误',
  USER_NOT_FOUND: '用户不存在',
  USER_DISABLED: '用户已被禁用',
  INVALID_CREDENTIALS: '用户名或密码错误',
  TOKEN_EXPIRED: 'Token已过期',
  TOKEN_INVALID: 'Token无效',
  ARTICLE_NOT_FOUND: '文章不存在',
  CATEGORY_NOT_FOUND: '分类不存在',
  COMMENT_NOT_FOUND: '评论不存在'
} as const

// Pagination Defaults
export const PAGINATION = {
  DEFAULT_PAGE: 1,
  DEFAULT_SIZE: 10,
  MAX_SIZE: 100
} as const

// JWT Token Types
export const TOKEN_TYPE = {
  BEARER: 'Bearer'
} as const

// User Roles
export const USER_ROLES = {
  ADMIN: 'admin',
  USER: 'user'
} as const

// Article Status
export const ARTICLE_STATUS = {
  DRAFT: 'draft',
  PUBLISHED: 'published',
  ARCHIVED: 'archived'
} as const

// Comment Status
export const COMMENT_STATUS = {
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected'
} as const