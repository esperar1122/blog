<template>
  <div class="search-view">
    <div class="search-header">
      <div class="search-container">
        <SearchBox
          v-model="searchQuery.q"
          placeholder="搜索文章、标签、作者..."
          :show-history="true"
          :show-suggestions="true"
          @search="handleSearch"
          @suggestion-select="handleSuggestionSelect"
        />
      </div>
    </div>

    <div v-if="hasSearched" class="search-content">
      <div class="search-layout">
        <!-- 左侧筛选器 -->
        <aside class="search-sidebar">
          <SearchFilters
            v-model="searchQuery"
            :loading="loading"
            @filter-change="handleFilterChange"
            @keyword-select="handleKeywordSelect"
          />

          <!-- 搜索历史 -->
          <SearchHistory
            v-if="searchHistory.length > 0"
            @history-select="handleHistorySelect"
          />

          <!-- 热门搜索 -->
          <HotKeywords
            :show-refresh="true"
            @keyword-select="handleKeywordSelect"
          />
        </aside>

        <!-- 主内容区 -->
        <main class="search-main">
          <SearchResults
            :search-result="searchResult"
            :loading="loading"
            :can-load-more="canLoadMore"
            @load-more="handleLoadMore"
            @clear-search="handleClearSearch"
          />
        </main>

        <!-- 右侧边栏（可选） -->
        <aside v-if="showSidebar" class="search-extra">
          <!-- 可以放置广告、推广等内容 -->
        </aside>
      </div>
    </div>

    <!-- 初始状态 -->
    <div v-else class="search-initial">
      <div class="initial-content">
        <el-icon size="80"><Search /></el-icon>
        <h2>探索精彩内容</h2>
        <p>搜索您感兴趣的文章、话题和作者</p>

        <!-- 热门搜索预览 -->
        <div v-if="hotKeywords.length > 0" class="popular-searches">
          <h3>热门搜索</h3>
          <div class="popular-tags">
            <el-tag
              v-for="keyword in hotKeywords.slice(0, 8)"
              :key="keyword.id"
              :type="getKeywordType(keyword.position)"
              class="popular-tag"
              @click="handleKeywordSelect(keyword.keyword)"
            >
              {{ keyword.keyword }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useSearchStore } from '@/stores/searchStore'
import { useArticleStore } from '@/stores/article'
import SearchBox from '@/components/search/SearchBox.vue'
import SearchFilters from '@/components/search/SearchFilters.vue'
import SearchResults from '@/components/search/SearchResults.vue'
import SearchHistory from '@/components/search/SearchHistory.vue'
import HotKeywords from '@/components/search/HotKeywords.vue'
import type { SearchQuery, SearchResult, HotKeywords as HotKeywordsType } from 'blog-shared'

const router = useRouter()
const route = useRoute()
const searchStore = useSearchStore()
const articleStore = useArticleStore()

// 响应式数据
const searchQuery = ref<SearchQuery>({
  q: '',
  fields: ['title', 'content'],
  categoryId: undefined,
  tagIds: [],
  sortBy: 'relevance'
})

const hasSearched = ref(false)
const showSidebar = ref(false)
const currentPage = ref(1)

// 计算属性
const searchResult = computed(() => searchStore.searchResult)
const searchHistory = computed(() => searchStore.searchHistory)
const hotKeywords = computed(() => searchStore.hotKeywords)
const loading = computed(() => searchStore.loading)

const canLoadMore = computed(() => {
  return searchResult.value &&
         currentPage.value < searchResult.value.pagination.totalPages
})

// 监听路由参数
watch(() => route.query, (query) => {
  if (query.q) {
    searchQuery.value.q = query.q as string
    handleSearch(query.q as string)
  }
}, { immediate: true })

// 执行搜索
const handleSearch = async (keyword: string) => {
  if (!keyword.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  if (keyword.trim().length < 2) {
    ElMessage.warning('搜索关键词至少需要2个字符')
    return
  }

  hasSearched.value = true
  currentPage.value = 1

  try {
    const query = { ...searchQuery.value, q: keyword.trim() }
    await searchStore.performSearch(query)

    // 更新URL
    await router.push({
      path: '/search',
      query: { q: keyword.trim() }
    })

  } catch (error) {
    ElMessage.error('搜索失败，请稍后重试')
  }
}

// 选择搜索建议
const handleSuggestionSelect = (suggestion: string) => {
  handleSearch(suggestion)
}

// 筛选条件变化
const handleFilterChange = (filters: SearchQuery) => {
  searchQuery.value = { ...filters }
  if (filters.q) {
    handleSearch(filters.q)
  }
}

// 选择热门关键词
const handleKeywordSelect = (keyword: string) => {
  handleSearch(keyword)
}

// 选择搜索历史
const handleHistorySelect = (keyword: string) => {
  handleSearch(keyword)
}

// 加载更多结果
const handleLoadMore = async () => {
  if (canLoadMore.value && !loading.value) {
    currentPage.value++
    try {
      await searchStore.loadMoreResults(currentPage.value)
    } catch (error) {
      ElMessage.error('加载更多失败')
      currentPage.value--
    }
  }
}

// 清空搜索
const handleClearSearch = () => {
  hasSearched.value = false
  searchQuery.value = {
    q: '',
    fields: ['title', 'content'],
    categoryId: undefined,
    tagIds: [],
    sortBy: 'relevance'
  }
  searchStore.clearResults()
  router.push('/search')
}

// 获取关键词标签类型
const getKeywordType = (position: number) => {
  if (position <= 3) return 'danger'
  if (position <= 6) return 'warning'
  if (position <= 10) return 'success'
  return 'info'
}

// 初始化
onMounted(async () => {
  // 初始化搜索store
  await searchStore.initialize()

  // 初始化文章store（获取分类和标签）
  try {
    await Promise.all([
      articleStore.fetchCategories(),
      articleStore.fetchTags()
    ])
  } catch (error) {
    console.error('Failed to initialize data:', error)
  }
})
</script>

<style scoped>
.search-view {
  min-height: 100vh;
  background: var(--el-bg-color-page);
}

.search-header {
  background: white;
  border-bottom: 1px solid var(--el-border-color-lighter);
  padding: 24px 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.search-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.search-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
}

.search-layout {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 24px;
  align-items: start;
}

.search-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 120px;
}

.search-main {
  min-width: 0;
}

.search-extra {
  width: 280px;
  position: sticky;
  top: 120px;
}

.search-initial {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: 40px 20px;
}

.initial-content {
  text-align: center;
  max-width: 600px;
}

.initial-content h2 {
  margin: 20px 0 12px;
  color: var(--el-text-color-primary);
}

.initial-content p {
  margin: 0 0 40px;
  color: var(--el-text-color-regular);
  font-size: 16px;
}

.popular-searches {
  text-align: left;
}

.popular-searches h3 {
  margin: 0 0 16px;
  color: var(--el-text-color-primary);
  font-size: 18px;
}

.popular-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.popular-tag {
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  padding: 8px 16px;
}

.popular-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .search-layout {
    grid-template-columns: 1fr;
  }

  .search-sidebar {
    position: static;
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 16px;
  }

  .search-extra {
    display: none;
  }
}

@media (max-width: 768px) {
  .search-header {
    padding: 16px 0;
  }

  .search-content {
    padding: 16px;
  }

  .initial-content {
    padding: 20px;
  }

  .initial-content h2 {
    font-size: 24px;
  }

  .popular-tags {
    justify-content: center;
  }

  .search-sidebar {
    grid-template-columns: 1fr;
  }
}
</style>