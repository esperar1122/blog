<template>
  <div class="comment-tree">
    <!-- 评论统计 -->
    <div class="comment-header">
      <h3 class="comment-title">
        <el-icon><ChatDotRound /></el-icon>
        评论 ({{ commentCount }})
      </h3>

      <!-- 排序选项 -->
      <div v-if="comments.length > 0" class="comment-sort">
        <el-radio-group v-model="sortBy" size="small" @change="handleSortChange">
          <el-radio-button label="createTime">最新</el-radio-button>
          <el-radio-button label="likeCount">最热</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 评论表单 -->
    <div v-if="showCommentForm" class="comment-form-section">
      <CommentForm
        :article-id="articleId"
        @success="handleCommentSuccess"
      />
    </div>

    <!-- 评论列表 -->
    <div v-loading="loading" class="comment-list">
      <!-- 无评论状态 -->
      <el-empty
        v-if="!loading && comments.length === 0"
        description="暂无评论，来发表第一条评论吧！"
        :image-size="120"
      >
        <template #image>
          <el-icon size="120" color="var(--el-color-info-light-5)">
            <ChatDotRound />
          </el-icon>
        </template>
      </el-empty>

      <!-- 评论项列表 -->
      <template v-else>
        <CommentItem
          v-for="comment in comments"
          :key="comment.id"
          :comment="comment"
          :level="comment.level"
          :current-user-id="currentUserId"
          :article-author-id="articleAuthorId"
          @reply="handleReply"
        />
      </template>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore && !loading" class="load-more">
      <el-button
        type="primary"
        link
        :loading="loadingMore"
        @click="loadMore"
      >
        加载更多评论
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound } from '@element-plus/icons-vue'
import type { Comment } from '@shared/types'
import { useUserStore } from '@/stores/user'
import { useCommentStore } from '@/stores/commentStore'
import CommentForm from './CommentForm.vue'
import CommentItem from './CommentItem.vue'

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

const userStore = useUserStore()
const commentStore = useCommentStore()

const sortBy = ref<'createTime' | 'likeCount'>('createTime')
const sortOrder = ref<'asc' | 'desc'>('desc')
const loading = ref(false)
const loadingMore = ref(false)

const comments = computed(() => commentStore.currentComments)
const commentCount = computed(() => commentStore.commentCount)
const hasMore = computed(() => {
  const { page, totalPages } = commentStore.pagination
  return page < totalPages
})

const currentUserId = computed(() => {
  const user = userStore.user
  return user?.id
})

// 加载评论
const loadComments = async (reset = true) => {
  if (!props.articleId) return

  loading.value = true

  try {
    if (reset) {
      commentStore.clearComments()
    }

    // 使用嵌套评论API获取所有评论
    await commentStore.fetchNestedComments(
      props.articleId,
      sortBy.value,
      sortOrder.value
    )

    // 获取评论数量
    await commentStore.fetchCommentCount(props.articleId)
  } catch (error) {
    console.error('加载评论失败:', error)
    ElMessage.error('加载评论失败，请重试')
  } finally {
    loading.value = false
  }
}

// 加载更多评论（仅用于分页模式）
const loadMore = async () => {
  loadingMore.value = true

  try {
    // 这里可以实现分页加载逻辑
    // 暂时使用重新加载的方式
    await loadComments(false)
  } catch (error) {
    console.error('加载更多评论失败:', error)
    ElMessage.error('加载更多评论失败，请重试')
  } finally {
    loadingMore.value = false
  }
}

// 处理排序变化
const handleSortChange = () => {
  loadComments(true)
}

// 处理评论成功
const handleCommentSuccess = () => {
  // 评论成功后重新加载评论列表
  loadComments(true)
  emit('comment-created', {} as Comment)
}

// 处理回复
const handleReply = (data: { commentId: number; userName: string }) => {
  // 可以在这里实现自动滚动到回复表单等功能
  ElMessage.info(`正在回复 @${data.userName}`)
}

// 刷新评论
const refreshComments = () => {
  loadComments(true)
}

// 监听文章ID变化
watch(
  () => props.articleId,
  (newArticleId) => {
    if (newArticleId && props.autoLoad) {
      loadComments(true)
    }
  },
  { immediate: true }
)

// 暴露方法给父组件
defineExpose({
  refresh: refreshComments,
  loadMore
})
</script>

<style scoped>
.comment-tree {
  padding: 20px;
  background: var(--el-bg-color);
  border-radius: 8px;
}

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.comment-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.comment-sort {
  display: flex;
  align-items: center;
}

.comment-form-section {
  margin-bottom: 24px;
  padding: 16px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
}

.comment-list {
  min-height: 200px;
}

.load-more {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

:deep(.el-empty__description) {
  color: var(--el-text-color-secondary);
  margin-top: 16px;
}

:deep(.el-radio-button__inner) {
  padding: 8px 16px;
}
</style>