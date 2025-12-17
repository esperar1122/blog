<template>
  <div class="comment-list">
    <CommentTree
      :article-id="articleId"
      :article-author-id="articleAuthorId"
      :show-comment-form="showCommentForm"
      :auto-load="autoLoad"
      @comment-created="handleCommentCreated"
      @comment-deleted="handleCommentDeleted"
      @comment-liked="handleCommentLiked"
    />
  </div>
</template>

<script setup lang="ts">
import type { Comment } from '@shared/types'
import CommentTree from './CommentTree.vue'

interface Props {
  articleId: number
  articleAuthorId?: number
  showCommentForm?: boolean
  autoLoad?: boolean
}

interface Emits {
  (e: 'comment-created', comment: Comment): void
  (e: 'comment-deleted', commentId: number): void
  (e: 'comment-liked', commentId: number): void
}

const props = withDefaults(defineProps<Props>(), {
  showCommentForm: true,
  autoLoad: true
})

const emit = defineEmits<Emits>()

const handleCommentCreated = (comment: Comment) => {
  emit('comment-created', comment)
}

const handleCommentDeleted = (commentId: number) => {
  emit('comment-deleted', commentId)
}

const handleCommentLiked = (commentId: number) => {
  emit('comment-liked', commentId)
}
</script>

<style scoped>
.comment-list {
  width: 100%;
}
</style>