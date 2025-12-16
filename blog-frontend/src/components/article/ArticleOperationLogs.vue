<template>
  <div class="article-operation-logs">
    <div class="header">
      <h3>操作日志</h3>
      <div class="header-actions">
        <el-filter-menu
          v-model="selectedOperationType"
          placeholder="筛选操作类型"
          :options="operationTypeOptions"
          clearable
          @change="filterLogs"
        />
        <el-button
          type="text"
          size="small"
          @click="refreshLogs"
          :loading="loading"
        >
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <div class="logs-container">
      <div
        v-if="loading"
        class="loading-container"
      >
        <el-skeleton :rows="6" animated />
      </div>

      <div
        v-else-if="filteredLogs.length === 0"
        class="empty-state"
      >
        <el-empty description="暂无操作日志" />
      </div>

      <div
        v-else
        class="logs-list"
      >
        <div
          v-for="log in filteredLogs"
          :key="log.id"
          class="log-item"
        >
          <div class="log-icon">
            <el-icon :size="20" :color="getOperationTypeColor(log.operationType)">
              <component :is="getOperationTypeIcon(log.operationType)" />
            </el-icon>
          </div>

          <div class="log-content">
            <div class="log-header">
              <span class="operation-type">{{ log.operationTypeDescription }}</span>
              <span class="log-time">{{ formatTime(log.createTime) }}</span>
            </div>

            <div class="log-details">
              <div class="operator-info">
                <el-avatar :size="24" :src="log.operatorAvatar" />
                <span class="operator-name">{{ log.operatorName }}</span>
                <span v-if="log.operatorIp" class="operator-ip">IP: {{ log.operatorIp }}</span>
              </div>

              <div v-if="log.oldStatus || log.newStatus" class="status-change">
                <span
                  v-if="log.oldStatus"
                  class="status-badge old-status"
                >
                  {{ getStatusText(log.oldStatus) }}
                </span>
                <el-icon class="arrow-icon"><ArrowRight /></el-icon>
                <span
                  v-if="log.newStatus"
                  class="status-badge new-status"
                >
                  {{ getStatusText(log.newStatus) }}
                </span>
              </div>

              <div v-if="log.operationDetail" class="operation-detail">
                <el-collapse>
                  <el-collapse-item title="详细信息">
                    <pre>{{ formatOperationDetail(log.operationDetail) }}</pre>
                  </el-collapse-item>
                </el-collapse>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="filteredLogs.length > 0" class="footer">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="filteredLogs.length"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, type PropType } from 'vue'
import {
  ElIcon, ElSkeleton, ElEmpty, ElButton, ElAvatar, ElCollapse, ElCollapseItem,
  ElPagination
} from 'element-plus'
import {
  Refresh, ArrowRight, Promotion, RemoveFilled, Top, Remove,
  Delete, RefreshLeft, Timer, Edit, View
} from '@element-plus/icons-vue'
import type { ArticleOperationLog, ArticleOperationType } from '@/types/article'
import { getArticleOperationLogs } from '@/api/article'

const props = defineProps({
  articleId: {
    type: Number,
    required: true
  }
})

const loading = ref(false)
const logs = ref<ArticleOperationLog[]>([])
const filteredLogs = ref<ArticleOperationLog[]>([])

const currentPage = ref(1)
const pageSize = ref(20)
const selectedOperationType = ref('')

const operationTypeOptions = [
  { label: '全部', value: '' },
  { label: '发布', value: 'PUBLISH' },
  { label: '下线', value: 'UNPUBLISH' },
  { label: '置顶', value: 'PIN' },
  { label: '取消置顶', value: 'UNPIN' },
  { label: '删除', value: 'SOFT_DELETE' },
  { label: '恢复', value: 'RESTORE' },
  { label: '定时发布', value: 'SCHEDULE_PUBLISH' },
  { label: '更新', value: 'UPDATE' }
]

// 加载操作日志
async function loadLogs() {
  try {
    loading.value = true
    logs.value = await getArticleOperationLogs(props.articleId)
    filterLogs()
  } catch (error) {
    console.error('加载操作日志失败:', error)
  } finally {
    loading.value = false
  }
}

// 刷新日志
async function refreshLogs() {
  await loadLogs()
}

// 筛选日志
function filterLogs() {
  if (selectedOperationType.value) {
    filteredLogs.value = logs.value.filter(log =>
      log.operationType === selectedOperationType.value
    )
  } else {
    filteredLogs.value = [...logs.value]
  }
  currentPage.value = 1
}

// 分页处理
function handleSizeChange(newSize: number) {
  pageSize.value = newSize
  currentPage.value = 1
}

function handleCurrentChange(newPage: number) {
  currentPage.value = newPage
}

// 获取操作类型图标
function getOperationTypeIcon(operationType: string) {
  const iconMap: Record<string, any> = {
    'PUBLISH': Promotion,
    'UNPUBLISH': RemoveFilled,
    'PIN': Top,
    'UNPIN': Remove,
    'SOFT_DELETE': Delete,
    'RESTORE': RefreshLeft,
    'SCHEDULE_PUBLISH': Timer,
    'UPDATE': Edit,
    'VIEW': View
  }
  return iconMap[operationType] || Edit
}

// 获取操作类型颜色
function getOperationTypeColor(operationType: string): string {
  const colorMap: Record<string, string> = {
    'PUBLISH': '#10b981',
    'UNPUBLISH': '#f59e0b',
    'PIN': '#8b5cf6',
    'UNPIN': '#6b7280',
    'SOFT_DELETE': '#ef4444',
    'RESTORE': '#06b6d4',
    'SCHEDULE_PUBLISH': '#ec4899',
    'UPDATE': '#3b82f6'
  }
  return colorMap[operationType] || '#6b7280'
}

// 获取状态文本
function getStatusText(status: string): string {
  const statusMap: Record<string, string> = {
    'DRAFT': '草稿',
    'PUBLISHED': '已发布',
    'DELETED': '已删除'
  }
  return statusMap[status] || status
}

// 格式化时间
function formatTime(time: string): string {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  // 如果是今天
  if (diff < 24 * 60 * 60 * 1000 && date.getDate() === now.getDate()) {
    return `今天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }

  // 如果是昨天
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.getDate() === yesterday.getDate() &&
      date.getMonth() === yesterday.getMonth() &&
      date.getFullYear() === yesterday.getFullYear()) {
    return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
  }

  // 其他日期
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化操作详情
function formatOperationDetail(detail: string): string {
  try {
    // 尝试解析JSON格式
    if (detail.startsWith('{') || detail.startsWith('[')) {
      return JSON.stringify(JSON.parse(detail), null, 2)
    }
    return detail
  } catch {
    return detail
  }
}

// 监听筛选条件变化
watch(selectedOperationType, filterLogs)

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.article-operation-logs {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logs-container {
  padding: 20px;
  max-height: 600px;
  overflow-y: auto;
}

.loading-container {
  padding: 20px;
}

.empty-state {
  padding: 40px;
}

.logs-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.log-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.2s ease;
}

.log-item:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
}

.log-icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.log-content {
  flex: 1;
  min-width: 0;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.operation-type {
  font-weight: 600;
  color: #1f2937;
  font-size: 16px;
}

.log-time {
  color: #6b7280;
  font-size: 14px;
}

.log-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.operator-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #6b7280;
  font-size: 14px;
}

.operator-name {
  font-weight: 500;
  color: #374151;
}

.operator-ip {
  font-size: 12px;
  color: #9ca3af;
}

.status-change {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.old-status {
  background-color: #fef3c7;
  color: #92400e;
  border: 1px solid #fcd34d;
}

.new-status {
  background-color: #d1fae5;
  color: #065f46;
  border: 1px solid #6ee7b7;
}

.arrow-icon {
  color: #6b7280;
  font-size: 16px;
}

.operation-detail {
  margin-top: 8px;
}

:deep(.el-collapse) {
  border: none;
}

:deep(.el-collapse-item__header) {
  font-size: 12px;
  color: #6b7280;
  padding: 0;
  height: auto;
  line-height: 1.4;
}

:deep(.el-collapse-item__content) {
  padding: 8px 0 0 0;
}

:deep(.el-collapse-item__content pre) {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  padding: 8px;
  font-size: 12px;
  line-height: 1.4;
  color: #374151;
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.footer {
  padding: 20px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .log-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .log-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .operator-info {
    flex-wrap: wrap;
  }
}
</style>