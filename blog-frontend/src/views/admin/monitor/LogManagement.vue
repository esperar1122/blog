<template>
  <div class="log-management">
    <div class="page-header">
      <h1>操作日志管理</h1>
      <p>查看和管理系统操作日志</p>
    </div>

    <!-- 搜索和筛选区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" :inline="true" @submit.prevent="handleSearch">
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select
            v-model="searchForm.operation"
            placeholder="请选择操作类型"
            clearable
            @clear="handleSearch"
            @change="handleSearch"
          >
            <el-option label="登录" value="登录" />
            <el-option label="登出" value="登出" />
            <el-option label="创建文章" value="创建文章" />
            <el-option label="编辑文章" value="编辑文章" />
            <el-option label="删除文章" value="删除文章" />
            <el-option label="用户管理" value="用户管理" />
            <el-option label="系统设置" value="系统设置" />
            <el-option label="数据备份" value="数据备份" />
          </el-select>
        </el-form-item>
        <el-form-item label="请求方法">
          <el-select
            v-model="searchForm.method"
            placeholder="请选择请求方法"
            clearable
            @clear="handleSearch"
            @change="handleSearch"
          >
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择操作状态"
            clearable
            @clear="handleSearch"
            @change="handleSearch"
          >
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleSearch"
          />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="请输入关键词搜索"
            clearable
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleExport">
            <el-icon><Download /></el-icon>
            导出日志
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon success">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ logStats.totalLogs || 0 }}</div>
              <div class="stat-label">总日志数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon warning">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ logStats.todayLogs || 0 }}</div>
              <div class="stat-label">今日日志</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon primary">
              <el-icon><SuccessFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ logStats.successCount || 0 }}</div>
              <div class="stat-label">成功操作</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon danger">
              <el-icon><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ logStats.failureCount || 0 }}</div>
              <div class="stat-label">失败操作</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日志列表 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>操作日志列表</span>
          <div class="header-actions">
            <el-button size="small" @click="handleBatchDelete" :disabled="selectedLogs.length === 0">
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
            <el-button size="small" @click="handleClearAll" type="danger">
              <el-icon><DeleteFilled /></el-icon>
              清空日志
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="logList"
        :loading="loading"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
        border
        stripe
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="operation" label="操作类型" width="120" />
        <el-table-column prop="method" label="请求方法" width="100">
          <template #default="scope">
            <el-tag :type="getMethodTagType(scope.row.method)" size="small">
              {{ scope.row.method }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="耗时" width="100" sortable="custom">
          <template #default="scope">
            <span :class="getTimeClass(scope.row.time)">
              {{ scope.row.time }}ms
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="160" sortable="custom">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleViewDetail(scope.row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="60%" destroy-on-close>
      <div class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="日志ID">
            {{ currentLog.id }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ currentLog.username }}
          </el-descriptions-item>
          <el-descriptions-item label="操作类型">
            {{ currentLog.operation }}
          </el-descriptions-item>
          <el-descriptions-item label="请求方法">
            {{ currentLog.method }}
          </el-descriptions-item>
          <el-descriptions-item label="IP地址">
            {{ currentLog.ip }}
          </el-descriptions-item>
          <el-descriptions-item label="操作状态">
            <el-tag :type="currentLog.status === 1 ? 'success' : 'danger'">
              {{ currentLog.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="执行耗时">
            {{ currentLog.time }}ms
          </el-descriptions-item>
          <el-descriptions-item label="操作时间">
            {{ formatDateTime(currentLog.createTime) }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="detail-section">
          <h4>请求参数</h4>
          <el-input
            v-model="currentLog.params"
            type="textarea"
            :rows="6"
            readonly
            placeholder="暂无参数信息"
          />
        </div>

        <div class="detail-section">
          <h4>操作结果</h4>
          <el-input
            v-model="currentLog.result"
            type="textarea"
            :rows="6"
            readonly
            placeholder="暂无结果信息"
          />
        </div>

        <div class="detail-section">
          <h4>用户代理</h4>
          <el-input
            v-model="currentLog.userAgent"
            type="textarea"
            :rows="3"
            readonly
            placeholder="暂无用户代理信息"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Refresh,
  Download,
  Delete,
  DeleteFilled,
  View,
  CircleCheck,
  Clock,
  SuccessFilled,
  CircleClose
} from '@element-plus/icons-vue'
import { adminLogApi } from '@/api/admin/log'

// 响应式数据
const loading = ref(false)
const logList = ref([])
const selectedLogs = ref([])
const detailVisible = ref(false)
const currentLog = ref({})

// 搜索表单
const searchForm = reactive({
  username: '',
  operation: '',
  method: '',
  status: '',
  keyword: ''
})

const timeRange = ref([])

// 分页
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

// 日志统计
const logStats = ref({})

// 排序
const sortConfig = reactive({
  prop: '',
  order: ''
})

// 方法
const handleSearch = () => {
  pagination.current = 1
  loadLogs()
}

const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    operation: '',
    method: '',
    status: '',
    keyword: ''
  })
  timeRange.value = []
  handleSearch()
}

const handleExport = async () => {
  try {
    const params = {
      ...searchForm,
      startTime: timeRange.value?.[0],
      endTime: timeRange.value?.[1]
    }

    const response = await adminLogApi.exportLogs(params)

    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.download = `operation_logs_${new Date().toISOString()}.csv`
    link.click()
    window.URL.revokeObjectURL(url)

    ElMessage.success('日志导出成功')
  } catch (error) {
    console.error('导出日志失败:', error)
    ElMessage.error('导出日志失败')
  }
}

const handleSelectionChange = (selection) => {
  selectedLogs.value = selection
}

const handleSortChange = ({ prop, order }) => {
  sortConfig.prop = prop
  sortConfig.order = order
  loadLogs()
}

const handleSizeChange = (val) => {
  pagination.size = val
  loadLogs()
}

const handleCurrentChange = (val) => {
  pagination.current = val
  loadLogs()
}

const handleViewDetail = (row) => {
  currentLog.value = { ...row }
  detailVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这条日志记录吗？', '提示', {
      type: 'warning'
    })

    await adminLogApi.deleteLog(row.id)
    ElMessage.success('删除成功')
    loadLogs()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除日志失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleBatchDelete = async () => {
  if (selectedLogs.value.length === 0) {
    ElMessage.warning('请先选择要删除的日志')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedLogs.value.length} 条日志记录吗？`,
      '批量删除',
      { type: 'warning' }
    )

    const ids = selectedLogs.value.map(log => log.id)
    await adminLogApi.deleteLogs(ids)
    ElMessage.success('批量删除成功')
    loadLogs()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有日志记录吗？此操作不可恢复！', '清空日志', {
      type: 'warning',
      confirmButtonText: '确定清空',
      cancelButtonText: '取消'
    })

    await adminLogApi.clearAllLogs()
    ElMessage.success('日志清空成功')
    loadLogs()
    loadLogStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空日志失败:', error)
      ElMessage.error('清空日志失败')
    }
  }
}

const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm,
      startTime: timeRange.value?.[0],
      endTime: timeRange.value?.[1],
      sortBy: sortConfig.prop,
      sortOrder: sortConfig.order === 'ascending' ? 'asc' : 'desc'
    }

    const response = await adminLogApi.getLogs(params)
    logList.value = response.data.records
    pagination.total = response.data.total
  } catch (error) {
    console.error('加载日志列表失败:', error)
    ElMessage.error('加载日志列表失败')
  } finally {
    loading.value = false
  }
}

const loadLogStats = async () => {
  try {
    const response = await adminLogApi.getLogStats()
    logStats.value = response.data
  } catch (error) {
    console.error('加载日志统计失败:', error)
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString()
}

const getMethodTagType = (method) => {
  const typeMap = {
    'GET': 'info',
    'POST': 'success',
    'PUT': 'warning',
    'DELETE': 'danger'
  }
  return typeMap[method] || 'info'
}

const getTimeClass = (time) => {
  if (time < 100) return 'time-fast'
  if (time < 500) return 'time-normal'
  return 'time-slow'
}

// 生命周期
onMounted(() => {
  loadLogs()
  loadLogStats()
})
</script>

<style scoped>
.log-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
}

.search-card {
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 20px;
  color: white;
}

.stat-icon.success {
  background: #67c23a;
}

.stat-icon.warning {
  background: #e6a23c;
}

.stat-icon.primary {
  background: #409eff;
}

.stat-icon.danger {
  background: #f56c6c;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.time-fast {
  color: #67c23a;
}

.time-normal {
  color: #e6a23c;
}

.time-slow {
  color: #f56c6c;
}

.log-detail {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section {
  margin-top: 20px;
}

.detail-section h4 {
  margin-bottom: 12px;
  color: #303133;
  font-size: 16px;
}
</style>