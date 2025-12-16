<template>
  <div class="admin-dashboard">
    <div class="dashboard-header">
      <h1>管理员控制台</h1>
      <p class="subtitle">内容管理和系统设置</p>
    </div>

    <ContentStats
      ref="contentStatsRef"
      @refresh="handleRefreshStats"
      @export="handleExport"
      @manage-sensitive="handleManageSensitive"
      @system-settings="handleSystemSettings"
    />

    <el-row :gutter="20">
      <el-col :span="12">
        <ContentManagement ref="contentManagementRef" />
      </el-col>
      <el-col :span="12">
        <el-tabs v-model="activeTab" type="border-card">
          <el-tab-pane label="敏感词管理" name="sensitive">
            <SensitiveWordManagement v-if="activeTab === 'sensitive'" />
          </el-tab-pane>
          <el-tab-pane label="系统设置" name="settings">
            <SystemSettings v-if="activeTab === 'settings'" />
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import ContentStats from '@/components/admin/ContentStats.vue'
import ContentManagement from '@/components/admin/ContentManagement.vue'
import SensitiveWordManagement from '@/components/admin/SensitiveWordManagement.vue'
import SystemSettings from '@/components/admin/SystemSettings.vue'

const router = useRouter()

const contentStatsRef = ref()
const contentManagementRef = ref()
const activeTab = ref('sensitive')

const handleRefreshStats = () => {
  contentStatsRef.value?.loadStats()
}

const handleExport = () => {
  contentManagementRef.value?.handleExport()
}

const handleManageSensitive = () => {
  activeTab.value = 'sensitive'
}

const handleSystemSettings = () => {
  activeTab.value = 'settings'
}
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.dashboard-header {
  margin-bottom: 30px;
  text-align: center;
}

.dashboard-header h1 {
  margin: 0;
  color: #303133;
  font-size: 32px;
  font-weight: 500;
}

.subtitle {
  margin: 8px 0 0 0;
  color: #909399;
  font-size: 16px;
}

:deep(.el-tabs__content) {
  padding: 0;
}

:deep(.el-tab-pane) {
  background-color: #fff;
}
</style>