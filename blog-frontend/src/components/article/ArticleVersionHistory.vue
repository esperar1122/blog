<template>
  <div class="article-version-history">
    <div class="header">
      <h3>版本历史</h3>
      <div class="header-info">
        <el-tag type="info" size="small">
          共 {{ versions.length }} 个版本
        </el-tag>
      </div>
    </div>

    <div class="versions-container">
      <div
        v-if="loading"
        class="loading-container"
      >
        <el-skeleton :rows="5" animated />
      </div>

      <div
        v-else-if="versions.length === 0"
        class="empty-state"
      >
        <el-empty description="暂无版本历史" />
      </div>

      <div
        v-else
        class="versions-list"
      >
        <div
          v-for="(version, index) in versions"
          :key="version.id"
          class="version-item"
          :class="{ 'current-version': index === 0 }"
        >
          <div class="version-header">
            <div class="version-info">
              <span class="version-number">v{{ version.versionNumber }}</span>
              <el-tag
                v-if="index === 0"
                type="success"
                size="small"
                class="current-tag"
              >
                当前版本
              </el-tag>
              <span class="change-reason">{{ version.changeReason || '无备注' }}</span>
            </div>
            <div class="version-meta">
              <span class="editor">
                <el-avatar :size="20" :src="version.editorAvatar" />
                {{ version.editorName }}
              </span>
              <span class="time">
                {{ formatTime(version.createTime) }}
              </span>
            </div>
          </div>

          <div class="version-content">
            <div class="version-title">
              <span class="label">标题：</span>
              <span class="title-text">{{ version.title }}</span>
            </div>
            <div class="version-summary" v-if="version.summary">
              <span class="label">摘要：</span>
              <span class="summary-text">{{ version.summary }}</span>
            </div>
            <div class="version-actions">
              <el-button
                type="text"
                size="small"
                @click="handleViewVersion(version)"
              >
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
              <el-button
                v-if="index > 0 && canRestore"
                type="text"
                size="small"
                @click="handleRestoreVersion(version)"
              >
                <el-icon><RefreshLeft /></el-icon>
                恢复此版本
              </el-button>
              <el-button
                type="text"
                size="small"
                @click="handleCompareVersion(version, index > 0 ? versions[index - 1] : null)"
              >
                <el-icon><Document /></el-icon>
                对比版本
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 版本详情对话框 -->
    <VersionDetailDialog
      v-model="showVersionDialog"
      :version="selectedVersion"
    />

    <!-- 版本对比对话框 -->
    <VersionCompareDialog
      v-model="showCompareDialog"
      :version1="compareVersion1"
      :version2="compareVersion2"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, type PropType } from 'vue'
import { ElTag, ElSkeleton, ElEmpty, ElButton, ElAvatar, ElIcon } from 'element-plus'
import { View, RefreshLeft, Document } from '@element-plus/icons-vue'
import type { ArticleVersion } from '@/types/article'
import { getArticleVersions } from '@/api/article'
import VersionDetailDialog from './VersionDetailDialog.vue'
import VersionCompareDialog from './VersionCompareDialog.vue'

const props = defineProps({
  articleId: {
    type: Number,
    required: true
  },
  canRestore: {
    type: Boolean,
    default: true
  }
})

const loading = ref(false)
const versions = ref<ArticleVersion[]>([])

const showVersionDialog = ref(false)
const selectedVersion = ref<ArticleVersion | null>(null)

const showCompareDialog = ref(false)
const compareVersion1 = ref<ArticleVersion | null>(null)
const compareVersion2 = ref<ArticleVersion | null>(null)

// 加载版本历史
async function loadVersions() {
  try {
    loading.value = true
    versions.value = await getArticleVersions(props.articleId)
  } catch (error) {
    console.error('加载版本历史失败:', error)
  } finally {
    loading.value = false
  }
}

// 格式化时间
function formatTime(time: string): string {
  return new Date(time).toLocaleString()
}

// 查看版本详情
function handleViewVersion(version: ArticleVersion) {
  selectedVersion.value = version
  showVersionDialog.value = true
}

// 恢复版本
async function handleRestoreVersion(version: ArticleVersion) {
  try {
    // 这里应该调用恢复版本的API
    console.log('恢复版本:', version)
    // await restoreArticleVersion(props.articleId, version.id)
    // 刷新版本列表
    // await loadVersions()
  } catch (error) {
    console.error('恢复版本失败:', error)
  }
}

// 对比版本
function handleCompareVersion(version1: ArticleVersion, version2: ArticleVersion | null) {
  compareVersion1.value = version1
  compareVersion2.value = version2
  showCompareDialog.value = true
}

onMounted(() => {
  loadVersions()
})
</script>

<style scoped>
.article-version-history {
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

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.versions-container {
  padding: 20px;
}

.loading-container {
  padding: 20px;
}

.empty-state {
  padding: 40px;
}

.versions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.version-item {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.2s ease;
  position: relative;
}

.version-item:hover {
  border-color: #3b82f6;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.1);
}

.version-item.current-version {
  border-color: #10b981;
  background-color: #f0fdf4;
}

.version-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f3f4f6;
}

.version-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.version-number {
  font-weight: 600;
  color: #3b82f6;
  font-size: 16px;
}

.current-tag {
  background-color: #dcfce7;
  border-color: #bbf7d0;
  color: #166534;
}

.change-reason {
  color: #6b7280;
  font-size: 14px;
}

.version-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #6b7280;
  font-size: 14px;
}

.editor {
  display: flex;
  align-items: center;
  gap: 6px;
}

.time {
  font-size: 12px;
}

.version-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.version-title, .version-summary {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.label {
  font-weight: 500;
  color: #6b7280;
  font-size: 14px;
  min-width: 50px;
  flex-shrink: 0;
}

.title-text, .summary-text {
  color: #374151;
  font-size: 14px;
  line-height: 1.5;
  flex: 1;
}

.title-text {
  font-weight: 500;
}

.version-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.version-actions .el-button {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  font-size: 12px;
  height: auto;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .version-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .version-meta {
    align-self: flex-end;
  }

  .version-actions {
    flex-wrap: wrap;
  }
}
</style>