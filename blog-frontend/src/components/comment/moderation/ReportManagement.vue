<template>
  <div class="report-management">
    <!-- Filters -->
    <el-card class="filter-card">
      <el-form :model="filters" inline>
        <el-form-item label="举报状态">
          <el-select v-model="filters.reportStatus" placeholder="选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="待处理" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="举报原因">
          <el-select v-model="filters.reason" placeholder="选择原因" clearable>
            <el-option label="全部" value="" />
            <el-option label="垃圾信息" value="SPAM" />
            <el-option label="不当内容" value="INAPPROPRIATE" />
            <el-option label="攻击性言论" value="OFFENSIVE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Reports Table -->
    <el-card class="table-card">
      <div class="table-header">
        <span>举报列表</span>
        <div class="table-actions">
          <el-button
            type="success"
            size="small"
            :disabled="selectedReports.length === 0"
            @click="handleBatchApprove"
          >
            批量通过
          </el-button>
          <el-button
            type="danger"
            size="small"
            :disabled="selectedReports.length === 0"
            @click="handleBatchReject"
          >
            批量拒绝
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="reports"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="被举报评论" min-width="200">
          <template #default="{ row }">
            <div class="comment-content">
              {{ row.comment?.content || '评论已被删除' }}
              <div class="comment-meta">
                评论者: {{ row.comment?.userName }} |
                {{ formatDateTime(row.comment?.createTime) }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="举报者" width="120">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="24" :src="row.reporter?.avatar">
                {{ row.reporter?.nickname?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ row.reporter?.nickname }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="举报原因" width="120">
          <template #default="{ row }">
            <el-tag :type="getReasonTagType(row.reason)">
              {{ getReasonText(row.reason) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="处理状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="举报时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewReport(row)"
            >
              查看
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="success"
              size="small"
              @click="handleApproveReport(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="danger"
              size="small"
              @click="handleRejectReport(row)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- Report Detail Dialog -->
    <el-dialog
      v-model="reportDetailVisible"
      title="举报详情"
      width="700px"
    >
      <div v-if="selectedReport" class="report-detail">
        <div class="detail-section">
          <h4>举报信息</h4>
          <div class="detail-item">
            <label>举报ID:</label>
            <span>{{ selectedReport.id }}</span>
          </div>
          <div class="detail-item">
            <label>举报者:</label>
            <span>{{ selectedReport.reporter?.nickname }}</span>
          </div>
          <div class="detail-item">
            <label>举报原因:</label>
            <el-tag :type="getReasonTagType(selectedReport.reason)">
              {{ getReasonText(selectedReport.reason) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <label>举报说明:</label>
            <div class="description-text">{{ selectedReport.description || '无' }}</div>
          </div>
          <div class="detail-item">
            <label>举报时间:</label>
            <span>{{ formatDateTime(selectedReport.createTime) }}</span>
          </div>
        </div>

        <div class="detail-section">
          <h4>被举报评论</h4>
          <div class="comment-section">
            <div class="comment-header">
              <el-avatar :size="32" :src="selectedReport.comment?.userAvatar">
                {{ selectedReport.comment?.userName?.charAt(0) }}
              </el-avatar>
              <div class="comment-meta-info">
                <span class="username">{{ selectedReport.comment?.userName }}</span>
                <span class="time">{{ formatDateTime(selectedReport.comment?.createTime) }}</span>
              </div>
            </div>
            <div class="comment-text">
              {{ selectedReport.comment?.content }}
            </div>
          </div>
        </div>

        <div v-if="selectedReport.reviewer" class="detail-section">
          <h4>处理信息</h4>
          <div class="detail-item">
            <label>处理状态:</label>
            <el-tag :type="getStatusTagType(selectedReport.status)">
              {{ getStatusText(selectedReport.status) }}
            </el-tag>
          </div>
          <div class="detail-item">
            <label>处理人:</label>
            <span>{{ selectedReport.reviewer.nickname }}</span>
          </div>
          <div class="detail-item">
            <label>处理时间:</label>
            <span>{{ formatDateTime(selectedReport.reviewTime) }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="reportDetailVisible = false">关闭</el-button>
        <el-button
          v-if="selectedReport?.status === 'PENDING'"
          type="success"
          @click="handleApproveReport(selectedReport)"
        >
          通过举报
        </el-button>
        <el-button
          v-if="selectedReport?.status === 'PENDING'"
          type="danger"
          @click="handleRejectReport(selectedReport)"
        >
          拒绝举报
        </el-button>
      </template>
    </el-dialog>

    <!-- Batch Operation Dialog -->
    <el-dialog
      v-model="batchOperationVisible"
      :title="batchOperationTitle"
      width="400px"
    >
      <p>{{ batchOperationMessage }}</p>
      <el-input
        v-model="operationReason"
        type="textarea"
        placeholder="请输入处理原因"
        :rows="3"
      />
      <template #footer>
        <el-button @click="batchOperationVisible = false">取消</el-button>
        <el-button
          :type="batchOperationType"
          @click="confirmBatchOperation"
        >
          确认{{ batchOperationType === 'success' ? '通过' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCommentModerationStore } from '@/stores/commentModerationStore'
import type { CommentReport } from '@shared/types'

const commentModerationStore = useCommentModerationStore()

// Data
const loading = ref(false)
const reports = ref<CommentReport[]>([])
const selectedReports = ref<CommentReport[]>([])
const selectedReport = ref<CommentReport | null>(null)
const reportDetailVisible = ref(false)
const batchOperationVisible = ref(false)
const batchOperationType = ref<'success' | 'danger'>('success')
const batchOperationTitle = ref('')
const batchOperationMessage = ref('')
const operationReason = ref('')

const filters = reactive({
  reportStatus: '',
  reason: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// Methods
const fetchReports = async () => {
  loading.value = true
  try {
    await commentModerationStore.fetchReports({
      status: filters.reportStatus,
      page: pagination.page,
      size: pagination.size
    })
    reports.value = commentModerationStore.reports
    pagination.total = commentModerationStore.pagination.total
  } catch (error) {
    ElMessage.error('获取举报列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchReports()
}

const handleReset = () => {
  filters.reportStatus = ''
  filters.reason = ''
  pagination.page = 1
  fetchReports()
}

const handleSelectionChange = (selection: CommentReport[]) => {
  selectedReports.value = selection
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  fetchReports()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  fetchReports()
}

const handleViewReport = (report: CommentReport) => {
  selectedReport.value = report
  reportDetailVisible.value = true
}

const handleApproveReport = async (report: CommentReport) => {
  try {
    await commentModerationStore.reviewReport(report.id, {
      status: 'APPROVED',
      reviewNote: operationReason.value || '举报成立，已删除相关评论'
    })
    ElMessage.success('举报处理成功')
    reportDetailVisible.value = false
    operationReason.value = ''
    await fetchReports()
  } catch (error) {
    ElMessage.error('处理举报失败')
  }
}

const handleRejectReport = async (report: CommentReport) => {
  try {
    await commentModerationStore.reviewReport(report.id, {
      status: 'REJECTED',
      reviewNote: operationReason.value || '举报不成立'
    })
    ElMessage.success('举报处理成功')
    reportDetailVisible.value = false
    operationReason.value = ''
    await fetchReports()
  } catch (error) {
    ElMessage.error('处理举报失败')
  }
}

const handleBatchApprove = () => {
  if (selectedReports.value.length === 0) {
    ElMessage.warning('请选择要通过的举报')
    return
  }
  batchOperationType.value = 'success'
  batchOperationTitle.value = '批量通过举报'
  batchOperationMessage.value = `确定要通过选中的 ${selectedReports.value.length} 条举报吗？`
  batchOperationVisible.value = true
}

const handleBatchReject = () => {
  if (selectedReports.value.length === 0) {
    ElMessage.warning('请选择要拒绝的举报')
    return
  }
  batchOperationType.value = 'danger'
  batchOperationTitle.value = '批量拒绝举报'
  batchOperationMessage.value = `确定要拒绝选中的 ${selectedReports.value.length} 条举报吗？`
  batchOperationVisible.value = true
}

const confirmBatchOperation = async () => {
  try {
    const status = batchOperationType.value === 'success' ? 'APPROVED' : 'REJECTED'
    const reviewNote = operationReason.value || (
      batchOperationType.value === 'success'
        ? '批量通过举报'
        : '批量拒绝举报'
    )

    for (const report of selectedReports.value) {
      await commentModerationStore.reviewReport(report.id, { status, reviewNote })
    }

    ElMessage.success('批量处理成功')
    batchOperationVisible.value = false
    operationReason.value = ''
    selectedReports.value = []
    await fetchReports()
  } catch (error) {
    ElMessage.error('批量处理失败')
  }
}

const getReasonTagType = (reason: string) => {
  const types: Record<string, string> = {
    SPAM: 'warning',
    INAPPROPRIATE: 'danger',
    OFFENSIVE: 'danger',
    OTHER: 'info'
  }
  return types[reason] || 'info'
}

const getReasonText = (reason: string) => {
  const texts: Record<string, string> = {
    SPAM: '垃圾信息',
    INAPPROPRIATE: '不当内容',
    OFFENSIVE: '攻击性言论',
    OTHER: '其他'
  }
  return texts[reason] || reason
}

const getStatusTagType = (status: string) => {
  const types: Record<string, string> = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    PENDING: '待处理',
    APPROVED: '已通过',
    REJECTED: '已拒绝'
  }
  return texts[status] || status
}

const formatDateTime = (date: Date | string) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleString('zh-CN')
}

// Lifecycle
onMounted(() => {
  fetchReports()
})
</script>

<style scoped>
.report-management {
  margin-top: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.comment-content {
  line-height: 1.5;
}

.comment-meta {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 14px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.report-detail {
  padding: 20px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
}

.detail-item label {
  width: 80px;
  font-weight: bold;
  color: #606266;
  flex-shrink: 0;
}

.detail-item span {
  flex: 1;
  color: #303133;
}

.description-text {
  line-height: 1.6;
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  white-space: pre-wrap;
}

.comment-section {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 16px;
  background-color: #fafafa;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.comment-meta-info {
  margin-left: 12px;
}

.comment-meta-info .username {
  font-weight: bold;
  margin-right: 8px;
}

.comment-meta-info .time {
  font-size: 12px;
  color: #909399;
}

.comment-text {
  line-height: 1.6;
  color: #303133;
}
</style>