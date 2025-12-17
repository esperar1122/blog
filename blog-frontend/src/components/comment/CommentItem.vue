<template>
  <div class="comment-item" :style="{ marginLeft: `${(level - 1) * 20}px` }">
    <div class="comment-avatar">
      <el-avatar
        :size="40"
        :src="comment.userAvatar"
        :alt="comment.userName"
      >
        <el-icon><User /></el-icon>
      </el-avatar>
    </div>

    <div class="comment-content">
      <div class="comment-header">
        <span class="comment-author">{{ comment.userName || '匿名用户' }}</span>
        <span class="comment-time" :title="formatCommentTime(comment.createTime).full">
          {{ formatCommentTime(comment.createTime).relative }}
        </span>
        <el-tag v-if="comment.level > 1" size="small" type="info">
          {{ `L${comment.level}` }}
        </el-tag>
      </div>

      <div class="comment-text" v-text="props.comment.content"></div>

      <div class="comment-actions">
        <el-button
          link
          type="primary"
          size="small"
          :icon="isLiked ? 'ChatDotRound' : 'ChatDotRound'"
          @click="handleLike"
        >
          {{ isLiked ? '已点赞' : '点赞' }} ({{ comment.likeCount || 0 }})
        </el-button>

        <el-button
          v-if="canReply"
          link
          type="primary"
          size="small"
          @click="handleReply"
        >
          回复
        </el-button>

        <el-dropdown
          v-if="canEditOrDelete"
          trigger="click"
          @command="handleCommand"
        >
          <el-button link type="info" size="small">
            <el-icon><MoreFilled /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item
                v-if="canEdit"
                command="edit"
                :icon="Edit"
              >
                编辑
              </el-dropdown-item>
              <el-dropdown-item
                v-if="canDelete"
                command="delete"
                :icon="Delete"
              >
                删除
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>

      <!-- 回复表单 -->
      <div v-if="showReplyForm" class="reply-form">
        <CommentForm
          :article-id="comment.articleId"
          :parent-id="comment.id"
          :placeholder="`回复 @${comment.userName || '匿名用户'}...`"
          submit-text="回复"
          show-cancel
          @success="handleReplySuccess"
          @cancel="handleReplyCancel"
        />
      </div>

      <!-- 编辑表单 -->
      <div v-if="showEditForm" class="edit-form">
        <CommentForm
          :article-id="comment.articleId"
          :initial-content="comment.content"
          submit-text="更新"
          show-cancel
          @success="handleEditSuccess"
          @cancel="handleEditCancel"
        />
      </div>

      <!-- 子评论 -->
      <div v-if="comment.replies && comment.replies.length > 0" class="comment-replies">
        <CommentItem
          v-for="reply in comment.replies"
          :key="reply.id"
          :comment="reply"
          :level="reply.level"
          :current-user-id="currentUserId"
          :article-author-id="articleAuthorId"
          @reply="handleReplyToReply"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Edit, Delete, MoreFilled } from '@element-plus/icons-vue'
import type { Comment } from '@shared/types'
import { formatCommentTime } from '@/utils/timeFormatter'
import { useUserStore } from '@/stores/user'
import { useCommentStore } from '@/stores/commentStore'
import CommentForm from './CommentForm.vue'

interface Props {
  comment: Comment
  level?: number
  currentUserId?: number
  articleAuthorId?: number
}

interface Emits {
  (e: 'reply', data: { commentId: number; userName: string }): void
}

const props = withDefaults(defineProps<Props>(), {
  level: 1
})

const emit = defineEmits<Emits>()

const userStore = useUserStore()
const commentStore = useCommentStore()

const showReplyForm = ref(false)
const showEditForm = ref(false)
const isLiked = ref(false)


const canReply = computed(() => {
  return userStore.isLoggedIn && props.comment.status === 'NORMAL'
})

const canEdit = computed(() => {
  return (
    userStore.isLoggedIn &&
    props.currentUserId &&
    props.comment.userId === props.currentUserId &&
    props.comment.status === 'NORMAL'
  )
})

const canDelete = computed(() => {
  return (
    userStore.isLoggedIn &&
    props.currentUserId &&
    (props.comment.userId === props.currentUserId ||
     props.articleAuthorId === props.currentUserId) &&
    props.comment.status === 'NORMAL'
  )
})

const canEditOrDelete = computed(() => canEdit.value || canDelete.value)

// 方法
const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再点赞')
    return
  }

  try {
    if (isLiked.value) {
      await commentStore.unlikeComment(props.comment.id)
      isLiked.value = false
      ElMessage.success('取消点赞成功')
    } else {
      await commentStore.likeComment(props.comment.id)
      isLiked.value = true
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

const handleReply = () => {
  showReplyForm.value = true
}

const handleReplyToReply = (data: { commentId: number; userName: string }) => {
  emit('reply', data)
}

const handleReplySuccess = () => {
  showReplyForm.value = false
}

const handleReplyCancel = () => {
  showReplyForm.value = false
}

const handleCommand = async (command: 'edit' | 'delete') => {
  if (command === 'edit') {
    showEditForm.value = true
  } else if (command === 'delete') {
    await handleDelete()
  }
}

const handleEditSuccess = () => {
  showEditForm.value = false
}

const handleEditCancel = () => {
  showEditForm.value = false
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条评论吗？删除后无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await commentStore.deleteComment(props.comment.id)
    ElMessage.success('评论删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除评论失败，请重试')
    }
  }
}

// 初始化时检查是否已点赞
const initLikeStatus = async () => {
  if (userStore.isLoggedIn) {
    try {
      // 这里可以调用API检查是否已点赞，暂时设为false
      isLiked.value = false
    } catch (error) {
      console.error('检查点赞状态失败:', error)
    }
  }
}

initLikeStatus()
</script>

<style scoped>
.comment-item {
  display: flex;
  padding: 16px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-avatar {
  margin-right: 12px;
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.comment-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  cursor: help;
}

.comment-text {
  color: var(--el-text-color-primary);
  line-height: 1.6;
  margin-bottom: 12px;
  word-break: break-word;
  white-space: pre-wrap;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.reply-form,
.edit-form {
  margin-top: 16px;
  padding-left: 20px;
  border-left: 2px solid var(--el-border-color-light);
}

.comment-replies {
  margin-top: 16px;
  padding-left: 20px;
  border-left: 2px solid var(--el-border-color-lighter);
}

:deep(.el-button.is-link) {
  padding: 0;
  height: auto;
  line-height: 1.5;
}
</style>