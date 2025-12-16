<template>
  <div class="pinned-articles-view">
    <div class="view-header">
      <h3>置顶文章</h3>
      <el-alert
        type="success"
        show-icon
        :closable="false"
      >
        置顶的文章将在列表中显示在顶部位置，可以随时取消置顶。
      </el-alert>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="pinnedArticles.length === 0" class="empty-state">
      <el-empty description="没有置顶的文章">
        <el-button type="primary" @click="$router.push('/articles')">
          去置顶文章
        </el-button>
      </el-empty>
    </div>

    <div v-else class="articles-container">
      <div class="articles-grid">
        <div
          v-for="(article, index) in pinnedArticles"
          :key="article.id"
          class="pinned-article-card"
          :class="{ 'first-pinned': index === 0 }"
        >
          <div class="pin-indicator">
            <el-icon v-if="index === 0" class="crown-icon"><Crown /></el-icon>
            <el-icon v-else class="pin-icon"><Top /></el-icon>
          </div>

          <div class="article-content">
            <div class="article-header">
              <div class="article-title-wrapper">
                <h4 class="article-title">{{ article.title }}</h4>
                <el-tag
                  type="warning"
                  size="small"
                  class="pin-tag"
                >
                  <el-icon><Top /></el-icon>
                  置顶 {{ index + 1 }}
                </el-tag>
              </div>

              <div class="article-actions">
                <el-button
                  type="text"
                  size="small"
                  @click="handleView(article)"
                >
                  <el-icon><View /></el-icon>
                  查看
                </el-button>
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
                  @click="handleUnpin(article)"
                >
                  <el-icon><Remove /></el-icon>
                  取消置顶
                </el-button>
              </div>
            </div>

            <div class="article-summary" v-if="article.summary">
              {{ article.summary }}
            </div>

            <div class="article-stats">
              <div class="stat-item">
                <el-icon><View /></el-icon>
                <span>{{ article.viewCount }}</span>
              </div>
              <div class="stat-item">
                <el-icon><Star /></el-icon>
                <span>{{ article.likeCount }}</span>
              </div>
              <div class="stat-item">
                <el-icon><ChatLineSquare /></el-icon>
                <span>{{ article.commentCount }}</span>
              </div>
            </div>

            <div class="article-meta">
              <span>发布时间：{{ formatTime(article.publishTime as string) }}</span>
              <span>更新时间：{{ formatTime(article.updateTime) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElSkeleton, ElEmpty, ElButton, ElTag, ElAlert, ElIcon } from 'element-plus'
import { Crown, Top, View, Edit, Remove, Star, ChatLineSquare } from '@element-plus/icons-vue'
import type { Article } from '@/types/article'
import { getPinnedArticles, unpinArticle } from '@/api/article'

const emit = defineEmits<{
  unpin: [articleId: number]
}>()

const loading = ref(false)
const pinnedArticles = ref<Article[]>([])

async function loadPinnedArticles() {
  try {
    loading.value = true
    pinnedArticles.value = await getPinnedArticles()
  } catch (error) {
    console.error('加载置顶文章失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleUnpin(article: Article) {
  try {
    const result = await unpinArticle(article.id)
    if (result.operationResult === 'success') {
      emit('unpin', article.id)
      // 从列表中移除
      const index = pinnedArticles.value.findIndex(a => a.id === article.id)
      if (index !== -1) {
        pinnedArticles.value.splice(index, 1)
      }
    }
  } catch (error) {
    console.error('取消置顶失败:', error)
  }
}

function handleView(article: Article) {
  // 跳转到文章详情页
  console.log('查看文章:', article.id)
  // $router.push(`/articles/${article.id}`)
}

function handleEdit(article: Article) {
  // 跳转到编辑页面
  console.log('编辑文章:', article.id)
  // $router.push(`/articles/edit/${article.id}`)
}

function formatTime(time: string): string {
  return new Date(time).toLocaleDateString()
}

onMounted(() => {
  loadPinnedArticles()
})
</script>

<style scoped>
.pinned-articles-view {
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

.articles-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.pinned-article-card {
  display: flex;
  background: white;
  border: 2px solid #fbbf24;
  border-radius: 12px;
  padding: 0;
  overflow: hidden;
  transition: all 0.2s ease;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.pinned-article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.15);
}

.pinned-article-card.first-pinned {
  border-color: #dc2626;
  background: linear-gradient(135deg, #fef3c7 0%, #fef9e7 100%);
}

.pin-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  background: #fbbf24;
  flex-shrink: 0;
}

.first-pinned .pin-indicator {
  background: #dc2626;
}

.crown-icon {
  font-size: 24px;
  color: #dc2626;
}

.pin-icon {
  font-size: 20px;
  color: white;
}

.article-content {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 4px;
}

.article-title-wrapper {
  flex: 1;
  margin-right: 12px;
}

.article-title {
  margin: 0 0 6px 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  line-height: 1.4;
}

.pin-tag {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.article-actions .el-button {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
}

.article-summary {
  color: #6b7280;
  font-size: 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6b7280;
  font-size: 14px;
}

.article-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #9ca3af;
  border-top: 1px solid #f3f4f6;
  padding-top: 12px;
  margin-top: auto;
}

@media (max-width: 768px) {
  .articles-grid {
    grid-template-columns: 1fr;
  }

  .pinned-article-card {
    flex-direction: column;
  }

  .pin-indicator {
    width: 100%;
    height: 40px;
  }

  .article-header {
    flex-direction: column;
    gap: 12px;
  }

  .article-title-wrapper {
    margin-right: 0;
  }

  .article-actions {
    align-self: flex-start;
  }

  .article-stats {
    flex-wrap: wrap;
  }
}
</style>