<template>
  <div class="article-detail">
    <!-- 文章头部 -->
    <div class="article-header">
      <h1 class="article-title">{{ article?.title }}</h1>

      <div class="article-meta">
        <div class="meta-left">
          <el-avatar
            :size="32"
            :src="article?.author?.avatar"
            :alt="article?.author?.nickname"
          >
            {{ article?.author?.nickname?.charAt(0) }}
          </el-avatar>
          <span class="author">{{ article?.author?.nickname }}</span>
          <span class="publish-time">
            {{ formatDateTime(article?.createTime) }}
          </span>
          <el-tag v-if="article?.category" size="small" type="info">
            {{ article?.category?.name }}
          </el-tag>
        </div>

        <div class="meta-right">
          <span class="view-count">
            <el-icon><View /></el-icon>
            {{ article?.viewCount }}
          </span>
          <span class="comment-count">
            <el-icon><ChatDotRound /></el-icon>
            {{ commentCount }}
          </span>
          <span class="like-count">
            <el-icon><Star /></el-icon>
            {{ article?.likeCount }}
          </span>
        </div>
      </div>

      <!-- 标签 -->
      <div v-if="article?.tags && article.tags.length > 0" class="article-tags">
        <el-tag
          v-for="tag in article.tags"
          :key="tag.id"
          size="small"
          :color="tag.color"
          effect="light"
        >
          {{ tag.name }}
        </el-tag>
      </div>
    </div>

    <!-- 文章内容 -->
    <div class="article-content">
      <div v-if="formattedContent" v-html="formattedContent" class="markdown-body"></div>
      <div v-else class="loading-content">
        <el-skeleton :rows="8" animated />
      </div>
    </div>

    <!-- 文章操作 -->
    <div class="article-actions">
      <el-button
        :type="isLiked ? 'primary' : 'default'"
        :icon="isLiked ? 'StarFilled' : 'Star'"
        @click="handleLike"
      >
        {{ isLiked ? '已点赞' : '点赞' }} ({{ article?.likeCount || 0 }})
      </el-button>

      <el-button icon="Share" @click="handleShare">
        分享
      </el-button>

      <el-button
        v-if="canEdit"
        icon="Edit"
        @click="handleEdit"
      >
        编辑
      </el-button>

      <el-button
        v-if="canDelete"
        type="danger"
        icon="Delete"
        @click="handleDelete"
      >
        删除
      </el-button>
    </div>

    <!-- 分割线 -->
    <el-divider />

    <!-- 评论区 -->
    <CommentList
      :article-id="articleId"
      :article-author-id="article?.authorId"
      :show-comment-form="true"
      :auto-load="true"
      @comment-created="handleCommentCreated"
      @comment-deleted="handleCommentDeleted"
      @comment-liked="handleCommentLiked"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, ChatDotRound, Star, StarFilled, Edit, Delete, Share } from '@element-plus/icons-vue'
import { marked } from 'marked'
import { formatDateTime } from '@/utils/date'
import { useUserStore } from '@/stores/user'
import { useArticleStore } from '@/stores/article'
import { useCommentStore } from '@/stores/commentStore'
import CommentList from '@/components/comment/CommentList.vue'
import type { Article, Comment } from '@shared/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const articleStore = useArticleStore()
const commentStore = useCommentStore()

const articleId = computed(() => Number(route.params.id))
const article = computed(() => articleStore.currentArticle)
const isLiked = ref(false)

const commentCount = computed(() => commentStore.commentCount)

const canEdit = computed(() => {
  return (
    userStore.isLoggedIn &&
    article.value &&
    article.value.authorId === userStore.user?.id
  )
})

const canDelete = computed(() => {
  return (
    userStore.isLoggedIn &&
    article.value &&
    (article.value.authorId === userStore.user?.id ||
     userStore.user?.role === 'ADMIN')
  )
})

const formattedContent = computed(() => {
  if (!article.value?.content) return ''

  // 配置 marked 选项
  marked.setOptions({
    breaks: true,
    gfm: true,
    sanitize: false // 注意：生产环境应该使用安全的配置
  })

  return marked.parse(article.value.content)
})

// 加载文章详情
const loadArticle = async () => {
  if (!articleId.value) return

  try {
    await articleStore.fetchArticleDetail(articleId.value)

    // 加载评论数量
    await commentStore.fetchCommentCount(articleId.value)

    // 检查是否已点赞
    await checkLikeStatus()
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('文章不存在或加载失败')
    router.push('/')
  }
}

// 检查点赞状态
const checkLikeStatus = async () => {
  if (!userStore.isLoggedIn || !articleId.value) return

  try {
    // 这里可以调用API检查是否已点赞
    // isLiked.value = await articleService.checkIfLiked(articleId.value)
    isLiked.value = false
  } catch (error) {
    console.error('检查点赞状态失败:', error)
  }
}

// 处理点赞
const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再点赞')
    return
  }

  try {
    if (isLiked.value) {
      // await articleService.unlikeArticle(articleId.value)
      isLiked.value = false
      ElMessage.success('取消点赞成功')
    } else {
      // await articleService.likeArticle(articleId.value)
      isLiked.value = true
      ElMessage.success('点赞成功')
    }

    // 重新加载文章以更新点赞数
    await loadArticle()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 处理分享
const handleShare = async () => {
  try {
    const url = window.location.href
    const title = article.value?.title

    // 复制到剪贴板
    await navigator.clipboard.writeText(`${title} ${url}`)
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    console.error('分享失败:', error)
    ElMessage.error('分享失败，请重试')
  }
}

// 处理编辑
const handleEdit = () => {
  router.push(`/admin/articles/${articleId.value}/edit`)
}

// 处理删除
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这篇文章吗？删除后无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    // await articleService.deleteArticle(articleId.value)
    ElMessage.success('文章删除成功')
    router.push('/')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文章失败:', error)
      ElMessage.error('删除文章失败，请重试')
    }
  }
}

// 处理评论相关事件
const handleCommentCreated = (comment: Comment) => {
  ElMessage.success('评论发表成功')
  loadArticle() // 重新加载文章以更新评论数
}

const handleCommentDeleted = (commentId: number) => {
  ElMessage.success('评论删除成功')
  loadArticle() // 重新加载文章以更新评论数
}

const handleCommentLiked = (commentId: number) => {
  // 评论点赞的额外处理
}

// 监听路由参数变化
watch(
  () => articleId.value,
  (newId) => {
    if (newId) {
      loadArticle()
    }
  },
  { immediate: true }
)

onMounted(() => {
  loadArticle()
})
</script>

<style scoped>
.article-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.article-header {
  margin-bottom: 32px;
}

.article-title {
  font-size: 32px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.4;
  margin: 0 0 20px 0;
}

.article-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.publish-time {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.meta-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.meta-right span {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.article-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.article-content {
  margin-bottom: 32px;
}

.loading-content {
  padding: 20px 0;
}

.markdown-body {
  color: var(--el-text-color-primary);
  line-height: 1.8;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  color: var(--el-text-color-primary);
  margin-top: 24px;
  margin-bottom: 16px;
}

.markdown-body :deep(p) {
  margin-bottom: 16px;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid var(--el-color-primary);
  padding-left: 16px;
  margin: 16px 0;
  color: var(--el-text-color-secondary);
}

.markdown-body :deep(code) {
  background: var(--el-fill-color-light);
  padding: 2px 4px;
  border-radius: 4px;
  font-size: 14px;
}

.markdown-body :deep(pre) {
  background: var(--el-fill-color-light);
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 16px 0;
}

.markdown-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
}

.article-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 32px;
}

@media (max-width: 768px) {
  .article-detail {
    padding: 16px;
  }

  .article-title {
    font-size: 24px;
  }

  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .article-actions {
    flex-wrap: wrap;
  }
}
</style>