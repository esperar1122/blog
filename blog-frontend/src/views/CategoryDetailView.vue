<template>
  <div class="category-detail">
    <!-- 面包屑导航 -->
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ name: 'home' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ name: 'categories' }">分类</el-breadcrumb-item>
      <el-breadcrumb-item v-if="category">{{ category.name }}</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 分类信息 -->
    <el-card v-if="category" class="category-info">
      <div class="category-header">
        <div class="category-icon" v-if="category.icon">
          <el-icon size="48"><component :is="category.icon" /></el-icon>
        </div>
        <div class="category-meta">
          <h1>{{ category.name }}</h1>
          <p v-if="category.description" class="description">{{ category.description }}</p>
          <div class="stats">
            <el-tag type="info" size="small">
              {{ category.articleCount }} 篇文章
            </el-tag>
            <el-tag v-if="parentCategory" type="primary" size="small">
              父分类: {{ parentCategory.name }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 文章列表 -->
    <el-card class="article-list">
      <template #header>
        <div class="list-header">
          <h3>文章列表</h3>
          <div class="sort-options">
            <el-select
              v-model="sortBy"
              placeholder="排序方式"
              size="small"
              style="width: 120px"
            >
              <el-option label="最新发布" value="publish_time" />
              <el-option label="浏览最多" value="view_count" />
              <el-option label="点赞最多" value="like_count" />
            </el-select>
          </div>
        </div>
      </template>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="articles.length === 0" class="empty-container">
        <el-empty description="该分类下暂无文章" />
      </div>

      <div v-else class="article-grid">
        <article-card
          v-for="article in articles"
          :key="article.id"
          :article="article"
          @click="handleArticleClick"
        />
      </div>

      <!-- 分页 -->
      <div v-if="total > 0" class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 30, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategoryById, getArticlesByCategory } from '@/api/category'
import { getPublishedArticlesWithPagination } from '@/api/article'
import ArticleCard from '@/components/ArticleCard.vue'
import type { Category, Article } from '@blog/shared/types'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const category = ref<Category | null>(null)
const articles = ref<Article[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const sortBy = ref('publish_time')

// 计算属性
const categoryId = computed(() => Number(route.params.id))

const parentCategory = computed(() => {
  // TODO: 从categories列表中查找父分类
  return null
})

// 加载分类信息
const loadCategory = async () => {
  try {
    category.value = await getCategoryById(categoryId.value)
  } catch (error) {
    console.error('加载分类失败:', error)
    router.push({ name: 'not-found' })
  }
}

// 加载文章列表
const loadArticles = async () => {
  if (!category.value) return

  try {
    loading.value = true
    const result = await getPublishedArticlesWithPagination({
      page: currentPage.value,
      size: pageSize.value,
      categoryId: categoryId.value,
      keyword: '',
      tagId: null
    })

    articles.value = result.records || []
    total.value = result.total || 0
  } catch (error) {
    console.error('加载文章失败:', error)
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 处理文章点击
const handleArticleClick = (article: Article) => {
  router.push({ name: 'article-detail', params: { id: article.id } })
}

// 处理分页大小变化
const handleSizeChange = (newSize: number) => {
  pageSize.value = newSize
  currentPage.value = 1
  loadArticles()
}

// 处理当前页变化
const handleCurrentChange = (newPage: number) => {
  currentPage.value = newPage
  loadArticles()
}

// 监听分类ID变化
watch(
  () => categoryId.value,
  (newId) => {
    if (newId) {
      currentPage.value = 1
      loadCategory()
      loadArticles()
    }
  },
  { immediate: true }
)

// 监听排序方式变化
watch(
  sortBy,
  () => {
    loadArticles()
  }
)

// 监听路由变化
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      loadCategory()
      loadArticles()
    }
  }
)

onMounted(() => {
  if (categoryId.value) {
    loadCategory()
    loadArticles()
  }
})
</script>

<style scoped>
.category-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.breadcrumb {
  margin-bottom: 20px;
}

.category-info {
  margin-bottom: 24px;
  background: linear-gradient(135deg, var(--el-color-primary-light-9) 0%, var(--el-fill-color-light) 100%);
}

.category-header {
  display: flex;
  align-items: center;
  gap: 20px;
}

.category-icon {
  color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
  padding: 16px;
  border-radius: 12px;
}

.category-meta h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.description {
  margin: 0 0 12px 0;
  color: var(--el-text-color-regular);
  font-size: 16px;
  line-height: 1.6;
}

.stats {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.article-list {
  min-height: 400px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.loading-container {
  padding: 20px;
}

.empty-container {
  padding: 40px;
  text-align: center;
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

@media (max-width: 768px) {
  .category-detail {
    padding: 16px;
  }

  .category-header {
    flex-direction: column;
    text-align: center;
  }

  .category-meta h1 {
    font-size: 24px;
  }

  .article-grid {
    grid-template-columns: 1fr;
  }

  .list-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
}
</style>