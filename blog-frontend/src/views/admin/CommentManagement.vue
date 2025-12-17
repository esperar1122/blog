<template>
  <div class="comment-management">
    <el-page-header @back="goBack">
      <template #content>
        <span class="text-large font-600 mr-3">评论管理</span>
      </template>
    </el-page-header>

    <!-- Statistics Cards -->
    <div class="statistics-cards">
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
              <el-icon class="stat-icon orange"><WarningFilled /></el-icon>
              <div class="stat-content">
                <div class="stat-value">{{ statistics?.pendingReports || 0 }}</div>
                <div class="stat-label">待处理举报</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <el-icon class="stat-icon red"><CircleCloseFilled /></el-icon>
              <div class="stat-content">
                <div class="stat-value">{{ statistics?.deletedComments || 0 }}</div>
                <div class="stat-label">已删除评论</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- Tabs for different sections -->
    <el-tabs v-model="activeTab" class="comment-tabs">
      <el-tab-pane label="评论审核" name="comments">
        <CommentModerationPanel />
      </el-tab-pane>
      <el-tab-pane label="举报管理" name="reports">
        <ReportManagement />
      </el-tab-pane>
      <el-tab-pane label="黑名单管理" name="blacklist">
        <BlacklistManager />
      </el-tab-pane>
      <el-tab-pane label="敏感词管理" name="sensitive">
        <SensitiveWordManager />
      </el-tab-pane>
      <el-tab-pane label="统计分析" name="statistics">
        <CommentStatisticsPanel />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCommentModerationStore } from '@/stores/commentModerationStore'
import {
  ChatLineSquare,
  Check,
  WarningFilled,
  CircleCloseFilled
} from '@element-plus/icons-vue'
import CommentModerationPanel from '@/components/comment/moderation/CommentModerationPanel.vue'
import ReportManagement from '@/components/comment/moderation/ReportManagement.vue'
import BlacklistManager from '@/components/comment/moderation/BlacklistManager.vue'
import SensitiveWordManager from '@/components/comment/moderation/SensitiveWordManager.vue'
import CommentStatisticsPanel from '@/components/comment/moderation/CommentStatisticsPanel.vue'

const router = useRouter()
const commentModerationStore = useCommentModerationStore()

const activeTab = ref('comments')
const statistics = ref(commentModerationStore.statistics)

const goBack = () => {
  router.push('/admin')
}

onMounted(async () => {
  await commentModerationStore.fetchStatistics()
  statistics.value = commentModerationStore.statistics
})
</script>

<style scoped>
.comment-management {
  padding: 20px;
}

.statistics-cards {
  margin: 20px 0;
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

.comment-tabs {
  margin-top: 20px;
}

.text-large {
  font-size: 16px;
}

.font-600 {
  font-weight: 600;
}

.mr-3 {
  margin-right: 12px;
}
</style>