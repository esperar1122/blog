<template>
  <div class="comment-statistics-panel">
    <!-- Statistics Overview -->
    <div class="statistics-overview">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <el-icon class="stat-icon blue"><ChatLineSquare /></el-icon>
              <div class="stat-content">
                <div class="stat-value">{{ statistics?.totalComments || 0 }}</div>
                <div class="stat-label">总评论数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <el-icon class="stat-icon green"><Check /></el-icon>
              <div class="stat-content">
                <div class="stat-value">{{ statistics?.activeComments || 0 }}</div>
                <div class="stat-label">活跃评论</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <el-icon class="stat-icon orange"><Edit /></el-icon>
              <div class="stat-content">
                <div class="stat-value">{{ statistics?.editedComments || 0 }}</div>
                <div class="stat-label">已编辑评论</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <el-icon class="stat-icon red"><Delete /></el-icon>
              <div class="stat-content">
                <div class="stat-value">{{ statistics?.deletedComments || 0 }}</div>
                <div class="stat-label">已删除评论</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- Report Statistics -->
    <div class="report-statistics">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>举报统计</span>
            <el-button type="primary" size="small" @click="refreshStatistics">
              刷新数据
            </el-button>
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :span="8">
            <div class="report-stat-item">
              <div class="stat-circle warning">
                {{ statistics?.pendingReports || 0 }}
              </div>
              <div class="stat-info">
                <div class="stat-title">待处理举报</div>
                <div class="stat-desc">需要审核的举报</div>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="report-stat-item">
              <div class="stat-circle success">
                {{ statistics?.approvedReports || 0 }}
              </div>
              <div class="stat-info">
                <div class="stat-title">已通过举报</div>
                <div class="stat-desc">确认违规的举报</div>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="report-stat-item">
              <div class="stat-circle danger">
                {{ statistics?.rejectedReports || 0 }}
              </div>
              <div class="stat-info">
                <div class="stat-title">已拒绝举报</div>
                <div class="stat-desc">确认无问题的举报</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- Charts Section -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>评论状态分布</span>
            </template>
            <div class="chart-container">
              <div class="pie-chart-placeholder">
                <div class="chart-segment normal" style="flex: 3">
                  正常 ({{ ((statistics?.activeComments || 0) / (statistics?.totalComments || 1) * 100).toFixed(1) }}%)
                </div>
                <div class="chart-segment deleted" style="flex: 1">
                  已删除 ({{ ((statistics?.deletedComments || 0) / (statistics?.totalComments || 1) * 100).toFixed(1) }}%)
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <span>举报处理情况</span>
            </template>
            <div class="chart-container">
              <div class="progress-list">
                <div class="progress-item">
                  <div class="progress-label">待处理</div>
                  <div class="progress-bar">
                    <div
                      class="progress-fill warning"
                      :style="{ width: getPercentage(statistics?.pendingReports || 0, statistics?.totalReports || 1) + '%' }"
                    ></div>
                  </div>
                  <div class="progress-value">{{ statistics?.pendingReports || 0 }}</div>
                </div>
                <div class="progress-item">
                  <div class="progress-label">已通过</div>
                  <div class="progress-bar">
                    <div
                      class="progress-fill success"
                      :style="{ width: getPercentage(statistics?.approvedReports || 0, statistics?.totalReports || 1) + '%' }"
                    ></div>
                  </div>
                  <div class="progress-value">{{ statistics?.approvedReports || 0 }}</div>
                </div>
                <div class="progress-item">
                  <div class="progress-label">已拒绝</div>
                  <div class="progress-bar">
                    <div
                      class="progress-fill danger"
                      :style="{ width: getPercentage(statistics?.rejectedReports || 0, statistics?.totalReports || 1) + '%' }"
                    ></div>
                  </div>
                  <div class="progress-value">{{ statistics?.rejectedReports || 0 }}</div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- Recent Activity -->
    <div class="recent-activity">
      <el-card>
        <template #header>
          <span>管理活动概览</span>
        </template>
        <div class="activity-list">
          <div class="activity-item">
            <el-icon class="activity-icon blue"><TrendCharts /></el-icon>
            <div class="activity-content">
              <div class="activity-title">今日新增评论</div>
              <div class="activity-desc">较昨日 +12.5%</div>
            </div>
            <div class="activity-value">+24</div>
          </div>
          <div class="activity-item">
            <el-icon class="activity-icon orange"><Warning /></el-icon>
            <div class="activity-content">
              <div class="activity-title">今日新增举报</div>
              <div class="activity-desc">较昨日 -8.3%</div>
            </div>
            <div class="activity-value">+3</div>
          </div>
          <div class="activity-item">
            <el-icon class="activity-icon green"><UserFilled /></el-icon>
            <div class="activity-content">
              <div class="activity-title">活跃评论用户</div>
              <div class="activity-desc">过去7天</div>
            </div>
            <div class="activity-value">156</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatLineSquare,
  Check,
  Edit,
  Delete,
  TrendCharts,
  Warning,
  UserFilled
} from '@element-plus/icons-vue'
import { useCommentModerationStore } from '@/stores/commentModerationStore'

const commentModerationStore = useCommentModerationStore()

const statistics = ref(commentModerationStore.statistics)

const refreshStatistics = async () => {
  try {
    await commentModerationStore.fetchStatistics()
    statistics.value = commentModerationStore.statistics
    ElMessage.success('统计数据已更新')
  } catch (error) {
    ElMessage.error('刷新统计数据失败')
  }
}

const getPercentage = (value: number, total: number) => {
  return total > 0 ? Math.round((value / total) * 100) : 0
}

onMounted(async () => {
  await commentModerationStore.fetchStatistics()
  statistics.value = commentModerationStore.statistics
})
</script>

<style scoped>
.comment-statistics-panel {
  margin-top: 20px;
}

.statistics-overview {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
}

.stat-item {
  display: flex;
  align-items: center;
  height: 100%;
}

.stat-icon {
  font-size: 40px;
  margin-right: 16px;
}

.stat-icon.blue {
  color: #409eff;
}

.stat-icon.green {
  color: #67c23a;
}

.stat-icon.orange {
  color: #e6a23c;
}

.stat-icon.red {
  color: #f56c6c;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.report-statistics {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.report-stat-item {
  display: flex;
  align-items: center;
  padding: 20px;
  text-align: center;
}

.stat-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  color: white;
  margin-right: 16px;
  flex-shrink: 0;
}

.stat-circle.warning {
  background-color: #e6a23c;
}

.stat-circle.success {
  background-color: #67c23a;
}

.stat-circle.danger {
  background-color: #f56c6c;
}

.stat-info {
  flex: 1;
  text-align: left;
}

.stat-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-desc {
  font-size: 14px;
  color: #909399;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-container {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pie-chart-placeholder {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  display: flex;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.chart-segment {
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 14px;
  text-align: center;
  padding: 8px;
}

.chart-segment.normal {
  background-color: #67c23a;
}

.chart-segment.deleted {
  background-color: #f56c6c;
}

.progress-list {
  width: 100%;
}

.progress-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.progress-label {
  width: 60px;
  font-size: 14px;
  color: #606266;
  flex-shrink: 0;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background-color: #e4e7ed;
  border-radius: 4px;
  margin: 0 12px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  transition: width 0.3s ease;
}

.progress-fill.warning {
  background-color: #e6a23c;
}

.progress-fill.success {
  background-color: #67c23a;
}

.progress-fill.danger {
  background-color: #f56c6c;
}

.progress-value {
  width: 40px;
  text-align: right;
  font-weight: bold;
  color: #303133;
  flex-shrink: 0;
}

.recent-activity {
  margin-bottom: 20px;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #fafafa;
  border-radius: 8px;
  transition: background-color 0.3s ease;
}

.activity-item:hover {
  background-color: #f0f2f5;
}

.activity-icon {
  font-size: 24px;
  margin-right: 16px;
  flex-shrink: 0;
}

.activity-icon.blue {
  color: #409eff;
}

.activity-icon.orange {
  color: #e6a23c;
}

.activity-icon.green {
  color: #67c23a;
}

.activity-content {
  flex: 1;
}

.activity-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.activity-desc {
  font-size: 14px;
  color: #909399;
}

.activity-value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  flex-shrink: 0;
}
</style>