<template>
  <div class="related-articles bg-white rounded-lg border p-6">
    <h3 class="text-lg font-semibold mb-4 flex items-center">
      <i class="fas fa-link mr-2 text-blue-500"></i>
      相关文章
    </h3>

    <!-- 加载中 -->
    <div v-if="loading" class="text-center py-4">
      <i class="fas fa-spinner fa-spin text-blue-500"></i>
    </div>

    <!-- 文章列表 -->
    <div v-else-if="relatedArticles.length > 0" class="space-y-4">
      <div
        v-for="article in relatedArticles"
        :key="article.id"
        @click="handleArticleClick(article.id)"
        class="cursor-pointer group"
      >
        <div class="flex gap-3">
          <!-- 缩略图 -->
          <img
            v-if="article.coverImage"
            :src="article.coverImage"
            :alt="article.title"
            class="w-20 h-16 object-cover rounded-lg flex-shrink-0"
          />
          <div
            v-else
            class="w-20 h-16 bg-gray-100 rounded-lg flex-shrink-0 flex items-center justify-center"
          >
            <i class="fas fa-image text-gray-400"></i>
          </div>

          <!-- 文章信息 -->
          <div class="flex-1 min-w-0">
            <h4 class="text-sm font-medium text-gray-900 mb-1 line-clamp-2 group-hover:text-blue-600 transition-colors">
              {{ article.title }}
            </h4>
            <div class="flex items-center gap-3 text-xs text-gray-500">
              <span>{{ formatDate(article.publishTime) }}</span>
              <div class="flex items-center gap-1">
                <i class="fas fa-eye"></i>
                <span>{{ article.viewCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="text-center py-8 text-gray-500">
      <i class="fas fa-inbox text-2xl mb-2"></i>
      <p class="text-sm">暂无相关文章</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getRelatedArticles } from '@/api/article'
import type { ArticleSummary } from '@/types/article'

interface Props {
  articleId: number
  categoryId: number
  limit?: number
}

defineProps<Props>()

const router = useRouter()
const loading = ref(false)
const relatedArticles = ref<ArticleSummary[]>([])

// 获取相关文章
const fetchRelatedArticles = async () => {
  loading.value = true
  try {
    // 这里需要调用获取相关文章的API
    // relatedArticles.value = await getRelatedArticles(props.articleId, props.categoryId)
    // 暂时使用空数组
    relatedArticles.value = []
  } catch (error) {
    console.error('获取相关文章失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理文章点击
const handleArticleClick = (articleId: number) => {
  router.push(`/articles/${articleId}`)
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    return '今天'
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return date.toLocaleDateString('zh-CN', {
      month: 'short',
      day: 'numeric'
    })
  }
}

onMounted(() => {
  fetchRelatedArticles()
})
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>