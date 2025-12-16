<template>
  <div class="deleted-articles-view">
    <div class="view-header">
      <h3>已删除的文章</h3>
      <el-alert
        type="info"
        show-icon
        :closable="false"
      >
        已删除的文章可以在这里恢复，恢复后文章将变为草稿状态。
      </el-alert>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="deletedArticles.length === 0" class="empty-state">
      <el-empty description="没有已删除的文章">
        <el-button type="primary" @click="$router.push('/articles/create')">
          创建文章
        </el-button>
      </el-empty>
    </div>

    <div v-else class="articles-grid">
      <div
        v-for="article in deletedArticles"
        :key="article.id"
        class="article-card"
      >
        <div class="card-header">
          <h4 class="article-title">{{ article.title }}</h4>
          <el-tag type="danger" size="small">已删除</el-tag>
        </div>

        <div class="card-content">
          <p class="article-summary">{{ article.summary || '无摘要' }}</p>
          <div class="article-meta">
            <span>删除时间：{{ formatTime(article.deletedAt as string) }}</span>
            <span>原状态：{{ getOriginalStatus(article.status) }}</span>
          </div>
        </div>

        <div class="card-actions">
          <el-button
            type="primary"
            size="small"
            @click="handleRestore(article)"
          >
            <el-icon><RefreshLeft /></el-icon>
            恢复文章
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="handlePermanentDelete(article)"
          >
            <el-icon><Delete /></el-icon>
            永久删除
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElSkeleton, ElEmpty, ElButton, ElTag, ElAlert, ElIcon, ElMessageBox } from 'element-plus'
import { RefreshLeft, Delete } from '@element-plus/icons-vue'
import type { Article } from '@/types/article'
import { getDeletedArticles, restoreArticle } from '@/api/article'

const emit = defineEmits<{
  restore: [articleId: number]
}>()

const loading = ref(false)
const deletedArticles = ref<Article[]>([])

async function loadDeletedArticles() {
  try {
    loading.value = true
    deletedArticles.value = await getDeletedArticles()
  } catch (error) {
    console.error('加载已删除文章失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleRestore(article: Article) {
  try {
    await ElMessageBox.confirm(`确定要恢复文章"${article.title}"吗？`, '确认恢复', {
      type: 'warning'
    })

    const result = await restoreArticle(article.id)
    if (result.operationResult === 'success') {
      emit('restore', article.id)
      // 从列表中移除
      const index = deletedArticles.value.findIndex(a => a.id === article.id)
      if (index !== -1) {
        deletedArticles.value.splice(index, 1)
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('恢复文章失败:', error)
    }
  }
}

async function handlePermanentDelete(article: Article) {
  try {
    await ElMessageBox.confirm(
      `确定要永久删除文章"${article.title}"吗？此操作不可撤销！`,
      '永久删除',
      {
        type: 'error',
        confirmButtonText: '确定删除',
        confirmButtonClass: 'el-button--danger'
      }
    )

    // 这里应该调用永久删除API
    console.log('永久删除:', article.id)
    // await permanentDeleteArticle(article.id)

    // 从列表中移除
    const index = deletedArticles.value.findIndex(a => a.id === article.id)
    if (index !== -1) {
      deletedArticles.value.splice(index, 1)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('永久删除失败:', error)
    }
  }
}

function formatTime(time: string): string {
  return new Date(time).toLocaleString()
}

function getOriginalStatus(status: string): string {
  // 这里应该从某个地方获取原始状态，暂时返回默认值
  return '未知'
}

onMounted(() => {
  loadDeletedArticles()
})
</script>

<style scoped>
.deleted-articles-view {
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

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.article-card {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.article-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
  flex: 1;
  margin-right: 8px;
}

.card-content {
  margin-bottom: 16px;
}

.article-summary {
  margin: 0 0 12px 0;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.card-actions .el-button {
  display: flex;
  align-items: center;
  gap: 4px;
}

@media (max-width: 768px) {
  .articles-grid {
    grid-template-columns: 1fr;
  }
}
</style>