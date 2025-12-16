<template>
  <div class="article-lifecycle-management">
    <div class="page-header">
      <div class="header-content">
        <h1>文章生命周期管理</h1>
        <p class="header-description">管理文章的发布、置顶、删除等状态，查看版本历史和操作日志</p>
      </div>
    </div>

    <div class="content-container">
      <el-tabs v-model="activeTab" class="management-tabs">
        <!-- 我的文章 -->
        <el-tab-pane label="我的文章" name="my-articles">
          <div class="tab-content">
            <div class="filters-section">
              <div class="filter-row">
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索文章标题..."
                  prefix-icon="Search"
                  clearable
                  style="width: 300px"
                  @input="handleSearch"
                />
                <el-select
                  v-model="statusFilter"
                  placeholder="筛选状态"
                  clearable
                  style="width: 150px"
                  @change="handleStatusFilter"
                >
                  <el-option label="全部" value="" />
                  <el-option label="草稿" value="DRAFT" />
                  <el-option label="已发布" value="PUBLISHED" />
                  <el-option label="已删除" value="DELETED" />
                </el-select>
                <el-button-group>
                  <el-button
                    :type="viewMode === 'grid' ? 'primary' : 'default'"
                    @click="viewMode = 'grid'"
                  >
                    <el-icon><Grid /></el-icon>
                    网格
                  </el-button>
                  <el-button
                    :type="viewMode === 'list' ? 'primary' : 'default'"
                    @click="viewMode = 'list'"
                  >
                    <el-icon><List /></el-icon>
                    列表
                  </el-button>
                </el-button-group>
              </div>
            </div>

            <div v-if="loading" class="loading-container">
              <el-skeleton :rows="5" animated />
            </div>

            <div v-else-if="filteredArticles.length === 0" class="empty-state">
              <el-empty description="暂无文章" />
            </div>

            <div v-else class="articles-container">
              <!-- 网格视图 -->
              <div v-if="viewMode === 'grid'" class="articles-grid">
                <div
                  v-for="article in paginatedArticles"
                  :key="article.id"
                  class="article-card"
                >
                  <ArticleCard
                    :article="article"
                    :show-status="true"
                    :show-actions="true"
                    @status-changed="handleArticleStatusChanged"
                  />
                </div>
              </div>

              <!-- 列表视图 -->
              <div v-else class="articles-list">
                <el-table :data="paginatedArticles" stripe>
                  <el-table-column prop="title" label="标题" min-width="200">
                    <template #default="{ row }">
                      <div class="article-title-cell">
                        <span class="title-text">{{ row.title }}</span>
                        <el-tag
                          v-if="row.isTop"
                          type="warning"
                          size="small"
                        >
                          置顶
                        </el-tag>
                      </div>
                    </template>
                  </el-table-column>

                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag
                        :type="getStatusTagType(row.status)"
                        size="small"
                      >
                        {{ getStatusText(row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>

                  <el-table-column prop="viewCount" label="浏览" width="80" align="center" />

                  <el-table-column prop="likeCount" label="点赞" width="80" align="center" />

                  <el-table-column prop="commentCount" label="评论" width="80" align="center" />

                  <el-table-column prop="createTime" label="创建时间" width="160">
                    <template #default="{ row }">
                      {{ formatTime(row.createTime) }}
                    </template>
                  </el-table-column>

                  <el-table-column label="操作" width="200" fixed="right">
                    <template #default="{ row }">
                      <ArticleStatusManager
                        :article="row"
                        :show-actions="true"
                        @status-changed="handleArticleStatusChanged"
                      />
                    </template>
                  </el-table-column>
                </el-table>
              </div>

              <!-- 分页 -->
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="currentPage"
                  v-model:page-size="pageSize"
                  :page-sizes="[10, 20, 50]"
                  :total="filteredArticles.length"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 已删除文章 -->
        <el-tab-pane label="已删除文章" name="deleted">
          <div class="tab-content">
            <DeletedArticlesView @restore="handleArticleRestore" />
          </div>
        </el-tab-pane>

        <!-- 定时发布 -->
        <el-tab-pane label="定时发布" name="scheduled">
          <div class="tab-content">
            <ScheduledArticlesView @cancel="handleScheduleCancel" />
          </div>
        </el-tab-pane>

        <!-- 置顶文章 -->
        <el-tab-pane label="置顶文章" name="pinned">
          <div class="tab-content">
            <PinnedArticlesView @unpin="handleArticleUnpin" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  ElTabs, ElTabPane, ElInput, ElSelect, ElOption, ElButtonGroup,
  ElButton, ElSkeleton, ElEmpty, ElTable, ElTableColumn,
  ElTag, ElPagination, ElIcon
} from 'element-plus'
import { Grid, List, Search } from '@element-plus/icons-vue'
import type { Article } from '@/types/article'
import { getMyArticles } from '@/api/article'
import ArticleCard from '@/components/article/ArticleCard.vue'
import ArticleStatusManager from '@/components/article/ArticleStatusManager.vue'
import DeletedArticlesView from './DeletedArticlesView.vue'
import ScheduledArticlesView from './ScheduledArticlesView.vue'
import PinnedArticlesView from './PinnedArticlesView.vue'

const activeTab = ref('my-articles')
const loading = ref(false)
const articles = ref<Article[]>([])
const searchKeyword = ref('')
const statusFilter = ref('')
const viewMode = ref<'grid' | 'list'>('grid')

// 分页
const currentPage = ref(1)
const pageSize = ref(20)

// 过滤后的文章
const filteredArticles = computed(() => {
  let result = articles.value

  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(article =>
      article.title.toLowerCase().includes(keyword) ||
      (article.summary && article.summary.toLowerCase().includes(keyword))
    )
  }

  // 状态筛选
  if (statusFilter.value) {
    result = result.filter(article => article.status === statusFilter.value)
  }

  return result
})

// 分页后的文章
const paginatedArticles = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredArticles.value.slice(start, end)
})

// 加载文章列表
async function loadArticles() {
  try {
    loading.value = true
    const response = await getMyArticles({
      page: 1,
      size: 1000 // 获取所有文章，前端分页
    })
    articles.value = response.content || []
  } catch (error) {
    console.error('加载文章失败:', error)
  } finally {
    loading.value = false
  }
}

// 处理搜索
function handleSearch() {
  currentPage.value = 1
}

// 处理状态筛选
function handleStatusFilter() {
  currentPage.value = 1
}

// 处理分页
function handleSizeChange(newSize: number) {
  pageSize.value = newSize
  currentPage.value = 1
}

function handleCurrentChange(newPage: number) {
  currentPage.value = newPage
}

// 处理文章状态变化
function handleArticleStatusChanged(article: Article) {
  const index = articles.value.findIndex(a => a.id === article.id)
  if (index !== -1) {
    articles.value[index] = article
  }
}

// 处理文章恢复
function handleArticleRestore(articleId: number) {
  loadArticles()
}

// 处理取消定时
function handleScheduleCancel(articleId: number) {
  loadArticles()
}

// 处理取消置顶
function handleArticleUnpin(articleId: number) {
  loadArticles()
}

// 获取状态标签类型
function getStatusTagType(status: string): string {
  switch (status) {
    case 'DRAFT':
      return 'info'
    case 'PUBLISHED':
      return 'success'
    case 'DELETED':
      return 'danger'
    default:
      return 'warning'
  }
}

// 获取状态文本
function getStatusText(status: string): string {
  switch (status) {
    case 'DRAFT':
      return '草稿'
    case 'PUBLISHED':
      return '已发布'
    case 'DELETED':
      return '已删除'
    default:
      return '未知'
  }
}

// 格式化时间
function formatTime(time: string): string {
  return new Date(time).toLocaleDateString()
}

onMounted(() => {
  loadArticles()
})
</script>

<style scoped>
.article-lifecycle-management {
  min-height: 100vh;
  background: #f5f5f5;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 40px 20px;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
}

.header-content h1 {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 700;
}

.header-description {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.content-container {
  max-width: 1200px;
  margin: -20px auto 0;
  padding: 0 20px 40px;
}

.management-tabs {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07);
  overflow: hidden;
}

.tab-content {
  padding: 24px;
}

.filters-section {
  margin-bottom: 24px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
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
  gap: 24px;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.articles-list {
  .el-table {
    border-radius: 8px;
    overflow: hidden;
  }
}

.article-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination-container {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}

@media (max-width: 768px) {
  .page-header {
    padding: 30px 20px;
  }

  .header-content h1 {
    font-size: 24px;
  }

  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-row .el-input,
  .filter-row .el-select {
    width: 100%;
  }

  .articles-grid {
    grid-template-columns: 1fr;
  }

  :deep(.el-table) {
    font-size: 12px;
  }
}
</style>