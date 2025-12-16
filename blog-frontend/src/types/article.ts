/**
 * 文章相关类型定义
 */

export interface ArticleSummary {
  id: number
  title: string
  summary: string
  coverImage: string
  status: string
  viewCount: number
  likeCount: number
  commentCount: number
  isTop: boolean
  authorId: number
  authorName: string
  authorAvatar: string
  categoryId: number
  categoryName: string
  tags: string[]
  createTime: string
  updateTime: string
  publishTime: string
}

export interface UserSummary {
  id: number
  username: string
  nickname: string
  avatar: string
  bio: string
}

export interface CategorySummary {
  id: number
  name: string
  description: string
}

export interface TagSummary {
  id: number
  name: string
  color: string
}

export interface ArticleListResponse {
  content: ArticleSummary[]
  totalPages: number
  totalElements: number
  size: number
  number: number
  first: boolean
  last: boolean
  empty: boolean
}

export interface Article {
  id: number
  title: string
  content: string
  summary: string
  coverImage: string
  status: string
  viewCount: number
  likeCount: number
  commentCount: number
  isTop: boolean
  authorId: number
  categoryId: number
  createTime: string
  updateTime: string
  publishTime: string
  authorName?: string
  authorAvatar?: string
  categoryName?: string
  tags?: Tag[]
}

export interface ArticleDetailResponse {
  article: Article
  author: UserSummary
  category: CategorySummary
  tags: TagSummary[]
  isLiked: boolean
  canEdit: boolean
}

export interface ArticleQueryParams {
  page?: number
  size?: number
  categoryId?: number
  tagId?: number
  keyword?: string
  sortBy?: 'publishTime' | 'viewCount' | 'likeCount' | 'createTime'
  sortDir?: 'asc' | 'desc'
  status?: 'PUBLISHED' | 'DRAFT' | 'ALL'
  authorId?: number
  isTop?: boolean
}

export interface Category {
  id: number
  name: string
  description: string
  articleCount: number
  createTime: string
}

export interface Tag {
  id: number
  name: string
  color: string
  articleCount: number
  createTime: string
}