<template>
  <div class="content-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>内容管理</h3>
          <el-radio-group v-model="activeTab" @change="handleTabChange">
            <el-radio-button label="articles">文章管理</el-radio-button>
            <el-radio-button label="comments">评论管理</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-form :model="queryParams" ref="queryForm" :inline="true" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="请输入搜索关键词"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="状态" v-if="activeTab === 'articles'">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已删除" value="DELETED" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" v-if="activeTab === 'comments'">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 150px">
            <el-option label="正常" value="NORMAL" />
            <el-option label="已删除" value="DELETED" />
          </el-select>
        </el-form-item>

        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="handleExport" :loading="exporting">导出</el-button>
        </el-form-item>
      </el-form>

      <div class="table-toolbar" v-if="selectedRows.length > 0">
        <el-alert
          :title="'已选择 ' + selectedRows.length + ' 条记录'"
          type="info"
          show-icon
          :closable="false"
        >
          <template #default>
            <el-button
              size="small"
              type="danger"
              @click="handleBatchDelete"
              :loading="batchOperating"
            >
              批量删除
            </el-button>

            <el-button
              v-if="activeTab === 'articles'"
              size="small"
              type="success"
              @click="handleBatchPublish"
              :loading="batchOperating"
            >
              批量发布
            </el-button>

            <el-button
              v-if="activeTab === 'articles'"
              size="small"
              type="warning"
              @click="handleBatchUnpublish"
              :loading="batchOperating"
            >
              批量取消发布
            </el-button>
          </template>
        </el-alert>
      </div>

      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        row-key="id"
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />

        <template v-if="activeTab === 'articles'">
          <el-table-column label="文章" min-width="300">
            <template #default="{ row }">
              <div class="article-info">
                <div class="article-title">{{ row.title }}</div>
                <div class="article-summary" v-if="row.summary">{{ row.summary }}</div>
                <div class="article-meta">
                  <span>作者: {{ row.authorName || '未知' }}</span>
                  <span>分类: {{ row.categoryName || '未分类' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="统计数据" width="150">
            <template #default="{ row }">
              <div class="stats">
                <span>👁 {{ row.viewCount || 0 }}</span>
                <span>👍 {{ row.likeCount || 0 }}</span>
                <span>💬 {{ row.commentCount || 0 }}</span>
              </div>
            </template>
          </el-table-column>
        </template>

        <template v-if="activeTab === 'comments'">
          <el-table-column label="评论内容" min-width="300">
            <template #default="{ row }">
              <div class="comment-info">
                <div class="comment-content">{{ row.content }}</div>
                <div class="comment-meta">
                  <span>评论者: {{ row.userName || '未知' }}</span>
                  <span>文章ID: {{ row.articleId }}</span>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="点赞数" width="80">
            <template #default="{ row }">
              {{ row.likeCount || 0 }}
            </template>
          </el-table-column>
        </template>

        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="activeTab === 'articles'"
              link
              type="primary"
              @click="handleReview(row)"
            >
              审核
            </el-button>

            <el-button
              link
              type="warning"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <ArticleReviewDialog
      v-model="reviewDialogVisible"
      :article="currentArticle"
      @success="handleReviewSuccess"
    />

    <ExportDialog
      v-model="exportDialogVisible"
      :content-type="activeTab"
      :filters="queryParams"
      @export="handleDoExport"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ElTable } from 'element-plus'
import adminContentService from '@/services/adminContentService'
import ArticleReviewDialog from './ArticleReviewDialog.vue'
import ExportDialog from './ExportDialog.vue'

const activeTab = ref('articles')
const loading = ref(false)
const exporting = ref(false)
const batchOperating = ref(false)
const tableData = ref([])
const total = ref(0)
const selectedRows = ref([])
const dateRange = ref()

const queryParams = reactive({
  page: 1,
  size: 20,
  keyword: '',
  status: '',
  startTime: '',
  endTime: ''
})

const reviewDialogVisible = ref(false)
const exportDialogVisible = ref(false)
const currentArticle = ref(null)

const queryForm = ref()

onMounted(() => {
  loadData()
})

const handleTabChange = () => {
  resetQuery()
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    const isArticle = activeTab.value === 'articles'
    const response = isArticle
      ? await adminContentService.getArticles(queryParams)
      : await adminContentService.getComments(queryParams)

    tableData.value = response.data.records || []
    total.value = response.data.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
    console.error('加载数据错误:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  loadData()
}

const resetQuery = () => {
  queryForm.value?.resetFields()
  queryParams.page = 1
  queryParams.keyword = ''
  queryParams.status = ''
  queryParams.startTime = ''
  queryParams.endTime = ''
  dateRange.value = null
  loadData()
}

const handleDateChange = (dates: string[]) => {
  if (dates && dates.length === 2) {
    queryParams.startTime = dates[0]
    queryParams.endTime = dates[1]
  } else {
    queryParams.startTime = ''
    queryParams.endTime = ''
  }
}

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

const handleReview = (row: any) => {
  currentArticle.value = { ...row }
  reviewDialogVisible.value = true
}

const handleReviewSuccess = () => {
  reviewDialogVisible.value = false
  loadData()
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const isArticle = activeTab.value === 'articles'
    const operation = isArticle
      ? adminContentService.batchOperateArticles({
          operationType: 'delete',
          ids: [row.id]
        })
      : adminContentService.batchOperateComments({
          operationType: 'delete',
          ids: [row.id]
        })

    await operation

    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('删除错误:', error)
    }
  }
}

const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 条记录吗？`, '确认批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    batchOperating.value = true

    const ids = selectedRows.value.map((row: any) => row.id)
    const isArticle = activeTab.value === 'articles'

    const operation = isArticle
      ? adminContentService.batchOperateArticles({
          operationType: 'delete',
          ids
        })
      : adminContentService.batchOperateComments({
          operationType: 'delete',
          ids
        })

    await operation

    ElMessage.success('批量删除成功')
    selectedRows.value = []
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
      console.error('批量删除错误:', error)
    }
  } finally {
    batchOperating.value = false
  }
}

const handleBatchPublish = async () => {
  try {
    batchOperating.value = true

    const ids = selectedRows.value.map((row: any) => row.id)

    await adminContentService.batchOperateArticles({
      operationType: 'publish',
      ids
    })

    ElMessage.success('批量发布成功')
    selectedRows.value = []
    loadData()
  } catch (error) {
    ElMessage.error('批量发布失败')
    console.error('批量发布错误:', error)
  } finally {
    batchOperating.value = false
  }
}

const handleBatchUnpublish = async () => {
  try {
    batchOperating.value = true

    const ids = selectedRows.value.map((row: any) => row.id)

    await adminContentService.batchOperateArticles({
      operationType: 'unpublish',
      ids
    })

    ElMessage.success('批量取消发布成功')
    selectedRows.value = []
    loadData()
  } catch (error) {
    ElMessage.error('批量取消发布失败')
    console.error('批量取消发布错误:', error)
  } finally {
    batchOperating.value = false
  }
}

const handleExport = () => {
  exportDialogVisible.value = true
}

const handleDoExport = async (format: string) => {
  try {
    exporting.value = true

    const isArticle = activeTab.value === 'articles'
    const response = isArticle
      ? await adminContentService.exportArticles(queryParams, format)
      : await adminContentService.exportComments(queryParams, format)

    const exportData = response.data

    const link = document.createElement('a')
    link.href = adminContentService.downloadFile(exportData.fileName)
    link.download = exportData.fileName
    link.click()

    ElMessage.success('导出成功')
    exportDialogVisible.value = false
  } catch (error) {
    ElMessage.error('导出失败')
    console.error('导出错误:', error)
  } finally {
    exporting.value = false
  }
}

const getStatusType = (status: string) => {
  const statusMap: Record<string, string> = {
    'PUBLISHED': 'success',
    'DRAFT': 'warning',
    'DELETED': 'danger',
    'NORMAL': 'success'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'PUBLISHED': '已发布',
    'DRAFT': '草稿',
    'DELETED': '已删除',
    'NORMAL': '正常'
  }
  return statusMap[status] || status
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.content-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
}

.search-form {
  margin-bottom: 20px;
}

.table-toolbar {
  margin-bottom: 20px;
}

.article-info {
  line-height: 1.4;
}

.article-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.article-summary {
  color: #666;
  font-size: 12px;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-meta {
  font-size: 12px;
  color: #999;
}

.article-meta span {
  margin-right: 10px;
}

.comment-info {
  line-height: 1.4;
}

.comment-content {
  font-size: 14px;
  margin-bottom: 4px;
  max-height: 3em;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.comment-meta {
  font-size: 12px;
  color: #999;
}

.comment-meta span {
  margin-right: 10px;
}

.stats {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>