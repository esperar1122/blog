<template>
  <div class="article-detail-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="text-center py-12">
      <i class="fas fa-spinner fa-spin text-4xl text-blue-500"></i>
      <p class="mt-4 text-gray-500">加载中...</p>
    </div>

    <!-- 文章不存在 -->
    <div v-else-if="!article" class="text-center py-12">
      <i class="fas fa-exclamation-circle text-4xl text-gray-400 mb-4"></i>
      <h2 class="text-2xl font-semibold mb-2">文章不存在</h2>
      <p class="text-gray-500 mb-6">您访问的文章可能已被删除或不存在</p>
      <router-link
        to="/"
        class="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
      >
        返回首页
      </router-link>
    </div>

    <!-- 文章内容 -->
    <div v-else class="max-w-4xl mx-auto">
      <!-- 返回按钮 -->
      <button
        @click="$router.go(-1)"
        class="mb-6 flex items-center gap-2 text-gray-600 hover:text-gray-900 transition-colors"
      >
        <i class="fas fa-arrow-left"></i>
        返回
      </button>

      <!-- 文章详情组件 -->
      <ArticleDetail
        :article="article"
        :author="author"
        :category="category"
        :tags="tags"
        :is-liked="isLiked"
        :can-edit="canEdit"
        @like="handleLike"
        @bookmark="handleBookmark"
      />

      <!-- 编辑按钮（仅作者可见） -->
      <div
        v-if="canEdit"
        class="fixed right-8 bottom-8 flex flex-col gap-3"
      >
        <router-link
          :to="`/articles/${article.id}/edit`"
          class="w-14 h-14 bg-blue-500 text-white rounded-full shadow-lg hover:bg-blue-600 transition-colors flex items-center justify-center"
          title="编辑文章"
        >
          <i class="fas fa-edit"></i>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticle, likeArticle as likeArticleApi } from '@/api/article'
import ArticleDetail from '@/components/article/ArticleDetail.vue'
import type {
  Article,
  UserSummary,
  CategorySummary,
  TagSummary
} from '@/types/article'

const route = useRoute()

const loading = ref(false)
const article = ref<Article>()
const author = ref<UserSummary>({
  id: 0,
  username: '',
  nickname: '',
  avatar: '',
  bio: ''
})
const category = ref<CategorySummary>({
  id: 0,
  name: '',
  description: ''
})
const tags = ref<TagSummary[]>([])
const isLiked = ref(false)
const canEdit = ref(false)

// 获取文章详情
const fetchArticleDetail = async () => {
  loading.value = true
  try {
    const response = await getArticle(Number(route.params.id))
    article.value = response.article
    author.value = response.author
    category.value = response.category
    tags.value = response.tags
    isLiked.value = response.isLiked
    canEdit.value = response.canEdit

    // 更新页面标题
    if (article.value.title) {
      document.title = `${article.value.title} - 博客`
    }
  } catch (error) {
    console.error('获取文章详情失败:', error)
    article.value = undefined
  } finally {
    loading.value = false
  }
}

// 处理点赞
const handleLike = async (articleId: number) => {
  try {
    await likeArticleApi(articleId)
    isLiked.value = !isLiked.value
    if (article.value) {
      article.value.likeCount += isLiked.value ? 1 : -1
    }
  } catch (error) {
    console.error('点赞失败:', error)
  }
}

// 处理收藏
const handleBookmark = (articleId: number) => {
  // 这里需要实现收藏功能
  console.log('收藏文章:', articleId)
}

// 初始化
onMounted(() => {
  fetchArticleDetail()
})
</script>