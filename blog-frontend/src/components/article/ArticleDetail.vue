<template>
  <div class="article-detail">
    <!-- 文章头部 -->
    <header class="mb-8">
      <!-- 封面图 -->
      <div v-if="article.coverImage" class="mb-8">
        <img
          :src="article.coverImage"
          :alt="article.title"
          class="w-full h-64 md:h-96 object-cover rounded-lg shadow-lg"
        />
      </div>

      <!-- 标题区域 -->
      <div class="mb-6">
        <!-- 状态标签 -->
        <div class="flex items-center gap-2 mb-4">
          <span
            v-if="article.isTop"
            class="px-3 py-1 bg-red-500 text-white text-sm rounded-full"
          >
            <i class="fas fa-star mr-1"></i>置顶
          </span>
          <span
            :class="[
              'px-3 py-1 text-sm rounded-full',
              article.status === 'PUBLISHED'
                ? 'bg-green-100 text-green-600'
                : 'bg-yellow-100 text-yellow-600'
            ]"
          >
            {{ article.status === 'PUBLISHED' ? '已发布' : '草稿' }}
          </span>
        </div>

        <h1 class="text-3xl md:text-4xl font-bold mb-4 text-gray-900">
          {{ article.title }}
        </h1>

        <!-- 摘要 -->
        <p v-if="article.summary" class="text-lg text-gray-600 mb-6">
          {{ article.summary }}
        </p>

        <!-- 元信息 -->
        <div class="flex flex-wrap items-center gap-6 text-gray-500">
          <!-- 作者信息 -->
          <div class="flex items-center gap-3">
            <img
              :src="author.avatar || '/default-avatar.png'"
              :alt="author.nickname"
              class="w-10 h-10 rounded-full"
            />
            <div>
              <div class="font-medium text-gray-900">{{ author.nickname }}</div>
              <div class="text-sm">{{ formatDate(article.publishTime) }}</div>
            </div>
          </div>

          <!-- 分类 -->
          <div v-if="category.name" class="flex items-center gap-2">
            <i class="fas fa-folder"></i>
            <span>{{ category.name }}</span>
          </div>

          <!-- 统计信息 -->
          <div class="flex items-center gap-4">
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

      <!-- 操作按钮 -->
      <div class="flex items-center justify-between border-t border-b py-4">
        <div class="flex items-center gap-3">
          <!-- 点赞按钮 -->
          <button
            @click="handleLike"
            :disabled="liking"
            :class="[
              'px-4 py-2 rounded-lg font-medium transition-colors',
              isLiked
                ? 'bg-red-500 text-white hover:bg-red-600'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
          >
            <i :class="isLiked ? 'fas fa-heart' : 'far fa-heart'"></i>
            <span class="ml-2">{{ article.likeCount }}</span>
          </button>

          <!-- 收藏按钮 -->
          <button
            @click="handleBookmark"
            :class="[
              'px-4 py-2 rounded-lg font-medium transition-colors',
              isBookmarked
                ? 'bg-yellow-500 text-white hover:bg-yellow-600'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
          >
            <i :class="isBookmarked ? 'fas fa-bookmark' : 'far fa-bookmark'"></i>
            <span class="ml-2">收藏</span>
          </button>
        </div>

        <!-- 分享按钮 -->
        <ShareButtons
          :title="article.title"
          :url="shareUrl"
          :description="article.summary"
        />
      </div>
    </header>

    <!-- 文章内容 -->
    <main class="grid grid-cols-1 lg:grid-cols-4 gap-8">
      <!-- 主体内容 -->
      <div class="lg:col-span-3">
        <!-- 标签 -->
        <div v-if="tags.length > 0" class="mb-6 flex flex-wrap gap-2">
          <span
            v-for="tag in tags"
            :key="tag.id"
            :style="{ backgroundColor: tag.color + '20', color: tag.color }"
            class="px-3 py-1 rounded-full text-sm"
          >
            {{ tag.name }}
          </span>
        </div>

        <!-- 文章正文 -->
        <article
          class="prose prose-lg max-w-none article-content"
          v-html="renderedContent"
        ></article>
      </div>

      <!-- 侧边栏 -->
      <aside class="lg:col-span-1">
        <!-- 目录 -->
        <TableOfContents
          v-if="tableOfContents.length > 0"
          :headings="tableOfContents"
          class="mb-8"
        />

        <!-- 作者信息卡片 -->
        <AuthorCard
          :author="author"
          class="mb-8"
        />

        <!-- 相关文章 -->
        <RelatedArticles
          :article-id="article.id"
          :category-id="article.categoryId"
          class="mb-8"
        />
      </aside>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import type {
  Article,
  UserSummary,
  CategorySummary,
  TagSummary,
  ArticleDetailResponse
} from '@/types/article'
import ShareButtons from '@/components/common/ShareButtons.vue'
import TableOfContents from './TableOfContents.vue'
import AuthorCard from './AuthorCard.vue'
import RelatedArticles from './RelatedArticles.vue'

interface Props {
  article: Article
  author: UserSummary
  category: CategorySummary
  tags: TagSummary[]
  isLiked?: boolean
  canEdit?: boolean
}

interface Emits {
  (e: 'like', articleId: number): void
  (e: 'bookmark', articleId: number): void
}

const props = withDefaults(defineProps<Props>(), {
  isLiked: false,
  canEdit: false
})

const emit = defineEmits<Emits>()

const route = useRoute()
const liking = ref(false)
const isBookmarked = ref(false)

// 渲染后的内容（Markdown转HTML）
const renderedContent = computed(() => {
  // 这里应该集成Markdown渲染器
  // 暂时返回原文，实际项目中需要使用marked、markdown-it等库
  return props.article.content
})

// 分享URL
const shareUrl = computed(() => {
  return `${window.location.origin}${route.fullPath}`
})

// 目录
const tableOfContents = computed(() => {
  // 从文章内容中提取标题生成目录
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = renderedContent.value

  const headings = Array.from(tempDiv.querySelectorAll('h1, h2, h3, h4, h5, h6')).map((heading, index) => ({
    id: `heading-${index}`,
    text: heading.textContent || '',
    level: parseInt(heading.tagName.substring(1)),
    element: heading
  }))

  return headings
})

// 处理点赞
const handleLike = async () => {
  if (liking.value) return

  liking.value = true
  try {
    emit('like', props.article.id)
  } finally {
    liking.value = false
  }
}

// 处理收藏
const handleBookmark = () => {
  isBookmarked.value = !isBookmarked.value
  emit('bookmark', props.article.id)
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 初始化
onMounted(() => {
  // 为标题添加id以便目录跳转
  setTimeout(() => {
    const content = document.querySelector('.article-content')
    if (content) {
      const headings = content.querySelectorAll('h1, h2, h3, h4, h5, h6')
      headings.forEach((heading, index) => {
        heading.id = `heading-${index}`
      })
    }
  }, 100)
})
</script>

<style scoped>
.article-content {
  @apply leading-relaxed;
}

.article-content :deep(h1) {
  @apply text-2xl font-bold mb-6 mt-8;
}

.article-content :deep(h2) {
  @apply text-xl font-bold mb-4 mt-6;
}

.article-content :deep(h3) {
  @apply text-lg font-bold mb-3 mt-4;
}

.article-content :deep(p) {
  @apply mb-4;
}

.article-content :deep(img) {
  @apply rounded-lg shadow-md my-6;
}

.article-content :deep(blockquote) {
  @apply border-l-4 border-blue-500 pl-4 italic my-6 bg-gray-50 py-2;
}

.article-content :deep(pre) {
  @apply bg-gray-900 text-gray-100 p-4 rounded-lg overflow-x-auto my-6;
}

.article-content :deep(code) {
  @apply bg-gray-100 px-2 py-1 rounded text-sm;
}

.article-content :deep(pre code) {
  @apply bg-transparent p-0;
}

.article-content :deep(ul) {
  @apply list-disc list-inside mb-4;
}

.article-content :deep(ol) {
  @apply list-decimal list-inside mb-4;
}

.article-content :deep(a) {
  @apply text-blue-500 hover:text-blue-700 underline;
}

.article-content :deep(table) {
  @apply w-full border-collapse border border-gray-300 my-6;
}

.article-content :deep(th) {
  @apply border border-gray-300 bg-gray-100 px-4 py-2 text-left;
}

.article-content :deep(td) {
  @apply border border-gray-300 px-4 py-2;
}
</style>