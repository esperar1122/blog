<template>
  <div class="article-list">
    <!-- View Mode Toggle -->
    <div class="flex justify-between items-center mb-6">
      <div class="flex gap-2">
        <button
          @click="viewMode = 'grid'"
          :class="[
            'px-4 py-2 rounded-lg transition-colors',
            viewMode === 'grid'
              ? 'bg-blue-500 text-white'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          ]"
        >
          <i class="fas fa-th-large mr-2"></i>网格视图
        </button>
        <button
          @click="viewMode = 'list'"
          :class="[
            'px-4 py-2 rounded-lg transition-colors',
            viewMode === 'list'
              ? 'bg-blue-500 text-white'
              : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          ]"
        >
          <i class="fas fa-list mr-2"></i>列表视图
        </button>
      </div>

      <!-- Sort Options -->
      <div class="flex items-center gap-4">
        <select
          v-model="sortBy"
          @change="handleSort"
          class="px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="publishTime">发布时间</option>
          <option value="viewCount">浏览量</option>
          <option value="likeCount">点赞数</option>
        </select>

        <button
          @click="sortDir = sortDir === 'desc' ? 'asc' : 'desc'"
          @change="handleSort"
          class="px-4 py-2 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
        >
          <i :class="sortDir === 'desc' ? 'fas fa-sort-amount-down' : 'fas fa-sort-amount-up'"></i>
        </button>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-8">
      <i class="fas fa-spinner fa-spin text-2xl text-blue-500"></i>
      <p class="mt-2 text-gray-500">加载中...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="!loading && articles.length === 0" class="text-center py-12">
      <i class="fas fa-inbox text-4xl text-gray-300 mb-4"></i>
      <p class="text-gray-500">暂无文章</p>
    </div>

    <!-- Grid View -->
    <div
      v-else-if="viewMode === 'grid'"
      class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
    >
      <ArticleCard
        v-for="article in articles"
        :key="article.id"
        :article="article"
        @click="handleArticleClick"
      />
    </div>

    <!-- List View -->
    <div v-else class="space-y-4">
      <div
        v-for="article in articles"
        :key="article.id"
        class="bg-white rounded-lg border p-6 hover:shadow-lg transition-shadow cursor-pointer"
        @click="handleArticleClick(article.id)"
      >
        <div class="flex gap-6">
          <!-- 封面图 -->
          <img
            v-if="article.coverImage"
            :src="article.coverImage"
            :alt="article.title"
            class="w-32 h-24 object-cover rounded-lg flex-shrink-0"
          />
          <div
            v-else
            class="w-32 h-24 bg-gray-100 rounded-lg flex-shrink-0 flex items-center justify-center"
          >
            <i class="fas fa-image text-gray-400"></i>
          </div>

          <!-- 文章信息 -->
          <div class="flex-1">
            <!-- 置顶标签 -->
            <div class="flex items-center gap-2 mb-2">
              <span
                v-if="article.isTop"
                class="px-2 py-1 bg-red-100 text-red-600 text-xs rounded-full"
              >
                置顶
              </span>
              <span
                :class="[
                  'px-2 py-1 text-xs rounded-full',
                  article.status === 'PUBLISHED'
                    ? 'bg-green-100 text-green-600'
                    : 'bg-yellow-100 text-yellow-600'
                ]"
              >
                {{ article.status === 'PUBLISHED' ? '已发布' : '草稿' }}
              </span>
            </div>

            <h3 class="text-lg font-semibold mb-2 line-clamp-1">
              {{ article.title }}
            </h3>
            <p class="text-gray-600 mb-3 line-clamp-2">
              {{ article.summary }}
            </p>

            <!-- 标签 -->
            <div class="flex flex-wrap gap-2 mb-3">
              <span
                v-for="tag in article.tags"
                :key="tag"
                class="px-2 py-1 bg-blue-100 text-blue-600 text-xs rounded"
              >
                {{ tag }}
              </span>
            </div>

            <!-- 元信息 -->
            <div class="flex items-center gap-4 text-sm text-gray-500">
              <div class="flex items-center gap-1">
                <img
                  :src="article.authorAvatar || '/default-avatar.png'"
                  :alt="article.authorName"
                  class="w-5 h-5 rounded-full"
                />
                <span>{{ article.authorName }}</span>
              </div>
              <span>{{ formatDate(article.publishTime) }}</span>
              <div class="flex items-center gap-1">
                <i class="fas fa-eye"></i>
                <span>{{ article.viewCount }}</span>
              </div>
              <div class="flex items-center gap-1">
                <i class="fas fa-heart"></i>
                <span>{{ article.likeCount }}</span>
              </div>
              <div class="flex items-center gap-1">
                <i class="fas fa-comment"></i>
                <span>{{ article.commentCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <Pagination
      v-if="!loading && totalPages > 1"
      :current-page="currentPage"
      :total-pages="totalPages"
      @page-change="handlePageChange"
      class="mt-8"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import type { ArticleSummary } from '@/types/article'
import ArticleCard from './ArticleCard.vue'
import Pagination from '@/components/common/Pagination.vue'

interface Props {
  loading?: boolean
  articles: ArticleSummary[]
  currentPage?: number
  totalPages?: number
  sortBy?: 'publishTime' | 'viewCount' | 'likeCount'
  sortDir?: 'asc' | 'desc'
}

interface Emits {
  (e: 'page-change', page: number): void
  (e: 'sort-change', sortBy: string, sortDir: string): void
  (e: 'article-click', articleId: number): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  articles: () => [],
  currentPage: 1,
  totalPages: 1,
  sortBy: 'publishTime',
  sortDir: 'desc'
})

const emit = defineEmits<Emits>()

const router = useRouter()
const viewMode = ref<'grid' | 'list'>('grid')

const handlePageChange = (page: number) => {
  emit('page-change', page)
}

const handleSort = () => {
  emit('sort-change', props.sortBy, props.sortDir)
}

const handleArticleClick = (articleId: number) => {
  emit('article-click', articleId)
  router.push(`/articles/${articleId}`)
}

const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}
</script>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>