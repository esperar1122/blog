<template>
  <div class="related-articles">
    <el-card class="articles-card" v-if="relatedArticles.length > 0">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><Connection /></el-icon>
            相关推荐
          </span>
          <el-button
            v-if="showRefresh"
            type="text"
            size="small"
            :loading="loading"
            @click="refreshArticles"
          >
            换一批
          </el-button>
        </div>
      </template>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else class="articles-list">
        <div
          v-for="article in relatedArticles"
          :key="article.id"
          class="article-item"
          @click="goToArticle(article.id)"
        >
          <div class="article-content">
            <h4 class="article-title">{{ article.title }}</h4>
            <p class="article-summary">{{ article.summary || '暂无摘要' }}</p>
            <div class="article-meta">
              <span class="author" v-if="article.author">
                <el-avatar :size="16" :src="article.author.avatar">
                  {{ article.author.nickname?.charAt(0) }}
                </el-avatar>
                {{ article.author.nickname }}
              </span>
              <span class="time">{{ formatTime(article.createTime) }}</span>
              <span class="views">
                <el-icon><View /></el-icon>
                {{ article.viewCount }}
              </span>
            </div>
            <div class="article-tags" v-if="article.tags && article.tags.length > 0">
              <el-tag
                v-for="tag in article.tags.slice(0, 3)"
                :key="tag.id"
                :color="tag.color"
                size="small"
                effect="light"
              >
                {{ tag.name }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="!loading && articleId" class="no-related">
        <el-empty description="暂无相关推荐" :image-size="60">
          <template #image>
            <el-icon><Connection /></el-icon>
          </template>
        </el-empty>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Connection, View } from '@element-plus/icons-vue'
import type { SearchResultItem } from 'blog-shared'
import { useSearchStore } from '@/stores/searchStore'

interface Props {
  articleId?: number
  limit?: number
  showRefresh?: boolean
}

interface Emits {
  (e: 'article-select', articleId: number): void
}

const props = withDefaults(defineProps<Props>(), {
  limit: 5,
  showRefresh: true
})

const emit = defineEmits<Emits>()

const router = useRouter()
const searchStore = useSearchStore()
const loading = ref(false)

// 计算属性
const relatedArticles = computed(() => searchStore.relatedArticles)

// 监听文章ID变化
watch(() => props.articleId, (newId) => {
  if (newId) {
    fetchRelatedArticles()
  }
}, { immediate: true })

// 获取相关文章
const fetchRelatedArticles = async () => {
  if (!props.articleId) return

  loading.value = true
  try {
    await searchStore.fetchRelatedArticles(props.articleId!, props.limit)
  } catch (error) {
    console.error('Failed to fetch related articles:', error)
  } finally {
    loading.value = false
  }
}

// 刷新相关文章
const refreshArticles = () => {
  fetchRelatedArticles()
}

// 跳转到文章详情
const goToArticle = (articleId: number) => {
  emit('article-select', articleId)
  router.push(`/article/${articleId}`)
}

// 格式化时间
const formatTime = (timeString: string) => {
  const date = new Date(timeString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minute = 60 * 1000
  const hour = minute * 60
  const day = hour * 24
  const week = day * 7

  if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`
  } else {
    return date.toLocaleDateString()
  }
}
</script>

<style scoped>
.related-articles {
  width: 100%;
}

.articles-card {
  height: fit-content;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.loading {
  padding: 16px;
}

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.article-item {
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid var(--el-border-color-lighter);
}

.article-item:hover {
  background-color: var(--el-fill-color-light);
  border-color: var(--el-color-primary);
  transform: translateY(-1px);
}

.article-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.article-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.article-summary {
  margin: 0;
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.5;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.author {
  display: flex;
  align-items: center;
  gap: 4px;
}

.views {
  display: flex;
  align-items: center;
  gap: 2px;
}

.article-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.no-related {
  padding: 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .article-title {
    font-size: 15px;
  }

  .article-summary {
    font-size: 13px;
  }

  .article-meta {
    flex-wrap: wrap;
    gap: 8px;
  }

  .article-item {
    padding: 10px;
  }
}
</style>