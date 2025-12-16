<template>
  <div class="system-metrics">
    <div class="metrics-header">
      <h3>系统详细指标</h3>
      <div class="header-actions">
        <el-select v-model="selectedTimeRange" @change="refreshMetrics">
          <el-option label="最近1小时" value="1h" />
          <el-option label="最近6小时" value="6h" />
          <el-option label="最近24小时" value="24h" />
        </el-select>
        <el-button @click="refreshMetrics" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
        <el-button type="primary" @click="exportMetrics">
          <el-icon><Download /></el-icon>
          导出数据
        </el-button>
      </div>
    </div>

    <!-- CPU指标 -->
    <el-card class="metric-card" header="CPU指标">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="metric-item">
            <span class="metric-label">系统CPU使用率:</span>
            <span class="metric-value">{{ cpuMetrics.systemCpuUsage }}%</span>
          </div>
          <div class="metric-item">
            <span class="metric-label">进程CPU使用率:</span>
            <span class="metric-value">{{ cpuMetrics.processCpuUsage }}%</span>
          </div>
          <div class="metric-item">
            <span class="metric-label">CPU核心数:</span>
            <span class="metric-value">{{ cpuMetrics.availableProcessors }}</span>
          </div>
          <div class="metric-item">
            <span class="metric-label">系统负载:</span>
            <span class="metric-value">{{ cpuMetrics.systemLoadAverage }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div ref="cpuChart" class="chart-small"></div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 内存指标 -->
    <el-card class="metric-card" header="内存指标">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="metric-item">
            <span class="metric-label">总内存:</span>
            <span class="metric-value">{{ formatBytes(memoryMetrics.totalSystemMemory) }}</span>
          </div>
          <div class="metric-item">
            <span class="metric-label">已用内存:</span>
            <span class="metric-value">{{ formatBytes(memoryMetrics.usedSystemMemory) }}</span>
          </div>
          <div class="metric-item">
            <span class="metric-label">可用内存:</span>
            <span class="metric-value">{{ formatBytes(memoryMetrics.freeSystemMemory) }}</span>
          </div>
          <div class="metric-item">
            <span class="metric-label">内存使用率:</span>
            <span class="metric-value">{{ memoryMetrics.systemMemoryUsage }}%</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div ref="memoryChart" class="chart-small"></div>
        </el-col>
      </el-row>
    </el-card>

    <!-- JVM指标 -->
    <el-card class="metric-card" header="JVM指标">
      <el-tabs v-model="activeJvmTab">
        <el-tab-pane label="内存" name="memory">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="jvm-metrics">
                <div class="metric-group">
                  <h4>堆内存</h4>
                  <div class="metric-item">
                    <span class="metric-label">最大堆内存:</span>
                    <span class="metric-value">{{ jvmMetrics.maxHeapMemory }} MB</span>
                  </div>
                  <div class="metric-item">
                    <span class="metric-label">已用堆内存:</span>
                    <span class="metric-value">{{ jvmMetrics.usedHeapMemory }} MB</span>
                  </div>
                  <div class="metric-item">
                    <span class="metric-label">堆内存使用率:</span>
                    <span class="metric-value">{{ jvmMetrics.heapMemoryUsage }}%</span>
                  </div>
                  <el-progress
                    :percentage="jvmMetrics.heapMemoryUsage"
                    :color="getProgressColor(jvmMetrics.heapMemoryUsage)"
                    :show-text="true"
                  />
                </div>

                <div class="metric-group">
                  <h4>非堆内存</h4>
                  <div class="metric-item">
                    <span class="metric-label">最大非堆内存:</span>
                    <span class="metric-value">{{ jvmMetrics.maxNonHeapMemory }} MB</span>
                  </div>
                  <div class="metric-item">
                    <span class="metric-label">已用非堆内存:</span>
                    <span class="metric-value">{{ jvmMetrics.usedNonHeapMemory }} MB</span>
                  </div>
                  <div class="metric-item">
                    <span class="metric-label">非堆内存使用率:</span>
                    <span class="metric-value">{{ jvmMetrics.nonHeapMemoryUsage }}%</span>
                  </div>
                  <el-progress
                    :percentage="jvmMetrics.nonHeapMemoryUsage"
                    :color="getProgressColor(jvmMetrics.nonHeapMemoryUsage)"
                    :show-text="true"
                  />
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div ref="jvmMemoryChart" class="chart-medium"></div>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="垃圾回收" name="gc">
          <el-table :data="jvmMetrics.garbageCollection" border>
            <el-table-column prop="name" label="GC名称" />
            <el-table-column prop="collectionCount" label="回收次数" />
            <el-table-column prop="collectionTime" label="总耗时(ms)" />
            <el-table-column label="平均耗时(ms)">
              <template #default="scope">
                {{ scope.row.collectionCount > 0 ? (scope.row.collectionTime / scope.row.collectionCount).toFixed(2) : 0 }}
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="线程" name="threads">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="thread-metrics">
                <div class="metric-item">
                  <span class="metric-label">当前线程数:</span>
                  <span class="metric-value">{{ jvmMetrics.threads.threadCount }}</span>
                </div>
                <div class="metric-item">
                  <span class="metric-label">峰值线程数:</span>
                  <span class="metric-value">{{ jvmMetrics.threads.peakThreadCount }}</span>
                </div>
                <div class="metric-item">
                  <span class="metric-label">守护线程数:</span>
                  <span class="metric-value">{{ jvmMetrics.threads.daemonThreadCount }}</span>
                </div>
                <div class="metric-item">
                  <span class="metric-label">阻塞线程数:</span>
                  <span class="metric-value">{{ jvmMetrics.threads.blockedThreadCount }}</span>
                </div>
                <div class="metric-item">
                  <span class="metric-label">等待线程数:</span>
                  <span class="metric-value">{{ jvmMetrics.threads.waitingThreadCount }}</span>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div ref="threadChart" class="chart-medium"></div>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="类加载" name="classLoading">
          <div class="class-loading-metrics">
            <div class="metric-item">
              <span class="metric-label">已加载类数:</span>
              <span class="metric-value">{{ jvmMetrics.classLoading.loadedClassCount }}</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">总加载类数:</span>
              <span class="metric-value">{{ jvmMetrics.classLoading.totalLoadedClassCount }}</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">已卸载类数:</span>
              <span class="metric-value">{{ jvmMetrics.classLoading.unloadedClassCount }}</span>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 系统负载信息 -->
    <el-card class="metric-card" header="系统负载信息">
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="load-info">
            <h4>CPU负载</h4>
            <div class="metric-item">
              <span class="metric-label">系统负载平均值:</span>
              <span class="metric-value">{{ systemLoad.systemLoadAverage }}</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">系统CPU使用率:</span>
              <span class="metric-value">{{ systemLoad.systemCpuLoad }}%</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">进程CPU使用率:</span>
              <span class="metric-value">{{ systemLoad.processCpuLoad }}%</span>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="load-info">
            <h4>物理内存</h4>
            <div class="metric-item">
              <span class="metric-label">总物理内存:</span>
              <span class="metric-value">{{ formatBytes(systemLoad.totalPhysicalMemory) }}</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">可用物理内存:</span>
              <span class="metric-value">{{ formatBytes(systemLoad.freePhysicalMemory) }}</span>
            </div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="load-info">
            <h4>交换空间</h4>
            <div class="metric-item">
              <span class="metric-label">总交换空间:</span>
              <span class="metric-value">{{ formatBytes(systemLoad.totalSwapSpace) }}</span>
            </div>
            <div class="metric-item">
              <span class="metric-label">可用交换空间:</span>
              <span class="metric-value">{{ formatBytes(systemLoad.freeSwapSpace) }}</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { systemMonitorApi } from '@/api/admin/monitor'

// 响应式数据
const loading = ref(false)
const selectedTimeRange = ref('1h')
const activeJvmTab = ref('memory')

const cpuMetrics = ref({})
const memoryMetrics = ref({})
const jvmMetrics = ref({
  garbageCollection: [],
  threads: {},
  classLoading: {}
})
const systemLoad = ref({})

// 图表引用
const cpuChart = ref(null)
const memoryChart = ref(null)
const jvmMemoryChart = ref(null)
const threadChart = ref(null)

let cpuChartInstance = null
let memoryChartInstance = null
let jvmMemoryChartInstance = null
let threadChartInstance = null
let refreshTimer = null

// 方法
const initCharts = () => {
  // CPU图表
  if (cpuChart.value) {
    cpuChartInstance = echarts.init(cpuChart.value)
    updateCpuChart()
  }

  // 内存图表
  if (memoryChart.value) {
    memoryChartInstance = echarts.init(memoryChart.value)
    updateMemoryChart()
  }

  // JVM内存图表
  if (jvmMemoryChart.value) {
    jvmMemoryChartInstance = echarts.init(jvmMemoryChart.value)
    updateJvmMemoryChart()
  }

  // 线程图表
  if (threadChart.value) {
    threadChartInstance = echarts.init(threadChart.value)
    updateThreadChart()
  }
}

const updateCpuChart = () => {
  const option = {
    title: {
      text: 'CPU使用率趋势',
      left: 'center',
      textStyle: { fontSize: 12 }
    },
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: Array.from({ length: 10 }, (_, i) => `${9 - i}分钟前`)
    },
    yAxis: {
      type: 'value',
      max: 100,
      axisLabel: { formatter: '{value}%' }
    },
    series: [
      {
        name: '系统CPU',
        type: 'line',
        data: Array.from({ length: 10 }, () => Math.random() * 50 + 10),
        smooth: true,
        itemStyle: { color: '#409eff' }
      },
      {
        name: '进程CPU',
        type: 'line',
        data: Array.from({ length: 10 }, () => Math.random() * 30 + 5),
        smooth: true,
        itemStyle: { color: '#67c23a' }
      }
    ]
  }
  cpuChartInstance.setOption(option)
}

const updateMemoryChart = () => {
  const option = {
    title: {
      text: '内存使用情况',
      left: 'center',
      textStyle: { fontSize: 12 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}MB ({d}%)'
    },
    series: [
      {
        name: '内存分布',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
        data: [
          { value: memoryMetrics.value.usedSystemMemory / 1024 / 1024, name: '已用内存', itemStyle: { color: '#f56c6c' } },
          { value: memoryMetrics.value.freeSystemMemory / 1024 / 1024, name: '可用内存', itemStyle: { color: '#67c23a' } }
        ]
      }
    ]
  }
  memoryChartInstance.setOption(option)
}

const updateJvmMemoryChart = () => {
  const option = {
    title: {
      text: 'JVM内存分布',
      left: 'center',
      textStyle: { fontSize: 12 }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}MB ({d}%)'
    },
    legend: {
      bottom: 0
    },
    series: [
      {
        name: '堆内存',
        type: 'pie',
        radius: ['20%', '40%'],
        center: ['25%', '50%'],
        data: [
          { value: jvmMetrics.value.usedHeapMemory, name: '已用堆内存', itemStyle: { color: '#f56c6c' } },
          { value: jvmMetrics.value.maxHeapMemory - jvmMetrics.value.usedHeapMemory, name: '空闲堆内存', itemStyle: { color: '#409eff' } }
        ]
      },
      {
        name: '非堆内存',
        type: 'pie',
        radius: ['20%', '40%'],
        center: ['75%', '50%'],
        data: [
          { value: jvmMetrics.value.usedNonHeapMemory, name: '已用非堆内存', itemStyle: { color: '#e6a23c' } },
          { value: jvmMetrics.value.maxNonHeapMemory - jvmMetrics.value.usedNonHeapMemory, name: '空闲非堆内存', itemStyle: { color: '#67c23a' } }
        ]
      }
    ]
  }
  jvmMemoryChartInstance.setOption(option)
}

const updateThreadChart = () => {
  const option = {
    title: {
      text: '线程状态分布',
      left: 'center',
      textStyle: { fontSize: 12 }
    },
    tooltip: {
      trigger: 'item'
    },
    series: [
      {
        name: '线程状态',
        type: 'pie',
        radius: '60%',
        center: ['50%', '50%'],
        data: [
          { value: jvmMetrics.value.threads.threadCount, name: '总线程数', itemStyle: { color: '#409eff' } },
          { value: jvmMetrics.value.threads.daemonThreadCount, name: '守护线程', itemStyle: { color: '#67c23a' } },
          { value: jvmMetrics.value.threads.blockedThreadCount, name: '阻塞线程', itemStyle: { color: '#f56c6c' } },
          { value: jvmMetrics.value.threads.waitingThreadCount, name: '等待线程', itemStyle: { color: '#e6a23c' } }
        ]
      }
    ]
  }
  threadChartInstance.setOption(option)
}

const refreshMetrics = async () => {
  loading.value = true
  try {
    const [metricsResponse, loadResponse] = await Promise.all([
      systemMonitorApi.getSystemMetrics(),
      systemMonitorApi.getSystemLoad()
    ])

    const data = metricsResponse.data
    const loadData = loadResponse.data

    // 更新CPU指标
    if (data.cpu) {
      cpuMetrics.value = {
        systemCpuUsage: data.cpu.systemCpuUsage?.toFixed(2) || 0,
        processCpuUsage: data.cpu.processCpuUsage?.toFixed(2) || 0,
        availableProcessors: data.cpu.availableProcessors || 0,
        systemLoadAverage: data.cpu.systemLoadAverage?.toFixed(2) || 0
      }
    }

    // 更新内存指标
    if (data.memory) {
      memoryMetrics.value = {
        totalSystemMemory: data.memory.totalSystemMemory || 0,
        usedSystemMemory: data.memory.usedSystemMemory || 0,
        freeSystemMemory: data.memory.freeSystemMemory || 0,
        systemMemoryUsage: data.memory.systemMemoryUsage?.toFixed(2) || 0
      }
    }

    // 更新JVM指标
    if (data.jvmMemory) {
      jvmMetrics.value = {
        ...jvmMetrics.value,
        maxHeapMemory: data.jvmMemory.maxHeapMemory || 0,
        usedHeapMemory: data.jvmMemory.usedHeapMemory || 0,
        heapMemoryUsage: data.jvmMemory.heapMemoryUsage || 0,
        maxNonHeapMemory: data.jvmMemory.maxNonHeapMemory || 0,
        usedNonHeapMemory: data.jvmMemory.usedNonHeapMemory || 0,
        nonHeapMemoryUsage: data.jvmMemory.nonHeapMemoryUsage || 0,
        garbageCollection: data.garbageCollection || [],
        threads: data.threads || {},
        classLoading: data.classLoading || {}
      }
    }

    // 更新系统负载
    systemLoad.value = loadData

    // 更新图表
    updateCpuChart()
    updateMemoryChart()
    updateJvmMemoryChart()
    updateThreadChart()

    ElMessage.success('指标数据已刷新')
  } catch (error) {
    console.error('刷新指标失败:', error)
    ElMessage.error('刷新指标失败')
  } finally {
    loading.value = false
  }
}

const exportMetrics = () => {
  // 导出指标数据为JSON文件
  const metricsData = {
    timestamp: new Date().toISOString(),
    timeRange: selectedTimeRange.value,
    cpu: cpuMetrics.value,
    memory: memoryMetrics.value,
    jvm: jvmMetrics.value,
    systemLoad: systemLoad.value
  }

  const dataStr = JSON.stringify(metricsData, null, 2)
  const dataBlob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(dataBlob)

  const link = document.createElement('a')
  link.href = url
  link.download = `system-metrics-${new Date().toISOString()}.json`
  link.click()

  URL.revokeObjectURL(url)
  ElMessage.success('指标数据已导出')
}

const formatBytes = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getProgressColor = (percentage) => {
  if (percentage < 50) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
}

// 生命周期
onMounted(() => {
  initCharts()
  refreshMetrics()

  // 设置定时刷新
  refreshTimer = setInterval(refreshMetrics, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
  // 销毁图表实例
  ;[cpuChartInstance, memoryChartInstance, jvmMemoryChartInstance, threadChartInstance].forEach(chart => {
    if (chart) chart.dispose()
  })
})
</script>

<style scoped>
.system-metrics {
  padding: 20px;
}

.metrics-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.metrics-header h3 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.metric-card {
  margin-bottom: 20px;
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

.chart-small {
  height: 200px;
  width: 100%;
}

.chart-medium {
  height: 250px;
  width: 100%;
}

.jvm-metrics {
  padding: 16px 0;
}

.metric-group {
  margin-bottom: 24px;
}

.metric-group h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
}

.thread-metrics,
.class-loading-metrics,
.load-info {
  padding: 16px 0;
}

.load-info h4 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 16px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 8px;
}
</style>