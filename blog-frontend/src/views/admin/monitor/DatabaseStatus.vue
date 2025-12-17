<template>
  <div class="database-status">
    <div class="status-header">
      <h3>数据库状态监控</h3>
      <div class="header-actions">
        <el-tag :type="dbStatus.status === 'UP' ? 'success' : 'danger'" size="large">
          {{ dbStatus.status }}
        </el-tag>
        <el-button @click="refreshStatus" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新状态
        </el-button>
      </div>
    </div>

    <!-- 基本信息 -->
    <el-card class="status-card" header="基本信息">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="数据库状态">
          <el-tag :type="dbStatus.status === 'UP' ? 'success' : 'danger'">
            {{ dbStatus.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="数据库版本">
          {{ dbStatus.version || 'N/A' }}
        </el-descriptions-item>
        <el-descriptions-item label="连接URL">
          <el-tooltip :content="dbStatus.url" placement="top">
            <span class="url-text">{{ truncateUrl(dbStatus.url) }}</span>
          </el-tooltip>
        </el-descriptions-item>
        <el-descriptions-item label="最后更新时间">
          {{ dbStatus.lastUpdated || 'N/A' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 连接池状态 -->
    <el-card class="status-card" header="连接池状态">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="pool-metrics">
            <div class="metric-item">
              <span class="metric-label">总连接数:</span>
              <span class="metric-value">{{ connectionPool.total || 0 }}</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">活跃连接数:</span>
              <span class="metric-value">{{ connectionPool.active || 0 }}</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">空闲连接数:</span>
              <span class="metric-value">{{ connectionPool.idle || 0 }}</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">最大连接数:</span>
              <span class="metric-value">{{ connectionPool.max || 0 }}</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">最小空闲连接数:</span>
              <span class="metric-value">{{ connectionPool.minIdle || 0 }}</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">等待连接的线程数:</span>
              <span class="metric-value">{{ connectionPool.waiting || 0 }}</span>
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="pool-visual">
            <h4>连接池使用率</h4>
            <el-progress
              type="dashboard"
              :percentage="connectionPool.usagePercent || 0"
              :width="180"
              :color="getPoolColor(connectionPool.usagePercent)"
            >
              <template #default="{ percentage }">
                <span class="percentage-value">{{ percentage }}%</span>
              </template>
            </el-progress>
            <div class="pool-stats">
              <div class="stat-item">
                <span class="stat-label">使用率:</span>
                <span class="stat-value">{{ connectionPool.usagePercent || 0 }}%</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">空闲率:</span>
                <span class="stat-value">{{ ((connectionPool.idle / connectionPool.max) * 100).toFixed(1) || 0 }}%</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 连接池趋势图表 -->
      <div class="chart-section">
        <h4>连接池趋势</h4>
        <div ref="poolChart" class="chart-container"></div>
      </div>
    </el-card>

    <!-- 性能指标 -->
    <el-card class="status-card" header="性能指标">
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="perf-metrics">
            <div class="metric-item">
              <span class="metric-label">总查询数:</span>
              <span class="metric-value">{{ formatNumber(performance.totalQueries) }}</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">每秒查询数(QPS):</span>
              <span class="metric-value">{{ performance.queriesPerSecond || 0 }}</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">平均查询时间:</span>
              <span class="metric-value">{{ performance.avgQueryTime || 0 }}ms</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">最大查询时间:</span>
              <span class="metric-value">{{ performance.maxQueryTime || 0 }}ms</span>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="perf-metrics">
            <div class="metric-item">
              <span class="metric-label">总事务数:</span>
              <span class="metric-value">{{ formatNumber(performance.totalTransactions) }}</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">每秒事务数(TPS):</span>
              <span class="metric-value">{{ performance.transactionsPerSecond || 0 }}</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">缓存命中率:</span>
              <span class="metric-value">{{ performance.cacheHitRate || 0 }}%</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">索引使用率:</span>
              <span class="metric-value">{{ performance.indexUsageRate || 0 }}%</span>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div ref="perfChart" class="chart-small"></div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 数据库详细信息 -->
    <el-card class="status-card" header="数据库详细信息">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="表空间" name="tablespaces">
          <el-table :data="dbStatus.tableSpaces || []" border>
            <el-table-column prop="name" label="表空间名称" />
            <el-table-column prop="totalSize" label="总大小">
              <template #default="scope">
                {{ formatBytes(scope.row.totalSize) }}
              </template>
            </el-table-column>
            <el-table-column prop="usedSize" label="已使用大小">
              <template #default="scope">
                {{ formatBytes(scope.row.usedSize) }}
              </template>
            </el-table-column>
            <el-table-column prop="usagePercent" label="使用率">
              <template #default="scope">
                <el-progress
                  :percentage="scope.row.usagePercent"
                  :color="getProgressColor(scope.row.usagePercent)"
                  :show-text="true"
                />
              </template>
            </el-table-column>
            <el-table-column prop="tableCount" label="表数量" />
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="慢查询统计" name="slowQueries">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="慢查询总数">
              {{ slowQueries.totalSlowQueries || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="今日慢查询数">
              {{ slowQueries.todaySlowQueries || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="慢查询阈值">
              {{ slowQueries.slowQueryThreshold || 0 }}秒
            </el-descriptions-item>
            <el-descriptions-item label="最慢查询">
              <el-tooltip :content="slowQueries.slowestQuery" placement="top">
                <span class="slow-query">{{ truncateQuery(slowQueries.slowestQuery) }}</span>
              </el-tooltip>
            </el-descriptions-item>
          </el-descriptions>

          <div class="slow-query-chart">
            <h4>慢查询趋势</h4>
            <div ref="slowQueryChart" class="chart-medium"></div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="性能分析" name="performance">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="analysis-section">
                <h4>查询性能分析</h4>
                <el-table :data="queryPerformanceData" border size="small">
                  <el-table-column prop="type" label="查询类型" width="120" />
                  <el-table-column prop="count" label="执行次数" />
                  <el-table-column prop="avgTime" label="平均耗时(ms)" />
                  <el-table-column prop="maxTime" label="最大耗时(ms)" />
                </el-table>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="analysis-section">
                <h4>索引分析</h4>
                <el-table :data="indexAnalysisData" border size="small">
                  <el-table-column prop="tableName" label="表名" />
                  <el-table-column prop="indexName" label="索引名" />
                  <el-table-column prop="usage" label="使用率">
                    <template #default="scope">
                      <el-progress
                        :percentage="scope.row.usage"
                        :show-text="true"
                        :format="(percentage) => `${percentage}%`"
                      />
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { systemMonitorApi } from '@/api/admin/monitor'

// 响应式数据
const loading = ref(false)
const activeTab = ref('tablespaces')

const dbStatus = ref({})
const connectionPool = ref({})
const performance = ref({})
const slowQueries = ref({})
const queryPerformanceData = ref([])
const indexAnalysisData = ref([])

// 图表引用
const poolChart = ref(null)
const perfChart = ref(null)
const slowQueryChart = ref(null)

let poolChartInstance = null
let perfChartInstance = null
let slowQueryChartInstance = null
let refreshTimer = null

// 方法
const initCharts = () => {
  // 连接池趋势图表
  if (poolChart.value) {
    poolChartInstance = echarts.init(poolChart.value)
    updatePoolChart()
  }

  // 性能图表
  if (perfChart.value) {
    perfChartInstance = echarts.init(perfChart.value)
    updatePerfChart()
  }

  // 慢查询图表
  if (slowQueryChart.value) {
    slowQueryChartInstance = echarts.init(slowQueryChart.value)
    updateSlowQueryChart()
  }
}

const updatePoolChart = () => {
  const option = {
    title: {
      text: '连接池使用趋势',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      bottom: 0
    },
    xAxis: {
      type: 'category',
      data: Array.from({ length: 12 }, (_, i) => `${11 - i}分钟前`)
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '活跃连接',
        type: 'line',
        data: Array.from({ length: 12 }, () => Math.floor(Math.random() * 10 + 5)),
        smooth: true,
        itemStyle: { color: '#409eff' }
      },
      {
        name: '空闲连接',
        type: 'line',
        data: Array.from({ length: 12 }, () => Math.floor(Math.random() * 15 + 10)),
        smooth: true,
        itemStyle: { color: '#67c23a' }
      },
      {
        name: '等待连接',
        type: 'line',
        data: Array.from({ length: 12 }, () => Math.floor(Math.random() * 3)),
        smooth: true,
        itemStyle: { color: '#f56c6c' }
      }
    ]
  }
  poolChartInstance.setOption(option)
}

const updatePerfChart = () => {
  const option = {
    title: {
      text: '数据库性能指标',
      left: 'center',
      textStyle: { fontSize: 12 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    series: [
      {
        name: '查询类型',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
        data: [
          { value: 35, name: 'SELECT', itemStyle: { color: '#409eff' } },
          { value: 25, name: 'INSERT', itemStyle: { color: '#67c23a' } },
          { value: 20, name: 'UPDATE', itemStyle: { color: '#e6a23c' } },
          { value: 20, name: 'DELETE', itemStyle: { color: '#f56c6c' } }
        ]
      }
    ]
  }
  perfChartInstance.setOption(option)
}

const updateSlowQueryChart = () => {
  const option = {
    title: {
      text: '慢查询统计',
      left: 'center',
      textStyle: { fontSize: 14 }
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['1小时前', '2小时前', '3小时前', '4小时前', '5小时前', '6小时前']
    },
    yAxis: {
      type: 'value',
      name: '查询数量'
    },
    series: [
      {
        name: '慢查询数量',
        type: 'bar',
        data: [12, 8, 15, 6, 9, 4],
        itemStyle: { color: '#f56c6c' }
      }
    ]
  }
  slowQueryChartInstance.setOption(option)
}

const refreshStatus = async () => {
  loading.value = true
  try {
    const response = await systemMonitorApi.getDatabaseStatus()
    const data = response.data

    dbStatus.value = data

    // 更新连接池数据
    if (data.connectionPool) {
      connectionPool.value = {
        ...data.connectionPool,
        usagePercent: Math.round((data.connectionPool.total / data.connectionPool.max) * 100)
      }
    }

    // 更新性能指标
    if (data.metrics) {
      performance.value = data.metrics
    }

    // 更新慢查询统计
    if (data.slowQueryStats) {
      slowQueries.value = data.slowQueryStats
    }

    // 更新图表
    updatePoolChart()
    updatePerfChart()
    updateSlowQueryChart()

    // 生成模拟数据
    generateMockData()

    ElMessage.success('数据库状态已刷新')
  } catch (error) {
    console.error('刷新数据库状态失败:', error)
    ElMessage.error('刷新数据库状态失败')
  } finally {
    loading.value = false
  }
}

const generateMockData = () => {
  // 模拟查询性能数据
  queryPerformanceData.value = [
    { type: 'SELECT', count: 1250, avgTime: 45, maxTime: 320 },
    { type: 'INSERT', count: 320, avgTime: 28, maxTime: 180 },
    { type: 'UPDATE', count: 180, avgTime: 38, maxTime: 220 },
    { type: 'DELETE', count: 85, avgTime: 22, maxTime: 150 }
  ]

  // 模拟索引分析数据
  indexAnalysisData.value = [
    { tableName: 't_user', indexName: 'idx_username', usage: 85 },
    { tableName: 't_article', indexName: 'idx_title', usage: 92 },
    { tableName: 't_article', indexName: 'idx_category', usage: 78 },
    { tableName: 't_comment', indexName: 'idx_article_id', usage: 95 }
  ]
}

const truncateUrl = (url) => {
  if (!url) return 'N/A'
  return url.length > 50 ? url.substring(0, 50) + '...' : url
}

const truncateQuery = (query) => {
  if (!query) return 'N/A'
  return query.length > 100 ? query.substring(0, 100) + '...' : query
}

const formatNumber = (num) => {
  if (!num) return '0'
  return num.toLocaleString()
}

const formatBytes = (bytes) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getPoolColor = (percentage) => {
  if (percentage < 70) return '#67c23a'
  if (percentage < 85) return '#e6a23c'
  return '#f56c6c'
}

const getProgressColor = (percentage) => {
  if (percentage < 50) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
}

// 生命周期
onMounted(() => {
  initCharts()
  refreshStatus()

  // 设置定时刷新
  refreshTimer = setInterval(refreshStatus, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  // 销毁图表实例
  ;[poolChartInstance, perfChartInstance, slowQueryChartInstance].forEach(chart => {
    if (chart) chart.dispose()
  })
})
</script>

<style scoped>
.database-status {
  padding: 20px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.status-header h3 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.status-card {
  margin-bottom: 20px;
}

.url-text {
  font-family: monospace;
  font-size: 12px;
  color: #606266;
}

.metric-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 14px;
}

.metric-label {
  color: #606266;
}

.metric-value {
  color: #303133;
  font-weight: 500;
}

.pool-visual {
  text-align: center;
}

.pool-visual h4 {
  margin-bottom: 20px;
  color: #303133;
}

.percentage-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.pool-stats {
  margin-top: 20px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  margin: 8px 0;
  font-size: 14px;
}

.stat-label {
  color: #606266;
}

.stat-value {
  color: #303133;
  font-weight: 500;
}

.chart-section {
  margin-top: 30px;
}

.chart-section h4 {
  margin-bottom: 16px;
  color: #303133;
  text-align: center;
}

.chart-container {
  height: 200px;
  width: 100%;
}

.chart-small {
  height: 150px;
  width: 100%;
}

.chart-medium {
  height: 250px;
  width: 100%;
}

.perf-metrics,
.pool-metrics {
  padding: 16px 0;
}

.analysis-section {
  margin-bottom: 24px;
}

.analysis-section h4 {
  margin-bottom: 16px;
  color: #303133;
}

.slow-query {
  font-family: monospace;
  font-size: 12px;
  color: #f56c6c;
}

.slow-query-chart {
  margin-top: 20px;
}
</style>