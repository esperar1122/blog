<template>
  <div class="system-monitor">
    <div class="page-header">
      <h1>系统监控</h1>
      <p>实时监控系统运行状态和性能指标</p>
    </div>

    <!-- 系统概览卡片 -->
    <el-row :gutter="20" class="overview-cards">
      <el-col :span="6" v-for="(card, index) in overviewCards" :key="index">
        <el-card class="overview-card" :class="card.status">
          <div class="card-content">
            <div class="card-icon">
              <el-icon :size="32" :color="card.color">
                <component :is="card.icon" />
              </el-icon>
            </div>
            <div class="card-info">
              <div class="card-title">{{ card.title }}</div>
              <div class="card-value">{{ card.value }}</div>
              <div class="card-desc">{{ card.description }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 实时图表区域 -->
    <el-row :gutter="20" class="charts-section">
      <!-- 系统性能趋势 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>系统性能趋势</span>
              <el-button-group>
                <el-button size="small" :type="timeRange === '1h' ? 'primary' : ''" @click="changeTimeRange('1h')">1小时</el-button>
                <el-button size="small" :type="timeRange === '6h' ? 'primary' : ''" @click="changeTimeRange('6h')">6小时</el-button>
                <el-button size="small" :type="timeRange === '24h' ? 'primary' : ''" @click="changeTimeRange('24h')">24小时</el-button>
              </el-button-group>
            </div>
          </template>
          <div ref="performanceChart" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 资源使用率 -->
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span>资源使用率</span>
          </template>
          <div ref="resourceChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细监控指标 -->
    <el-row :gutter="20" class="metrics-section">
      <!-- JVM指标 -->
      <el-col :span="8">
        <el-card class="metrics-card">
          <template #header>
            <div class="card-header">
              <span>JVM指标</span>
              <el-button size="small" circle @click="refreshJvmMetrics">
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </template>
          <div class="metrics-content">
            <div class="metric-item" v-for="(item, index) in jvmMetrics" :key="index">
              <div class="metric-label">{{ item.label }}</div>
              <div class="metric-value">
                <el-progress
                  :percentage="item.percentage"
                  :color="getProgressColor(item.percentage)"
                  :show-text="false"
                  :stroke-width="6"
                />
                <span class="value-text">{{ item.value }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 数据库状态 -->
      <el-col :span="8">
        <el-card class="metrics-card">
          <template #header>
            <div class="card-header">
              <span>数据库状态</span>
              <el-tag :type="databaseStatus.status === 'UP' ? 'success' : 'danger'">
                {{ databaseStatus.status }}
              </el-tag>
            </div>
          </template>
          <div class="database-info">
            <div class="info-item">
              <span class="info-label">连接数:</span>
              <span class="info-value">{{ databaseStatus.connections }} / {{ databaseStatus.maxConnections }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">版本:</span>
              <span class="info-value">{{ databaseStatus.version }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">QPS:</span>
              <span class="info-value">{{ databaseStatus.qps || 0 }}</span>
            </div>
            <el-progress
              :percentage="databaseStatus.connectionUsage"
              :show-text="true"
              :format="(percentage) => `${percentage}%`"
            />
          </div>
        </el-card>
      </el-col>

      <!-- Redis状态 -->
      <el-col :span="8">
        <el-card class="metrics-card">
          <template #header>
            <div class="card-header">
              <span>Redis状态</span>
              <el-tag :type="redisStatus.status === 'UP' ? 'success' : 'danger'">
                {{ redisStatus.status }}
              </el-tag>
            </div>
          </template>
          <div class="redis-info">
            <div class="info-item">
              <span class="info-label">内存使用:</span>
              <span class="info-value">{{ formatBytes(redisStatus.usedMemory) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">命中率:</span>
              <span class="info-value">{{ redisStatus.hitRate || 0 }}%</span>
            </div>
            <div class="info-item">
              <span class="info-label">连接数:</span>
              <span class="info-value">{{ redisStatus.connectedClients || 0 }}</span>
            </div>
            <el-progress
              :percentage="redisStatus.memoryUsage || 0"
              :show-text="true"
              :format="(percentage) => `${percentage}%`"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 健康检查状态 -->
    <el-row class="health-section">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统健康检查</span>
              <el-button size="small" @click="refreshHealthCheck">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          <div class="health-check">
            <div
              class="health-item"
              v-for="(item, key) in healthCheck"
              :key="key"
              :class="item.status === 'UP' ? 'healthy' : 'unhealthy'"
            >
              <div class="health-icon">
                <el-icon v-if="item.status === 'UP'" color="#67c23a"><CircleCheck /></el-icon>
                <el-icon v-else color="#f56c6c"><CircleClose /></el-icon>
              </div>
              <div class="health-info">
                <div class="health-name">{{ getHealthName(key) }}</div>
                <div class="health-status">{{ item.status }}</div>
                <div class="health-details" v-if="item.details">
                  <el-tooltip effect="dark" content="查看详情" placement="top">
                    <el-button size="small" text @click="showHealthDetails(key, item.details)">
                      查看详情
                    </el-button>
                  </el-tooltip>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 健康检查详情对话框 -->
    <el-dialog v-model="healthDetailVisible" title="健康检查详情" width="50%">
      <pre>{{ JSON.stringify(healthDetailContent, null, 2) }}</pre>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  Monitor,
  Server,
  Database,
  Refresh,
  CircleCheck,
  CircleClose
} from '@element-plus/icons-vue'
import { systemMonitorApi } from '@/api/admin/monitor'

// 响应式数据
const timeRange = ref('1h')
const systemStatus = ref({})
const performanceChart = ref(null)
const resourceChart = ref(null)
let performanceChartInstance = null
let resourceChartInstance = null
let refreshTimer = null

// 概览卡片数据
const overviewCards = ref([
  {
    title: '系统状态',
    value: '运行中',
    description: '所有服务正常',
    icon: 'Monitor',
    color: '#67c23a',
    status: 'success'
  },
  {
    title: 'CPU使用率',
    value: '0%',
    description: '当前CPU使用率',
    icon: 'Server',
    color: '#409eff',
    status: 'info'
  },
  {
    title: '内存使用率',
    value: '0%',
    description: '当前内存使用率',
    icon: 'Monitor',
    color: '#e6a23c',
    status: 'warning'
  },
  {
    title: '磁盘使用率',
    value: '0%',
    description: '当前磁盘使用率',
    icon: 'Server',
    color: '#f56c6c',
    status: 'danger'
  }
])

// JVM指标
const jvmMetrics = ref([
  { label: '堆内存使用', value: '0MB / 0MB', percentage: 0 },
  { label: '非堆内存使用', value: '0MB / 0MB', percentage: 0 },
  { label: 'GC次数', value: '0次', percentage: 0 }
])

// 数据库状态
const databaseStatus = ref({
  status: 'UNKNOWN',
  connections: 0,
  maxConnections: 0,
  version: '',
  qps: 0,
  connectionUsage: 0
})

// Redis状态
const redisStatus = ref({
  status: 'UNKNOWN',
  usedMemory: 0,
  hitRate: 0,
  connectedClients: 0,
  memoryUsage: 0
})

// 健康检查
const healthCheck = ref({})
const healthDetailVisible = ref(false)
const healthDetailContent = ref({})

// 方法
const initCharts = () => {
  // 初始化性能趋势图表
  if (performanceChart.value) {
    performanceChartInstance = echarts.init(performanceChart.value)
    updatePerformanceChart()
  }

  // 初始化资源使用图表
  if (resourceChart.value) {
    resourceChartInstance = echarts.init(resourceChart.value)
    updateResourceChart()
  }
}

const updatePerformanceChart = () => {
  const option = {
    title: {
      text: '系统性能趋势',
      left: 'center',
      textStyle: {
        fontSize: 14
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      bottom: 0
    },
    xAxis: {
      type: 'category',
      data: generateTimeLabels()
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: 'CPU使用率',
        type: 'line',
        data: generateRandomData(),
        smooth: true,
        itemStyle: { color: '#409eff' }
      },
      {
        name: '内存使用率',
        type: 'line',
        data: generateRandomData(),
        smooth: true,
        itemStyle: { color: '#67c23a' }
      },
      {
        name: '磁盘使用率',
        type: 'line',
        data: generateRandomData(),
        smooth: true,
        itemStyle: { color: '#e6a23c' }
      }
    ]
  }
  performanceChartInstance.setOption(option)
}

const updateResourceChart = () => {
  const option = {
    title: {
      text: '资源使用率',
      left: 'center',
      textStyle: {
        fontSize: 14
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}% ({d}%)'
    },
    legend: {
      bottom: 0
    },
    series: [
      {
        name: '资源使用',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '45%'],
        data: [
          { value: 35, name: '已用CPU', itemStyle: { color: '#409eff' } },
          { value: 65, name: '空闲CPU', itemStyle: { color: '#ecf5ff' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  resourceChartInstance.setOption(option)
}

const generateTimeLabels = () => {
  const labels = []
  const now = new Date()
  for (let i = 11; i >= 0; i--) {
    const time = new Date(now - i * 5 * 60 * 1000)
    labels.push(time.toLocaleTimeString())
  }
  return labels
}

const generateRandomData = () => {
  return Array.from({ length: 12 }, () => Math.floor(Math.random() * 80 + 10))
}

const loadSystemStatus = async () => {
  try {
    const response = await systemMonitorApi.getSystemStatus()
    systemStatus.value = response.data

    // 更新概览卡片
    overviewCards.value[1].value = `${response.data.cpuUsage?.toFixed(1) || 0}%`
    overviewCards.value[2].value = `${response.data.memoryUsage?.toFixed(1) || 0}%`
    overviewCards.value[3].value = `${response.data.diskUsage?.toFixed(1) || 0}%`

    // 更新卡片状态
    overviewCards.value[1].status = getStatusClass(response.data.cpuUsage)
    overviewCards.value[2].status = getStatusClass(response.data.memoryUsage)
    overviewCards.value[3].status = getStatusClass(response.data.diskUsage)

  } catch (error) {
    console.error('获取系统状态失败:', error)
  }
}

const loadSystemMetrics = async () => {
  try {
    const response = await systemMonitorApi.getSystemMetrics()
    const metrics = response.data

    // 更新JVM指标
    if (metrics.jvmMemory) {
      jvmMetrics.value[0] = {
        label: '堆内存使用',
        value: `${metrics.jvmMemory.usedHeapMemory}MB / ${metrics.jvmMemory.maxHeapMemory}MB`,
        percentage: metrics.jvmMemory.heapMemoryUsage || 0
      }
      jvmMetrics.value[1] = {
        label: '非堆内存使用',
        value: `${metrics.jvmMemory.usedNonHeapMemory}MB / ${metrics.jvmMemory.maxNonHeapMemory}MB`,
        percentage: metrics.jvmMemory.nonHeapMemoryUsage || 0
      }
    }

  } catch (error) {
    console.error('获取系统指标失败:', error)
  }
}

const loadDatabaseStatus = async () => {
  try {
    const response = await systemMonitorApi.getDatabaseStatus()
    const dbStatus = response.data

    databaseStatus.value = {
      status: dbStatus.status,
      connections: dbStatus.connectionPool?.total || 0,
      maxConnections: dbStatus.connectionPool?.max || 0,
      version: dbStatus.version || '',
      qps: dbStatus.metrics?.queriesPerSecond || 0,
      connectionUsage: dbStatus.connectionPool?.usagePercent || 0
    }

  } catch (error) {
    console.error('获取数据库状态失败:', error)
  }
}

const loadRedisStatus = async () => {
  try {
    const response = await systemMonitorApi.getRedisStatus()
    const redis = response.data

    redisStatus.value = {
      status: redis.status,
      usedMemory: redis.memoryInfo?.usedMemory || 0,
      hitRate: redis.statsInfo?.hitRate || 0,
      connectedClients: redis.clientInfo?.connectedClients || 0,
      memoryUsage: redis.memoryInfo?.memoryUsagePercent || 0
    }

  } catch (error) {
    console.error('获取Redis状态失败:', error)
  }
}

const loadHealthCheck = async () => {
  try {
    const response = await systemMonitorApi.healthCheck()
    healthCheck.value = response.data

  } catch (error) {
    console.error('获取健康检查失败:', error)
  }
}

const refreshJvmMetrics = () => {
  loadSystemMetrics()
  ElMessage.success('JVM指标已刷新')
}

const refreshHealthCheck = () => {
  loadHealthCheck()
  ElMessage.success('健康检查已刷新')
}

const changeTimeRange = (range) => {
  timeRange.value = range
  updatePerformanceChart()
}

const getStatusClass = (percentage) => {
  if (percentage < 50) return 'success'
  if (percentage < 80) return 'warning'
  return 'danger'
}

const getProgressColor = (percentage) => {
  if (percentage < 50) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
}

const getHealthName = (key) => {
  const nameMap = {
    'database': '数据库',
    'redis': 'Redis缓存',
    'diskSpace': '磁盘空间',
    'memory': '内存'
  }
  return nameMap[key] || key
}

const showHealthDetails = (key, details) => {
  healthDetailContent.value = details
  healthDetailVisible.value = true
}

const formatBytes = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const refreshAllData = async () => {
  await Promise.all([
    loadSystemStatus(),
    loadSystemMetrics(),
    loadDatabaseStatus(),
    loadRedisStatus(),
    loadHealthCheck()
  ])

  // 更新图表
  updatePerformanceChart()
  updateResourceChart()
}

// 生命周期
onMounted(() => {
  initCharts()
  refreshAllData()

  // 设置定时刷新
  refreshTimer = setInterval(refreshAllData, 30000) // 30秒刷新一次
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  if (performanceChartInstance) {
    performanceChartInstance.dispose()
  }
  if (resourceChartInstance) {
    resourceChartInstance.dispose()
  }
})
</script>

<style scoped>
.system-monitor {
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

.overview-cards {
  margin-bottom: 20px;
}

.overview-card {
  height: 120px;
}

.overview-card.success {
  border-left: 4px solid #67c23a;
}

.overview-card.info {
  border-left: 4px solid #409eff;
}

.overview-card.warning {
  border-left: 4px solid #e6a23c;
}

.overview-card.danger {
  border-left: 4px solid #f56c6c;
}

.card-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.card-icon {
  margin-right: 16px;
}

.card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.card-desc {
  font-size: 12px;
  color: #606266;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  width: 100%;
  height: 320px;
}

.metrics-section {
  margin-bottom: 20px;
}

.metrics-card {
  height: 300px;
}

.metrics-content {
  padding: 10px 0;
}

.metric-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.metric-label {
  font-size: 14px;
  color: #606266;
  flex: 1;
}

.metric-value {
  display: flex;
  align-items: center;
  flex: 2;
  gap: 12px;
}

.value-text {
  font-size: 14px;
  color: #303133;
  min-width: 80px;
  text-align: right;
}

.database-info,
.redis-info {
  padding: 10px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-label {
  color: #606266;
}

.info-value {
  color: #303133;
  font-weight: 500;
}

.health-section {
  margin-bottom: 20px;
}

.health-check {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.health-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  min-width: 200px;
  background: #fafafa;
}

.health-item.healthy {
  border-color: #67c23a;
  background: #f0f9ff;
}

.health-item.unhealthy {
  border-color: #f56c6c;
  background: #fef0f0;
}

.health-icon {
  margin-right: 12px;
}

.health-info {
  flex: 1;
}

.health-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.health-status {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.health-details {
  font-size: 12px;
}
</style>