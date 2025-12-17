// Search related types
export interface SearchQuery {
  q: string
  fields?: ('title' | 'content' | 'author' | 'tags')[]
  categoryId?: number
  tagIds?: number[]
  startDate?: string
  endDate?: string
  sortBy?: 'relevance' | 'createTime' | 'viewCount'
}

export interface SearchResult {
  results: SearchResultItem[]
  pagination: {
    page: number
    size: number
    total: number
    totalPages: number
  }
  searchMeta: {
    keyword: string
    searchTime: number
    resultCount: number
  }
}

export interface SearchResultItem {
  id: number
  title: string
  summary?: string
  author?: {
    id: number
    nickname?: string
    avatar?: string
  }
  category?: {
    id: number
    name: string
  }
  tags?: {
    id: number
    name: string
    color?: string
  }[]
  viewCount: number
  createTime: string
  highlights?: {
    title?: string[]
    content?: string[]
  }
}

export interface SearchHistory {
  id: number
  userId: number
  keyword: string
  resultCount: number
  searchTime: string
}

export interface SearchStats {
  id: number
  keyword: string
  searchCount: number
  avgResultCount: number
  lastSearchTime: string
}

export interface HotKeywords {
  id: number
  keyword: string
  searchCount: number
  position: number
  updateTime: string
}