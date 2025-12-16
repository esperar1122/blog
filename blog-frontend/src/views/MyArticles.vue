<template>
  <div class="my-articles-page">
    <!-- 页面头部 -->
    <div class="mb-8">
      <h1 class="text-3xl font-bold mb-2">我的文章</h1>
      <p class="text-gray-600">管理您的所有文章</p>
    </div>

    <!-- 统计信息 -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
      <div class="bg-white p-6 rounded-lg border">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-500 text-sm">总文章数</p>
            <p class="text-2xl font-bold">{{ stats.totalArticles }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-file-alt text-blue-500"></i>
          </div>
        </div>
      </div>

      <div class="bg-white p-6 rounded-lg border">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-500 text-sm">已发布</p>
            <p class="text-2xl font-bold text-green-600">{{ stats.publishedArticles }}</p>
          </div>
          <div class="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-check-circle text-green-500"></i>
          </div>
        </div>
      </div>

      <div class="bg-white p-6 rounded-lg border">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-500 text-sm">草稿</p>
            <p class="text-2xl font-bold text-yellow-600">{{ stats.draftArticles }}</p>
          </div>
          <div class="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-edit text-yellow-500"></i>
          </div>
        </div>
      </div>

      <div class="bg-white p-6 rounded-lg border">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-gray-500 text-sm">总浏览量</p>
            <p class="text-2xl font-bold">{{ formatNumber(stats.totalViews) }}</p>
          </div>
          <div class="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
            <i class="fas fa-eye text-purple-500"></i>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="bg-white rounded-lg border p-6 mb-6">
      <div class="flex flex-col md:flex-row gap-4 items-center justify-between">
        <!-- 左侧操作 -->
        <div class="flex flex-wrap gap-3 items-center">
          <!-- 新建文章按钮 -->
          <router-link
            to="/articles/create"
            class="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors font-medium"
          >
            <i class="fas fa-plus mr-2"></i>新建文章
          </router-link>

          <!-- 批量操作 -->
          <div v-if="selectedArticles.length > 0" class="flex items-center gap-2">
            <span class="text-sm text-gray-500">
              已选择 {{ selectedArticles.length }} 篇文章
            </span>
            <button
              @click="handleBatchPublish"
              class="px-3 py-1 bg-green-500 text-white rounded text-sm hover:bg-green-600 transition-colors"
            >
              批量发布
            </button>
            <button
              @click="handleBatchDelete"
              class="px-3 py-1 bg-red-500 text-white rounded text-sm hover:bg-red-600 transition-colors"
            >
              批量删除
            </button>
            <button
              @click="clearSelection"
              class="px-3 py-1 bg-gray-500 text-white rounded text-sm hover:bg-gray-600 transition-colors"
            >
              取消选择
            </button>
          </div>
        </div>

        <!-- 右侧筛选 -->
        <div class="flex gap-3 items-center">
          <!-- 状态筛选 -->
          <select
            v-model="queryParams.status"
            @change="handleSearch"
            class="px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="ALL">全部状态</option>
            <option value="PUBLISHED">已发布</option>
            <option value="DRAFT">草稿</option>
          </select>

          <!-- 搜索框 -->
          <div class="relative">
            <SearchBox
              :placeholder="'搜索我的文章'"
              @search="handleSearch"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 文章列表 -->
    <div class="bg-white rounded-lg border">
      <!-- 选择模式切换 -->
      <div class="flex items-center justify-between p-4 border-b">
        <label class="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            v-model="selectMode"
            class="rounded text-blue-500 focus:ring-blue-500"
          />
          <span class="text-sm">批量选择</span>
        </label>

        <div class="text-sm text-gray-500">
          共 {{ totalElements }} 篇文章
        </div>
      </div>

      <!-- 文章项列表 -->
      <div class="divide-y">
        <!-- 加载状态 -->
        <div v-if="loading" class="p-8 text-center">
          <i class="fas fa-spinner fa-spin text-2xl text-blue-500"></i>
          <p class="mt-2 text-gray-500">加载中...</p>
        </div>

        <!-- 空状态 -->
        <div v-else-if="articles.length === 0" class="p-12 text-center">
          <i class="fas fa-inbox text-4xl text-gray-300 mb-4"></i>
          <p class="text-gray-500">暂无文章</p>
          <router-link
            to="/articles/create"
            class="mt-4 inline-block px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors"
          >
            创建第一篇文章
          </router-link>
        </div>

        <!-- 文章列表 -->
        <div
          v-else
          v-for="article in articles"
          :key="article.id"
          class="p-4 hover:bg-gray-50 transition-colors"
        >
          <div class="flex items-center gap-4">
            <!-- 复选框 -->
            <div v-if="selectMode" class="flex-shrink-0">
              <input
                type="checkbox"
                v-model="selectedArticles"
                :value="article.id"
                class="rounded text-blue-500 focus:ring-blue-500"
              />
            </div>

            <!-- 文章信息 -->
            <div class="flex-1 min-w-0">
              <div class="flex items-start gap-4">
                <!-- 封面图 -->
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

                <!-- 文章内容 -->
                <div class="flex-1 min-w-0">
                  <!-- 标题和状态 -->
                  <div class="flex items-center gap-2 mb-2">
                    <h3 class="font-medium text-gray-900 truncate hover:text-blue-600 cursor-pointer">
                      {{ article.title }}
                    </h3>
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

                  <!-- 摘要 -->
                  <p class="text-sm text-gray-600 mb-2 line-clamp-2">
                    {{ article.summary }}
                  </p>

                  <!-- 元信息 -->
                  <div class="flex items-center gap-4 text-xs text-gray-500">
                    <span>{{ formatDate(article.updateTime) }}</span>
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

            <!-- 操作按钮 -->
            <div class="flex items-center gap-2 flex-shrink-0">
              <!-- 查看 -->
              <button
                @click="viewArticle(article.id)"
                class="p-2 text-gray-500 hover:text-blue-600 transition-colors"
                title="查看"
              >
                <i class="fas fa-eye"></i>
              </button>

              <!-- 编辑 -->
              <button
                @click="editArticle(article.id)"
                class="p-2 text-gray-500 hover:text-green-600 transition-colors"
                title="编辑"
              >
                <i class="fas fa-edit"></i>
              </button>

              <!-- 发布/取消发布 -->
              <button
                v-if="article.status === 'DRAFT'"
                @click="publishArticle(article.id)"
                class="p-2 text-gray-500 hover:text-blue-600 transition-colors"
                title="发布"
              >
                <i class="fas fa-paper-plane"></i>
              </button>
              <button
                v-else
                @click="unpublishArticle(article.id)"
                class="p-2 text-gray-500 hover:text-yellow-600 transition-colors"
                title="取消发布"
              >
                <i class="fas fa-eye-slash"></i>
              </button>

              <!-- 置顶 -->
              <button
                @click="toggleTop(article)"
                :class="[
                  'p-2 transition-colors',
                  article.isTop
                    ? 'text-yellow-500 hover:text-yellow-600'
                    : 'text-gray-500 hover:text-yellow-600'
                ]"
                :title="article.isTop ? '取消置顶' : '置顶'"
              >
                <i class="fas fa-star"></i>
              </button>

              <!-- 删除 -->
              <button
                @click="deleteArticle(article.id)"
                class="p-2 text-gray-500 hover:text-red-600 transition-colors"
                title="删除"
              >
                <i class="fas fa-trash"></i>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="!loading && articles.length > 0" class="p-4 border-t">
        <Pagination
          :current-page="currentPage"
          :total-pages="totalPages"
          :total-items="totalElements"
          :items-per-page="queryParams.size"
          @page-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getMyArticles, deleteArticle as deleteArticleApi, publishArticleApi, unpublishArticleApi, setArticleTopApi } from '@/api/article'
import { getArticleStats } from '@/api/article'
import type { ArticleSummary, ArticleQueryParams } from '@/types/article'
import SearchBox from '@/components/common/SearchBox.vue'
import Pagination from '@/components/common/Pagination.vue'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const articles = ref<ArticleSummary[]>([])
const selectMode = ref(false)
const selectedArticles = ref<number[]>([])
const currentPage = ref(1)
const totalPages = ref(1)
const totalElements = ref(0)

// 查询参数
const queryParams = reactive<ArticleQueryParams>({
  page: 0,
  size: 10,
  status: 'ALL',
  keyword: ''
})

// 统计数据
const stats = ref({
  totalArticles: 0,
  publishedArticles: 0,
  draftArticles: 0,
  totalViews: 0
})

// 获取文章列表
const fetchArticles = async () => {
  loading.value = true
  try {
    queryParams.page = currentPage.value - 1
    const response = await getMyArticles(queryParams)
    articles.value = response.content
    totalPages.value = response.totalPages
    totalElements.value = response.totalElements
  } catch (error) {
    console.error('获取文章列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const response = await getArticleStats()
    stats.value = response
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 搜索处理
const handleSearch = (keyword?: string) => {
  if (keyword !== undefined) {
    queryParams.keyword = keyword
  }
  currentPage.value = 1
  fetchArticles()
}

// 页面变化处理
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchArticles()
}

// 查看文章
const viewArticle = (id: number) => {
  router.push(`/articles/${id}`)
}

// 编辑文章
const editArticle = (id: number) => {
  router.push(`/articles/${id}/edit`)
}

// 发布文章
const publishArticle = async (id: number) => {
  try {
    await publishArticleApi(id)
    fetchArticles()
    fetchStats()
  } catch (error) {
    console.error('发布文章失败:', error)
  }
}

// 取消发布文章
const unpublishArticle = async (id: number) => {
  try {
    await unpublishArticleApi(id)
    fetchArticles()
    fetchStats()
  } catch (error) {
    console.error('取消发布失败:', error)
  }
}

// 删除文章
const deleteArticle = async (id: number) => {
  if (!confirm('确定要删除这篇文章吗？')) return

  try {
    await deleteArticleApi(id)
    fetchArticles()
    fetchStats()
  } catch (error) {
    console.error('删除文章失败:', error)
  }
}

// 切换置顶
const toggleTop = async (article: ArticleSummary) => {
  try {
    await setArticleTopApi(article.id, !article.isTop)
    fetchArticles()
  } catch (error) {
    console.error('切换置顶状态失败:', error)
  }
}

// 批量发布
const handleBatchPublish = async () => {
  // 这里需要实现批量发布逻辑
  console.log('批量发布:', selectedArticles.value)
}

// 批量删除
const handleBatchDelete = async () => {
  if (!confirm(`确定要删除选中的 ${selectedArticles.value.length} 篇文章吗？`)) return

  // 这里需要实现批量删除逻辑
  console.log('批量删除:', selectedArticles.value)
}

// 清除选择
const clearSelection = () => {
  selectedArticles.value = []
  selectMode.value = false
}

// 格式化数字
const formatNumber = (num: number) => {
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 初始化
onMounted(() => {
  fetchArticles()
  fetchStats()
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