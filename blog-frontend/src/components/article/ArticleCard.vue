<template>
  <div
    class="bg-white rounded-lg border hover:shadow-xl transition-all duration-300 cursor-pointer overflow-hidden group"
    @click="$emit('click', article.id)"
  >
    <!-- 封面图 -->
    <div class="relative h-48 overflow-hidden">
      <img
        v-if="article.coverImage"
        :src="article.coverImage"
        :alt="article.title"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
      />
      <div
        v-else
        class="w-full h-full bg-gradient-to-br from-blue-50 to-purple-50 flex items-center justify-center"
      >
        <i class="fas fa-image text-4xl text-gray-300"></i>
      </div>

      <!-- 状态标签 -->
      <div class="absolute top-3 left-3 flex gap-2">
        <span
          v-if="article.isTop"
          class="px-2 py-1 bg-red-500 text-white text-xs rounded-full shadow-md"
        >
          置顶
        </span>
        <span
          :class="[
            'px-2 py-1 text-xs rounded-full shadow-md',
            article.status === 'PUBLISHED'
              ? 'bg-green-500 text-white'
              : 'bg-yellow-500 text-white'
          ]"
        >
          {{ article.status === 'PUBLISHED' ? '已发布' : '草稿' }}
        </span>
      </div>

      <!-- 渐变遮罩 -->
      <div class="absolute inset-x-0 bottom-0 h-20 bg-gradient-to-t from-black/50 to-transparent"></div>
    </div>

    <!-- 内容区域 -->
    <div class="p-5">
      <!-- 标题 -->
      <h3 class="text-lg font-semibold mb-2 line-clamp-2 group-hover:text-blue-600 transition-colors">
        {{ article.title }}
      </h3>

      <!-- 摘要 -->
      <p class="text-gray-600 text-sm mb-4 line-clamp-3">
        {{ article.summary }}
      </p>

      <!-- 标签 -->
      <div v-if="article.tags && article.tags.length > 0" class="flex flex-wrap gap-1 mb-4">
        <span
          v-for="tag in article.tags.slice(0, 3)"
          :key="tag.id || tag"
          class="px-2 py-1 bg-blue-100 text-blue-600 text-xs rounded-full"
        >
          {{ tag.name || tag }}
        </span>
        <span
          v-if="article.tags.length > 3"
          class="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full"
        >
          +{{ article.tags.length - 3 }}
        </span>
      </div>

      <!-- 底部信息 -->
      <div class="flex items-center justify-between text-xs text-gray-500">
        <!-- 作者信息 -->
        <div class="flex items-center gap-2">
          <img
            :src="article.authorAvatar || article.author?.avatar || '/default-avatar.png'"
            :alt="article.authorName || article.author?.nickname || '匿名'"
            class="w-5 h-5 rounded-full"
          />
          <span class="truncate max-w-20">{{ article.authorName || article.author?.nickname || '匿名' }}</span>
        </div>

        <!-- 统计信息 -->
        <div class="flex items-center gap-3">
          <div class="flex items-center gap-1">
            <i class="fas fa-eye"></i>
            <span>{{ formatNumber(article.viewCount) }}</span>
          </div>
          <div class="flex items-center gap-1">
            <i class="fas fa-heart"></i>
            <span>{{ formatNumber(article.likeCount) }}</span>
          </div>
          <div class="flex items-center gap-1">
            <i class="fas fa-comment"></i>
            <span>{{ formatNumber(article.commentCount) }}</span>
          </div>
        </div>
      </div>

      <!-- 发布时间 -->
      <div class="mt-2 text-xs text-gray-400">
        {{ formatDate(article.publishTime || article.createdAt) }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ArticleSummary } from '@/types/article'

interface Props {
  article: ArticleSummary
}

interface Emits {
  (e: 'click', articleId: number): void
}

defineProps<Props>()
defineEmits<Emits>()

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

const formatNumber = (num: number) => {
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>