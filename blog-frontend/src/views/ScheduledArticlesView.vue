<template>
  <div class="scheduled-articles-view">
    <div class="view-header">
      <h3>定时发布的文章</h3>
      <el-alert
        type="warning"
        show-icon
        :closable="false"
      >
        定时发布的文章将在指定时间自动发布，你可以随时取消定时设置。
      </el-alert>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="scheduledArticles.length === 0" class="empty-state">
      <el-empty description="没有定时发布的文章">
        <el-button type="primary" @click="$router.push('/articles/create')">
          创建文章并设置定时发布
        </el-button>
      </el-empty>
    </div>

    <div v-else class="articles-timeline">
      <el-timeline>
        <el-timeline-item
          v-for="article in sortedScheduledArticles"
          :key="article.id"
          :timestamp="formatScheduledTime(article.scheduledPublishTime as string)"
          :type="getTimelineType(article.scheduledPublishTime as string)"
          placement="top"
        >
          <el-card class="scheduled-article-card">
            <div class="card-header">
              <div class="article-info">
                <h4 class="article-title">{{ article.title }}</h4>
                <div class="schedule-time">
                  <el-icon><Timer /></el-icon>
                  将于 {{ formatScheduledTime(article.scheduledPublishTime as string) }} 发布
                </div>
              </div>
              <div class="card-actions">
                <el-button
                  type="text"
                  size="small"
                  @click="handleEdit(article)"
                >
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button
                  type="warning"
                  size="small"
                  @click="handleCancelSchedule(article)"
                >
                  <el-icon><Close /></el-icon>
                  取消定时
                </el-button>
              </div>
            </div>

            <div class="article-summary" v-if="article.summary">
              {{ article.summary }}
            </div>

            <div class="article-meta">
              <span>创建时间：{{ formatTime(article.createTime) }}</span>
              <span>状态：{{ getStatusText(article.status) }}</span>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElSkeleton, ElEmpty, ElButton, ElAlert, ElIcon, ElTimeline, ElTimelineItem, ElCard } from 'element-plus'
import { Timer, Edit, Close } from '@element-plus/icons-vue'
import type { Article } from '@/types/article'
import { getScheduledArticles, schedulePublishArticle } from '@/api/article'

const emit = defineEmits<{
  cancel: [articleId: number]
}>()

const loading = ref(false)
const scheduledArticles = ref<Article[]>([])

// 按发布时间排序
const sortedScheduledArticles = computed(() => {
  return [...scheduledArticles.value].sort((a, b) => {
    const timeA = new Date(a.scheduledPublishTime || 0).getTime()
    const timeB = new Date(b.scheduledPublishTime || 0).getTime()
    return timeA - timeB
  })
})

async function loadScheduledArticles() {
  try {
    loading.value = true
    scheduledArticles.value = await getScheduledArticles()
  } catch (error) {
    console.error('加载定时文章失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleCancelSchedule(article: Article) {
  try {
    await ElMessageBox.confirm(`确定要取消文章"${article.title}"的定时发布吗？`, '确认取消', {
      type: 'warning'
    })

    // 设置为过去的时间来取消定时
    const result = await schedulePublishArticle(article.id, {
      articleId: article.id,
      scheduledPublishTime: new Date(0).toISOString()
    })

    if (result.operationResult === 'success') {
      emit('cancel', article.id)
      // 从列表中移除
      const index = scheduledArticles.value.findIndex(a => a.id === article.id)
      if (index !== -1) {
        scheduledArticles.value.splice(index, 1)
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消定时发布失败:', error)
    }
  }
}

function handleEdit(article: Article) {
  // 跳转到编辑页面
  console.log('编辑文章:', article.id)
  // $router.push(`/articles/edit/${article.id}`)
}

function getTimelineType(scheduledTime: string): 'primary' | 'success' | 'warning' | 'danger' | 'info' {
  const now = new Date()
  const scheduled = new Date(scheduledTime)
  const diff = scheduled.getTime() - now.getTime()
  const hours = diff / (1000 * 60 * 60)

  if (hours < 1) return 'danger'
  if (hours < 24) return 'warning'
  if (hours < 72) return 'primary'
  return 'info'
}

function formatScheduledTime(time: string): string {
  const date = new Date(time)
  const now = new Date()
  const diff = date.getTime() - now.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))

  if (days > 0) {
    return `${days}天${hours}小时后`
  } else if (hours > 0) {
    return `${hours}小时后`
  } else {
    return '即将发布'
  }
}

function formatTime(time: string): string {
  return new Date(time).toLocaleString()
}

function getStatusText(status: string): string {
  switch (status) {
    case 'DRAFT':
      return '草稿'
    case 'PUBLISHED':
      return '已发布'
    case 'DELETED':
      return '已删除'
    default:
      return '未知'
  }
}

onMounted(() => {
  loadScheduledArticles()
})
</script>

<style scoped>
.scheduled-articles-view {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.view-header {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.view-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.loading-container {
  padding: 40px;
}

.empty-state {
  padding: 60px;
}

.articles-timeline {
  padding: 20px 0;
}

.scheduled-article-card {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.article-info {
  flex: 1;
  margin-right: 16px;
}

.article-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
}

.schedule-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #f59e0b;
  font-size: 14px;
  font-weight: 500;
}

.card-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.card-actions .el-button {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-summary {
  margin-bottom: 12px;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #9ca3af;
}

:deep(.el-timeline-item__timestamp) {
  font-weight: 500;
}
</style>