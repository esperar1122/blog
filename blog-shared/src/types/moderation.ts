// Comment moderation related types

export interface CommentReport {
  id: number
  commentId: number
  reporterId: number
  reporter?: User
  reason: ReportReason
  description?: string
  status: ReportStatus
  reviewerId?: number
  reviewer?: User
  reviewTime?: Date
  createTime: Date
}

export interface CommentBlacklist {
  id: number
  userId: number
  user?: User
  reason?: string
  blacklistedBy: number
  blacklistedByUser?: User
  createTime: Date
  expireTime?: Date
}

export interface SensitiveWord {
  id: number
  word: string
  replacement?: string
  pattern: string
  type: WordType
  createTime: Date
}

export interface CommentModerationQuery {
  status?: string
  reportStatus?: string
  keyword?: string
  dateRange?: string[]
  page?: number
  size?: number
}

export interface CommentStatistics {
  totalComments: number
  activeComments: number
  deletedComments: number
  editedComments: number
  pendingReports: number
  approvedReports: number
  rejectedReports: number
  totalReports: number
}

// Enums
export enum ReportReason {
  SPAM = 'SPAM',
  INAPPROPRIATE = 'INAPPROPRIATE',
  OFFENSIVE = 'OFFENSIVE',
  OTHER = 'OTHER'
}

export enum ReportStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
}

export enum WordType {
  FILTER = 'FILTER',
  BLOCK = 'BLOCK',
  WARNING = 'WARNING'
}

// Request/Response types
export interface CommentReportRequest {
  commentId: number
  reason: ReportReason
  description?: string
}

export interface CommentModerationRequest {
  status: string
  reviewNote?: string
}

export interface BlacklistRequest {
  userId: number
  reason?: string
  expireTime?: Date
}

export interface SensitiveWordRequest {
  word: string
  replacement?: string
  pattern: string
  type: WordType
}

export interface SensitiveWordFilterResult {
  filteredText: string
  containsBlocked: boolean
  warningWords: string[]
}

export interface BatchModerationRequest {
  commentIds: number[]
  action: string
  reason?: string
}

// Import User type from main index
import type { User } from './index'